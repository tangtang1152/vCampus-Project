package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Product;
import com.vCampus.service.IProductService;
import com.vCampus.service.IShopService;
import com.vCampus.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopAdminController extends BaseController {

    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField tfKeyword;
    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colDesc;

    private final IProductService productService = ServiceFactory.getProductService();
    private final IShopService shopService = ServiceFactory.getShopService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        loadCategories();
        reload(null, null);
    }

    private void loadCategories() {
        var cats = shopService.getAllCategories();
        var list = FXCollections.observableArrayList(cats);
        list.add(0, "全部");
        cbCategory.setItems(list);
        cbCategory.setValue("全部");
        cbCategory.setOnAction(e -> onSearch());
    }

    private void reload(String cat, String kw) {
        List<Product> data;
        if (kw != null && !kw.isBlank()) data = productService.searchProductsByName(kw.trim());
        else if (cat != null && !"全部".equals(cat)) data = productService.getProductsByCategory(cat);
        else data = productService.getAllProducts();
        table.setItems(FXCollections.observableArrayList(data));
    }

    @FXML private void onSearch() {
        reload(cbCategory.getValue(), tfKeyword.getText());
    }

    @FXML private void onAdd() {
        openForm(null, "新增商品");
    }

    @FXML private void onEdit() {
        var p = table.getSelectionModel().getSelectedItem();
        if (p == null) { showWarning("请选择商品"); return; }
        openForm(p, "编辑商品");
    }

    @FXML private void onDelete() {
        var p = table.getSelectionModel().getSelectedItem();
        if (p == null) { showWarning("请选择商品"); return; }
        if (!showConfirmation("删除确认", "确定删除商品: " + p.getProductName() + " ?")) return;
        boolean ok = productService.deleteProduct(p.getProductId());
        if (ok) { showSuccess("删除成功"); onSearch(); } else { showError("删除失败"); }
    }

    @FXML private void onAdjust() {
        var p = table.getSelectionModel().getSelectedItem();
        if (p == null) { showWarning("请选择商品"); return; }
        TextInputDialog d1 = new TextInputDialog(String.valueOf(p.getPrice()));
        d1.setHeaderText("设置价格");
        var r1 = d1.showAndWait();
        r1.ifPresent(priceStr -> {
            try { p.setPrice(Double.parseDouble(priceStr)); } catch (Exception ignored) {}
        });
        TextInputDialog d2 = new TextInputDialog(String.valueOf(p.getStock()));
        d2.setHeaderText("设置库存");
        var r2 = d2.showAndWait();
        r2.ifPresent(stockStr -> {
            try { p.setStock(Integer.parseInt(stockStr)); } catch (Exception ignored) {}
        });
        boolean ok = productService.updateProduct(p);
        if (ok) { showSuccess("已更新"); onSearch(); } else { showError("更新失败"); }
    }

    private void openForm(Product editing, String title) {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shop-product-form.fxml"));
            javafx.scene.Parent root = loader.load();
            var ctrl = (ShopProductFormController) loader.getController();
            if (editing != null) ctrl.setEditing(editing);
            var stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setWidth(520); stage.setHeight(480);
            stage.showAndWait();
            onSearch();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开表单失败: " + e.getMessage());
        }
    }
}


