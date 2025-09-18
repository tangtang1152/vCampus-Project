package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Student;
import com.vCampus.entity.Product;
import com.vCampus.service.IShopService;
import com.vCampus.service.ShopServiceImpl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController extends BaseController {
    @FXML private TextField keywordField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> pIdCol;
    @FXML private TableColumn<Product, String> pNameCol;
    @FXML private TableColumn<Product, String> pCategoryCol;
    @FXML private TableColumn<Product, Number> pPriceCol;
    @FXML private TableColumn<Product, Number> pStockCol;

    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> cProductIdCol;
    @FXML private TableColumn<OrderItem, Number> cQtyCol;
    @FXML private TableColumn<OrderItem, Number> cSubtotalCol;
    @FXML private TextField qtyField;
    @FXML private Label totalLabel;

    private final IShopService service = new ShopServiceImpl();
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> cart = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductId()));
        pNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        pCategoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        pPriceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()));
        pStockCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStock()));
        productTable.setItems(products);

        cProductIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductId()));
        cQtyCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        cSubtotalCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()));
        cartTable.setItems(cart);

        refresh();
    }

    private void refresh() {
        List<Product> list;
        String kw = keywordField == null ? "" : keywordField.getText();
        if (kw == null || kw.isBlank()) list = service.getAllProducts();
        else list = service.searchProducts(kw);
        products.setAll(list == null ? List.of() : list);
        updateTotal();
    }

    @FXML private void onSearch() { refresh(); }
    @FXML private void onRefresh() { refresh(); }

    @FXML private void onAddToCart() {
        Product sel = productTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择商品"); return; }
        int qty = 1;
        try { qty = Integer.parseInt(qtyField.getText()); } catch (Exception ignored) {}
        if (qty <= 0) qty = 1;
        if (!service.checkStock(sel.getProductId(), qty)) { showError("库存不足"); return; }
        OrderItem item = new OrderItem();
        item.setProductId(sel.getProductId());
        item.setQuantity(qty);
        item.setSubtotal(sel.getPrice() * qty);
        cart.add(item);
        updateTotal();
    }

    @FXML private void onClearCart() {
        cart.clear();
        updateTotal();
    }

    @FXML private void onPurchase() {
        if (cart.isEmpty()) { showWarning("购物车为空"); return; }
        Integer uid = SessionContext.requireCurrentUserId();
        if (uid == null) { showWarning("请先登录"); return; }
        // 注意：tbl_order.studentId 外键指向的是 tbl_student.studentId
        // 因此需要把当前用户的 userId 映射为学生的 studentId
        var studentService = new com.vCampus.service.StudentServiceImpl();
        Student stu = studentService.getByUserId(uid);
        if (stu == null || stu.getStudentId() == null || stu.getStudentId().isEmpty()) {
            showError("当前账号未关联学生信息，无法下单");
            return;
        }
        List<OrderItem> items = new ArrayList<>(cart);
        String orderId = service.purchase(stu.getStudentId(), items);
        if (orderId != null) { showInformation("下单成功", "订单号：" + orderId); cart.clear(); refresh(); }
        else { showError("下单失败"); }
    }

    @FXML private void onViewOrders() {
        Integer uid = SessionContext.requireCurrentUserId();
        if (uid == null) { showWarning("请先登录"); return; }
        var orders = service.getOrderHistory(String.valueOf(uid));
        showInformation("我的订单", orders == null ? "(空)" : ("共 " + orders.size() + " 笔订单"));
    }

    private void updateTotal() {
        double total = 0.0;
        for (OrderItem i : cart) total += i.getSubtotal();
        if (totalLabel != null) totalLabel.setText(String.valueOf(total));
    }
}


