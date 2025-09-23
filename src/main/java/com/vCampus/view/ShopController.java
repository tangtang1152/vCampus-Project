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
    private javafx.animation.Timeline autoRefresh;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        addActionButtons();
        loadCategories();
        if (ConfigManager.isSocketEnabled()) {
            loadProducts(null, null);
            startAutoRefresh();
        } else {
            tryLoadProductsFromHttp();
        }
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

    private final java.util.concurrent.atomic.AtomicBoolean loading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private void loadProducts(String category, String keyword) {
        if (!loading.compareAndSet(false, true)) return;
        List<Product> products;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            startDaemon(() -> {
                var client = com.vCampus.net.ShopSocketClient.fromConfig();
                var rows = client.listProducts(category, keyword);
                List<Product> list = new java.util.ArrayList<>();
                java.util.Set<String> cats = new java.util.HashSet<>();
                for (var m : rows) {
                    Product p = new Product();
                    p.setProductId(String.valueOf(m.get("productId")));
                    p.setProductName(String.valueOf(m.get("productName")));
                    Object pr = m.get("price"); if (pr != null) p.setPrice(((Number)pr).doubleValue());
                    Object st = m.get("stock"); if (st != null) p.setStock(((Number)st).intValue());
                    String cat = String.valueOf(m.get("category"));
                    p.setCategory(cat);
                    p.setDescription(String.valueOf(m.get("description")));
                    list.add(p);
                    if (cat != null && !cat.isBlank()) cats.add(cat);
                }
                javafx.application.Platform.runLater(() -> {
                    tableProducts.setItems(FXCollections.observableArrayList(list));
                    if (cbCategory != null) {
                        String cur = cbCategory.getValue();
                        var newCats = FXCollections.observableArrayList(cats);
                        newCats.sort(String::compareTo);
                        newCats.add(0, "全部");
                        cbCategory.setItems(newCats);
                        if (cur == null || !newCats.contains(cur)) cbCategory.setValue("全部");
                        if (rows.isEmpty()) cbCategory.setPromptText("服务器不可用");
                    }
                    loading.set(false);
                });
            }, "shop-load");
            return;
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = shopService.searchProducts(keyword.trim());
        } else if (category != null && !category.equals("全部")) {
            products = shopService.getProductsByCategory(category);
        } else {
            products = shopService.getAllProducts();
        }
        tableProducts.setItems(FXCollections.observableArrayList(products));
        loading.set(false);
    }

    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(8), e -> loadProducts(cbCategory.getValue(), tfKeyword.getText()))
        );
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();
        tableProducts.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, ov, nv) -> {
                    if (nv != null) nv.setOnHidden(evt -> { if (autoRefresh != null) autoRefresh.stop(); });
                });
            } else {
                if (autoRefresh != null) autoRefresh.stop();
            }
        });
    }

    @Override
    public void onUnload() {
        if (autoRefresh != null) autoRefresh.stop();
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


