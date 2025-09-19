package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
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
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shop-pay-view.fxml"));
            javafx.scene.Parent root = loader.load();
            var stage = new javafx.stage.Stage();
            stage.setTitle("支付");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setWidth(480); stage.setHeight(360);
            stage.showAndWait();
            reload();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开支付失败: " + e.getMessage());
        }
    }
}


