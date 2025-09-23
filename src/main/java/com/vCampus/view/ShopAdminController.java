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
    private javafx.animation.Timeline autoRefresh;

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
        startAutoRefresh();
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
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            new Thread(() -> {
                try {
                    java.util.Map<String,String> params = new java.util.HashMap<>();
                    if (cat != null && !"全部".equals(cat)) params.put("category", cat);
                    if (kw != null && !kw.isBlank()) params.put("keyword", kw.trim());
                    var req = new com.vCampus.net.dto.SocketRequest("SHOP_LIST", params);
                    java.net.Socket s = new java.net.Socket();
                    s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
                    s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
                    java.util.List<Product> list = null;
                    try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                         java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                        out.writeObject(req); out.flush();
                        Object obj = in.readObject();
                        if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m && m.get("rows") instanceof java.util.List<?> rows) {
                            list = new java.util.ArrayList<>();
                            for (Object r : rows) {
                                if (r instanceof java.util.Map<?,?> rm) {
                                    Product p = new Product();
                                    p.setProductId(String.valueOf(rm.get("productId")));
                                    p.setProductName(String.valueOf(rm.get("productName")));
                                    Object pr = rm.get("price"); if (pr != null) p.setPrice(((Number)pr).doubleValue());
                                    Object st = rm.get("stock"); if (st != null) p.setStock(((Number)st).intValue());
                                    p.setCategory(String.valueOf(rm.get("category")));
                                    p.setDescription(String.valueOf(rm.get("description")));
                                    list.add(p);
                                }
                            }
                        }
                    } finally { s.close(); }
                    final java.util.List<Product> flist = list;
                    javafx.application.Platform.runLater(() -> {
                        if (flist == null) {
                            // 非阻塞提示
                            if (cbCategory != null) cbCategory.setPromptText("服务器不可用");
                        } else {
                            table.setItems(FXCollections.observableArrayList(flist));
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        if (cbCategory != null) cbCategory.setPromptText("服务器不可用");
                    });
                }
            }, "shopadmin-reload").start();
            return;
        }
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
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            ok = sendProductOp("SHOP_DELETE", p);
        } else {
            ok = productService.deleteProduct(p.getProductId());
        }
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
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            ok = sendProductOp("SHOP_UPDATE", p);
        } else {
            ok = productService.updateProduct(p);
        }
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

    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(8), e -> reload(cbCategory.getValue(), tfKeyword.getText()))
        );
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, ov, nv) -> {
                    if (nv != null) nv.setOnHidden(evt -> { if (autoRefresh != null) autoRefresh.stop(); });
                });
            }
        });
    }

    @Override
    public void onUnload() {
        if (autoRefresh != null) autoRefresh.stop();
    }

    private boolean sendProductOp(String action, Product p) {
        try {
            java.util.Map<String,String> pm = new java.util.HashMap<>();
            if (p != null) {
                if (p.getProductId() != null) pm.put("productId", p.getProductId());
                if (p.getProductName() != null) pm.put("productName", p.getProductName());
                pm.put("price", String.valueOf(p.getPrice() == null ? 0.0 : p.getPrice()));
                pm.put("stock", String.valueOf(p.getStock() == null ? 0 : p.getStock()));
                if (p.getCategory() != null) pm.put("category", p.getCategory());
                if (p.getDescription() != null) pm.put("description", p.getDescription());
            }
            var req = new com.vCampus.net.dto.SocketRequest(action, pm);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                if (obj instanceof com.vCampus.net.dto.SocketResponse resp) return resp.isSuccess();
            } finally { s.close(); }
        } catch (Exception e) { }
        return false;
    }
}


