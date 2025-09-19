package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Product;
import client.net.SocketClient;
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

    // 移除直接依赖服务，改为通过 SocketClient 通信

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
        try {
            String response = SocketClient.sendRequest("GET_ALL_CATEGORIES");
            if (response != null && response.startsWith("SUCCESS:CATEGORIES:")) {
                List<String> cats = parseCategoriesFromResponse(response);
                var list = FXCollections.observableArrayList(cats);
                list.add(0, "全部");
                cbCategory.setItems(list);
                cbCategory.setValue("全部");
                cbCategory.setOnAction(e -> onSearch());
            }
        } catch (Exception e) {
            showError("加载分类失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reload(String cat, String kw) {
        try {
            String request;
            if (kw != null && !kw.isBlank()) {
                request = "SEARCH_PRODUCTS:" + kw.trim();
            } else if (cat != null && !"全部".equals(cat)) {
                request = "GET_PRODUCTS_BY_CATEGORY:" + cat;
            } else {
                request = "GET_ALL_PRODUCTS";
            }
            
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:PRODUCTS:")) {
                List<Product> data = parseProductsFromResponse(response);
                table.setItems(FXCollections.observableArrayList(data));
            } else {
                showError("获取商品列表失败: " + response);
                table.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            String request = "DELETE_PRODUCT:" + p.getProductId();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:DELETE:")) {
                showSuccess("删除成功");
                onSearch();
            } else {
                showError("删除失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            String request = "UPDATE_PRODUCT:" + p.getProductId() + "," + p.getProductName() + "," + p.getPrice() + "," + 
                           p.getStock() + "," + p.getCategory() + "," + p.getDescription();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:UPDATE:")) {
                showSuccess("已更新");
                onSearch();
            } else {
                showError("更新失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
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

    /**
     * 从服务器响应解析商品列表
     */
    private List<Product> parseProductsFromResponse(String response) {
        List<Product> products = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:PRODUCTS:")) {
                String data = response.substring("SUCCESS:PRODUCTS:".length());
                if (!data.isEmpty()) {
                    String[] productStrings = data.split("\\|");
                    for (String productString : productStrings) {
                        String[] fields = productString.split(",");
                        if (fields.length >= 6) {
                            Product product = new Product();
                            product.setProductId(fields[0]);
                            product.setProductName(fields[1]);
                            product.setPrice(Double.parseDouble(fields[2]));
                            product.setStock(Integer.parseInt(fields[3]));
                            product.setCategory(fields[4]);
                            product.setDescription(fields[5]);
                            products.add(product);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析商品数据异常: " + e.getMessage());
        }
        return products;
    }

    /**
     * 从服务器响应解析分类列表
     */
    private List<String> parseCategoriesFromResponse(String response) {
        List<String> categories = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:CATEGORIES:")) {
                String data = response.substring("SUCCESS:CATEGORIES:".length());
                if (!data.isEmpty()) {
                    String[] categoryStrings = data.split("\\|");
                    for (String category : categoryStrings) {
                        if (!category.trim().isEmpty()) {
                            categories.add(category.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析分类数据异常: " + e.getMessage());
        }
        return categories;
    }
}
