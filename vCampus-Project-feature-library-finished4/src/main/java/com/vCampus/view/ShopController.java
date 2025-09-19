package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Product;
import client.net.SocketClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController extends BaseController {

    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField tfKeyword;
    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Void> colAction;

    // 移除直接依赖 IShopService，改为通过 SocketClient 通信

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        addActionButtons();
        loadCategories();
        loadProducts(null, null);
    }

    private void addActionButtons() {
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Product, Void> call(TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("加入购物车");
                    {
                        btn.setOnAction(e -> {
                            Product p = getTableView().getItems().get(getIndex());
                            addToCart(p);
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
    }

    private void loadCategories() {
        try {
            // 构建获取分类请求
            String request = "GET_ALL_CATEGORIES";
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:CATEGORIES:")) {
                List<String> cats = parseCategoriesFromResponse(response);
                ObservableList<String> list = FXCollections.observableArrayList(cats);
                list.add(0, "全部");
                cbCategory.setItems(list);
                cbCategory.setValue("全部");
                cbCategory.setOnAction(e -> onSearch());
            } else {
                showError("获取分类失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts(String category, String keyword) {
        try {
            String request;
            if (keyword != null && !keyword.trim().isEmpty()) {
                request = "SEARCH_PRODUCTS:" + keyword.trim();
            } else if (category != null && !category.equals("全部")) {
                request = "GET_PRODUCTS_BY_CATEGORY:" + category;
            } else {
                request = "GET_ALL_PRODUCTS";
            }
            
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:PRODUCTS:")) {
                List<Product> products = parseProductsFromResponse(response);
                tableProducts.setItems(FXCollections.observableArrayList(products));
            } else {
                showError("获取商品失败: " + response);
                tableProducts.getItems().clear();
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        String cat = cbCategory.getValue();
        String kw = tfKeyword.getText();
        loadProducts(cat, kw);
    }

    @FXML
    private void onOpenCart() {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shop-cart-view.fxml"));
            javafx.scene.Parent root = loader.load();
            var stage = new javafx.stage.Stage();
            stage.setTitle("购物车");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setWidth(640); stage.setHeight(520);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开购物车失败: " + e.getMessage());
        }
    }

    private void addToCart(Product p) {
        if (p == null) return;
        OrderItem exist = null;
        for (OrderItem it : ShopSession.getCartItems()) {
            if (p.getProductId().equals(it.getProductId())) { exist = it; break; }
        }
        if (exist == null) {
            OrderItem it = new OrderItem();
            it.setProductId(p.getProductId());
            it.setQuantity(1);
            it.setSubtotal(p.getPrice());
            ShopSession.getCartItems().add(it);
        } else {
            exist.setQuantity(exist.getQuantity() + 1);
            exist.setSubtotal(exist.getSubtotal() + p.getPrice());
        }
        showSuccess("已加入购物车");
    }

    /**
     * 从服务器响应解析分类列表
     */
    private List<String> parseCategoriesFromResponse(String response) {
        List<String> categories = new java.util.ArrayList<>();
        try {
            String data = response.substring("SUCCESS:CATEGORIES:".length());
            if (!data.isEmpty()) {
                String[] catArray = data.split(",");
                for (String category : catArray) {
                    if (!category.trim().isEmpty()) {
                        categories.add(category.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析分类数据异常: " + e.getMessage());
        }
        return categories;
    }

    /**
     * 从服务器响应解析商品列表
     */
    private List<Product> parseProductsFromResponse(String response) {
        List<Product> products = new java.util.ArrayList<>();
        try {
            String data = response.substring("SUCCESS:PRODUCTS:".length());
            if (data.isEmpty()) return products;
            
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
        } catch (Exception e) {
            System.err.println("解析商品数据异常: " + e.getMessage());
        }
        return products;
    }
}


