package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.ShopSession;
import com.vCampus.common.ConfigManager;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Product;
import com.vCampus.service.IShopService;
import com.vCampus.service.ServiceFactory;
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

    private final IShopService shopService = ServiceFactory.getShopService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        addActionButtons();
        loadCategories();
        tryLoadProductsFromHttp();
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
        List<String> cats = shopService.getAllCategories();
        ObservableList<String> list = FXCollections.observableArrayList(cats);
        list.add(0, "全部");
        cbCategory.setItems(list);
        cbCategory.setValue("全部");
        cbCategory.setOnAction(e -> onSearch());
    }

    private void loadProducts(String category, String keyword) {
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = shopService.searchProducts(keyword.trim());
        } else if (category != null && !category.equals("全部")) {
            products = shopService.getProductsByCategory(category);
        } else {
            products = shopService.getAllProducts();
        }
        tableProducts.setItems(FXCollections.observableArrayList(products));
    }

    private void tryLoadProductsFromHttp() {
        // 精简版：若服务端可用，则直接用 HTTP 方式加载；失败则回退本地 Service
        String base = ConfigManager.getApiBaseUrl();
        String url = base + "/products";
        javafx.concurrent.Task<javafx.collections.ObservableList<Product>> task = new javafx.concurrent.Task<>() {
            @Override protected javafx.collections.ObservableList<Product> call() {
                try {
                    var client = java.net.http.HttpClient.newHttpClient();
                    var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).GET().build();
                    var resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                    if (resp.statusCode() == 200) {
                        // 期待响应：{code:0,data:[{productId,...}]}
                        var json = resp.body();
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
                        if (root.has("code") && root.get("code").asInt() == 0) {
                            java.util.List<Product> list = new java.util.ArrayList<>();
                            for (var n : root.withArray("data")) {
                                Product p = new Product();
                                p.setProductId(n.path("productId").asText());
                                p.setProductName(n.path("productName").asText());
                                p.setPrice(n.path("price").asDouble());
                                p.setStock(n.path("stock").asInt());
                                p.setCategory(n.path("category").asText());
                                p.setDescription(n.path("description").asText());
                                list.add(p);
                            }
                            return FXCollections.observableArrayList(list);
                        }
                    }
                } catch (Exception ignored) {}
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            var data = task.getValue();
            if (data != null) {
                tableProducts.setItems(data);
            } else {
                // 回退本地
                loadProducts(null, null);
            }
        });
        new Thread(task, "load-products-http").start();
    }

    @FXML
    private void onPing() {
        String base = ConfigManager.getApiBaseUrl();
        String url = base + "/products";
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override protected String call() {
                long t0 = System.currentTimeMillis();
                try {
                    var client = java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(3)).build();
                    var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).timeout(java.time.Duration.ofSeconds(5)).GET().build();
                    var resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
                    long ms = System.currentTimeMillis() - t0;
                    return resp.statusCode() == 200 ? ("OK " + ms + " ms") : ("HTTP " + resp.statusCode());
                } catch (Exception e) {
                    long ms = System.currentTimeMillis() - t0;
                    return "FAIL (" + ms + " ms): " + e.getClass().getSimpleName();
                }
            }
        };
        task.setOnSucceeded(e -> showInformation("连通测试", "GET /products => " + task.getValue() + "\n服务器: " + base));
        new Thread(task, "shop-ping").start();
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
}


