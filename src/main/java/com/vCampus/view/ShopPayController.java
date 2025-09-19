package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Student;
import com.vCampus.entity.User;
import com.vCampus.service.IShopService;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopPayController extends BaseController {

    @FXML private Label lbOrderId;
    @FXML private Label lbBalance;
    @FXML private Label lbAmount;

    private final IShopService shopService = ServiceFactory.getShopService();
    private final IStudentService studentService = ServiceFactory.getStudentService();
    private String orderId;
    private double amount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<OrderItem> items = ShopSession.getCartItems();
        amount = shopService.calculateCartTotal(items);
        lbAmount.setText(String.format("%.2f", amount));
        lbBalance.setText(String.format("%.2f", ShopSession.getWalletBalance()));

        String studentId = resolveCurrentStudentId();
        if (studentId == null) {
            showError("当前账户未绑定学生信息，无法下单");
            lbOrderId.setText("-");
            return;
        }

        // 生成订单（待支付）
        orderId = shopService.purchase(studentId, items);
        ShopSession.setPendingOrderId(orderId);
        lbOrderId.setText(orderId == null ? "-" : orderId);
        if (orderId == null) {
            showError("订单创建失败，请检查购物车与库存");
        }
    }

    private String resolveCurrentStudentId() {
        User u = SessionContext.getCurrentUser();
        if (u == null) return null;
        if (u instanceof Student) {
            return ((Student) u).getStudentId();
        }
        try {
            Student s = studentService.getByUserId(u.getUserId());
            return s == null ? null : s.getStudentId();
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
        boolean ok = shopService.payOrder(orderId);
        if (ok) {
            ShopSession.setWalletBalance(ShopSession.getWalletBalance() - amount);
            ShopSession.clearCart();
            showSuccess("支付成功");
            close();
        } else {
            showError("支付失败");
        }
    }

    private void close() {
        try { ((javafx.stage.Stage) lbAmount.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }
}


