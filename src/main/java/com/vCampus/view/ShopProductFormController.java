package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Product;
import com.vCampus.service.IProductService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ShopProductFormController extends BaseController {
    @FXML private TextField tfId;
    @FXML private TextField tfName;
    @FXML private TextField tfPrice;
    @FXML private TextField tfStock;
    @FXML private TextField tfCategory;
    @FXML private TextArea taDesc;

    private final IProductService productService = ServiceFactory.getProductService();
    private Product editing;

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public void setEditing(Product p) {
        this.editing = p;
        if (p != null) {
            tfId.setText(p.getProductId()); tfId.setDisable(true);
            tfName.setText(p.getProductName());
            tfPrice.setText(String.valueOf(p.getPrice()));
            tfStock.setText(String.valueOf(p.getStock()));
            tfCategory.setText(p.getCategory());
            taDesc.setText(p.getDescription());
        }
    }

    @FXML private void onSave() {
        Product p = editing == null ? new Product() : editing;
        p.setProductId(tfId.getText());
        p.setProductName(tfName.getText());
        try { p.setPrice(Double.parseDouble(tfPrice.getText())); } catch (Exception e) { p.setPrice(0.0); }
        try { p.setStock(Integer.parseInt(tfStock.getText())); } catch (Exception e) { p.setStock(0); }
        p.setCategory(tfCategory.getText());
        p.setDescription(taDesc.getText());
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            java.util.Map<String,String> pm = new java.util.HashMap<>();
            pm.put("productId", p.getProductId());
            pm.put("productName", p.getProductName());
            pm.put("price", String.valueOf(p.getPrice()==null?0.0:p.getPrice()));
            pm.put("stock", String.valueOf(p.getStock()==null?0:p.getStock()));
            pm.put("category", p.getCategory());
            pm.put("description", p.getDescription());
            String action = (editing == null) ? "SHOP_ADD" : "SHOP_UPDATE";
            try {
                var req = new com.vCampus.net.dto.SocketRequest(action, pm);
                java.net.Socket s = new java.net.Socket();
                s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
                s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                     java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                    out.writeObject(req); out.flush();
                    Object obj = in.readObject();
                    ok = (obj instanceof com.vCampus.net.dto.SocketResponse resp) && resp.isSuccess();
                } finally { s.close(); }
            } catch (Exception e) {
                ok = false;
            }
        } else {
            ok = (editing == null) ? productService.addProduct(p) : productService.updateProduct(p);
        }
        if (ok) { showSuccess("保存成功"); close(); } else { showError("保存失败"); }
    }

    @FXML private void onCancel() { close(); }

    private void close() {
        try { ((javafx.stage.Stage) tfId.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }
}


