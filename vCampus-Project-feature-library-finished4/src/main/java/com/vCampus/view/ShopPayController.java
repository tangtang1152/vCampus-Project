package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Product;
import com.vCampus.entity.Student;
import com.vCampus.entity.User;
import client.net.SocketClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopPayController extends BaseController {

    @FXML private Label lbOrderId;
    @FXML private Label lbBalance;
    @FXML private Label lbAmount;

    // 移除直接依赖服务，改为通过 SocketClient 通信
    private String orderId;
    private double amount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<OrderItem> items = ShopSession.getCartItems();
        try {
            amount = calculateCartTotal(items);
            lbAmount.setText(String.format("%.2f", amount));
            lbBalance.setText(String.format("%.2f", ShopSession.getWalletBalance()));

            String studentId = resolveCurrentStudentId();
            if (studentId == null) {
                showError("当前账户未绑定学生信息，无法下单");
                lbOrderId.setText("-");
                return;
            }

            // 生成订单（待支付）
            orderId = createOrder(studentId, items);
            ShopSession.setPendingOrderId(orderId);
            lbOrderId.setText(orderId == null ? "-" : orderId);
            if (orderId == null) {
                showError("订单创建失败，请检查购物车与库存");
            }
        } catch (Exception e) {
            showError("初始化支付页面失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String resolveCurrentStudentId() {
        User u = SessionContext.getCurrentUser();
        if (u == null) return null;
        if (u instanceof Student) {
            return ((Student) u).getStudentId();
        }
        try {
            String request = "GET_STUDENT_BY_USER_ID:" + u.getUserId();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                Student s = parseStudentFromResponse(response);
                return s == null ? null : s.getStudentId();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML private void onCancel() {
        close();
    }

    @FXML private void onPay() {
        if (orderId == null) { showError("订单生成失败"); return; }
        if (ShopSession.getWalletBalance() < amount) { showWarning("余额不足"); return; }
        try {
            String request = "PAY_ORDER:" + orderId;
            String response = SocketClient.sendRequest(request);
            boolean ok = response != null && response.startsWith("SUCCESS:PAY:");
            if (ok) {
                ShopSession.setWalletBalance(ShopSession.getWalletBalance() - amount);
                ShopSession.clearCart();
                showSuccess("支付成功");
                close();
            } else {
                showError("支付失败: " + response);
            }
        } catch (Exception e) {
            showError("支付失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void close() {
        try { ((javafx.stage.Stage) lbAmount.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }

    /**
     * 计算购物车总价
     */
    private double calculateCartTotal(List<OrderItem> items) {
        double total = 0.0;
        for (OrderItem item : items) {
            try {
                String request = "GET_PRODUCT_BY_ID:" + item.getProductId();
                String response = SocketClient.sendRequest(request);
                if (response != null && response.startsWith("SUCCESS:PRODUCT:")) {
                    Product product = parseProductFromResponse(response);
                    if (product != null) {
                        total += product.getPrice() * item.getQuantity();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取商品价格失败: " + e.getMessage());
            }
        }
        return total;
    }

    /**
     * 创建订单
     */
    private String createOrder(String studentId, List<OrderItem> items) {
        try {
            // 构建订单项字符串
            StringBuilder itemsStr = new StringBuilder();
            for (OrderItem item : items) {
                if (itemsStr.length() > 0) {
                    itemsStr.append("|");
                }
                itemsStr.append(item.getProductId()).append(",")
                       .append(item.getQuantity()).append(",")
                       .append(item.getSubtotal());
            }
            
            String request = "CREATE_ORDER:" + studentId + ":" + itemsStr.toString();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:ORDER:")) {
                return response.substring("SUCCESS:ORDER:".length());
            }
            return null;
        } catch (Exception e) {
            System.err.println("创建订单失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从服务器响应解析商品信息
     */
    private Product parseProductFromResponse(String response) {
        try {
            if (response != null && response.startsWith("SUCCESS:PRODUCT:")) {
                String data = response.substring("SUCCESS:PRODUCT:".length());
                String[] fields = data.split(",");
                if (fields.length >= 6) {
                    Product product = new Product();
                    product.setProductId(fields[0]);
                    product.setProductName(fields[1]);
                    product.setPrice(Double.parseDouble(fields[2]));
                    product.setStock(Integer.parseInt(fields[3]));
                    product.setCategory(fields[4]);
                    product.setDescription(fields[5]);
                    return product;
                }
            }
        } catch (Exception e) {
            System.err.println("解析商品数据异常: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从服务器响应解析学生信息
     */
    private Student parseStudentFromResponse(String response) {
        try {
            if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                String data = response.substring("SUCCESS:STUDENT:".length());
                String[] fields = data.split(",");
                if (fields.length >= 6) {
                    Student student = new Student();
                    student.setStudentId(fields[0]);
                    student.setStudentName(fields[1]);
                    student.setUserId(Integer.parseInt(fields[2]));
                    student.setClassName(fields[3]);
                    student.setSex(fields[4]);
                    student.setEmail(fields[5]);
                    return student;
                }
            }
        } catch (Exception e) {
            System.err.println("解析学生数据异常: " + e.getMessage());
        }
        return null;
    }
}
