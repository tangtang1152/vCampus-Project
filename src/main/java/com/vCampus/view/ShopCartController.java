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
                        // 直接刷新表格与合计，避免依赖重建列表导致列未即时渲染
                        double total = ShopCartController.this.shopService.calculateCartTotal(getTableView().getItems());
                        ShopCartController.this.lbTotal.setText(String.format("%.2f", total));
                        getTableView().refresh();
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
        java.util.List<OrderItem> items = ShopSession.getCartItems();
        if (items == null || items.isEmpty()) { showWarning("购物车为空"); return; }
        String studentId = resolveCurrentStudentId();
        if (studentId == null) { showError("未绑定学生信息"); return; }

        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            try {
                var client = com.vCampus.net.ShopSocketClient.fromConfig();
                java.util.List<com.vCampus.net.dto.ShopDtos.OrderItemDTO> list = new java.util.ArrayList<>();
                for (OrderItem it : items) {
                    com.vCampus.net.dto.ShopDtos.OrderItemDTO dto = new com.vCampus.net.dto.ShopDtos.OrderItemDTO();
                    dto.productId = it.getProductId();
                    dto.quantity = it.getQuantity();
                    list.add(dto);
                }
                var resp = client.createOrder(studentId, list);
                if (resp == null || resp.orderId == null) { showError("下单失败"); return; }
                boolean ok = client.payOrder(resp.orderId);
                if (!ok) { showError("支付失败"); return; }
                ShopSession.clearCart();
                reload();
                showSuccess("下单并支付成功，订单: " + resp.orderId);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showError("结算失败: " + e.getMessage());
                return;
            }
        }

        // 回退 HTTP
        // ... 保留原 HTTP 逻辑
        java.util.List<OrderItem> httpItems = ShopSession.getCartItems();
        if (httpItems == null || httpItems.isEmpty()) { showWarning("购物车为空"); return; }
        String base = ConfigManager.getApiBaseUrl();
        String createUrl = base + "/orders";
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String,Object> body = new java.util.HashMap<>();
            body.put("studentId", studentId);
            java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
            for (OrderItem it : httpItems) {
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
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            try {
                var req = new com.vCampus.net.dto.SocketRequest("STUDENT_BY_USER").put("userId", String.valueOf(u.getUserId()));
                java.net.Socket s = new java.net.Socket();
                s.connect(new java.net.InetSocketAddress(
                        com.vCampus.common.ConfigManager.getSocketServerHost(),
                        com.vCampus.common.ConfigManager.getSocketServerPort()),
                        com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
                s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                     java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                    out.writeObject(req); out.flush();
                    Object obj = in.readObject();
                    if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m) {
                        Object sid = m.get("studentId");
                        if (sid != null) return String.valueOf(sid);
                    }
                } finally { s.close(); }
            } catch (Exception ignored) {}
        }
        try {
            var s = ServiceFactory.getStudentService().getByUserId(u.getUserId());
            return s == null ? null : s.getStudentId();
        } catch (Exception e) { return null; }
    }
}


