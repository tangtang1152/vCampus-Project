package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
import com.vCampus.common.ConfigManager;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Product;
import com.vCampus.service.IShopService;
import com.vCampus.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopCartController extends BaseController {

    @FXML private TableView<OrderItem> tableCart;
    @FXML private TableColumn<OrderItem, String> colPid;
    @FXML private TableColumn<OrderItem, Integer> colQty;
    @FXML private TableColumn<OrderItem, Double> colSubtotal;
    @FXML private TableColumn<OrderItem, Void> colActions;
    @FXML private Label lbTotal;

    private final IShopService shopService = ServiceFactory.getShopService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colPid.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        addActionButtons();
        reload();
    }

    private void addActionButtons() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<OrderItem, Void> call(TableColumn<OrderItem, Void> param) {
                return new TableCell<>() {
                    private final Button btnAdd = new Button("+");
                    private final Button btnSub = new Button("-");
                    private final Button btnDel = new Button("删");
                    private final HBox box = new HBox(6, btnSub, btnAdd, btnDel);
                    {
                        btnAdd.setOnAction(e -> changeQty(1));
                        btnSub.setOnAction(e -> changeQty(-1));
                        btnDel.setOnAction(e -> removeItem());
                    }
                    private void changeQty(int delta) {
                        OrderItem it = getTableView().getItems().get(getIndex());
                        Product p = ServiceFactory.getProductService().getProductById(it.getProductId());
                        if (p == null) return;
                        int q = Math.max(1, it.getQuantity() + delta);
                        it.setQuantity(q);
                        it.setSubtotal(p.getPrice() * q);
                        reload();
                    }
                    private void removeItem() {
                        OrderItem it = getTableView().getItems().get(getIndex());
                        ShopSession.getCartItems().remove(it);
                        reload();
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                };
            }
        });
    }

    private void reload() {
        List<OrderItem> items = ShopSession.getCartItems();
        tableCart.setItems(FXCollections.observableArrayList(items));
        double total = shopService.calculateCartTotal(items);
        lbTotal.setText(String.format("%.2f", total));
    }

    @FXML private void onClear() {
        ShopSession.clearCart();
        reload();
    }

    @FXML private void onCheckout() {
        // 精简版：直接在此调用服务端下单与支付（减少一步弹窗），成功后清空购物车
        java.util.List<OrderItem> items = ShopSession.getCartItems();
        if (items == null || items.isEmpty()) { showWarning("购物车为空"); return; }
        // 组装请求
        String base = ConfigManager.getApiBaseUrl();
        String createUrl = base + "/orders";
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String,Object> body = new java.util.HashMap<>();
            // 由支付页面自动解析过一次，这里复用客户端侧的学号解析逻辑简单化处理：若无则提示
            String studentId = resolveCurrentStudentId();
            if (studentId == null) { showError("未绑定学生信息"); return; }
            body.put("studentId", studentId);
            java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
            for (OrderItem it : items) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("productId", it.getProductId());
                m.put("quantity", it.getQuantity());
                list.add(m);
            }
            body.put("items", list);
            String json = mapper.writeValueAsString(body);

            var client = java.net.http.HttpClient.newHttpClient();
            var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(createUrl))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            var resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) { showError("下单失败: HTTP " + resp.statusCode()); return; }
            var root = mapper.readTree(resp.body());
            if (root.path("code").asInt() != 0) { showError("下单失败: " + root.path("message").asText()); return; }
            String orderId = root.path("data").path("orderId").asText();
            // 支付
            String payUrl = base + "/orders/" + orderId + "/pay";
            var payReq = java.net.http.HttpRequest.newBuilder(java.net.URI.create(payUrl)).POST(java.net.http.HttpRequest.BodyPublishers.noBody()).build();
            var payResp = client.send(payReq, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (payResp.statusCode() != 200) { showError("支付失败: HTTP " + payResp.statusCode()); return; }
            var payRoot = mapper.readTree(payResp.body());
            if (payRoot.path("code").asInt() != 0) { showError("支付失败: " + payRoot.path("message").asText()); return; }
            ShopSession.clearCart();
            reload();
            showSuccess("下单并支付成功，订单: " + orderId);
        } catch (Exception e) {
            e.printStackTrace();
            showError("结算失败: " + e.getMessage());
        }
    }

    private String resolveCurrentStudentId() {
        var u = com.vCampus.common.SessionContext.getCurrentUser();
        if (u == null) return null;
        if (u instanceof com.vCampus.entity.Student s) return s.getStudentId();
        try {
            var s = ServiceFactory.getStudentService().getByUserId(u.getUserId());
            return s == null ? null : s.getStudentId();
        } catch (Exception e) { return null; }
    }
}


