package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Product;
import client.net.SocketClient;
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

    // 移除直接依赖服务，改为通过 SocketClient 通信
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
        try {
            Product p = editing == null ? new Product() : editing;
            p.setProductId(tfId.getText());
            p.setProductName(tfName.getText());
            try { p.setPrice(Double.parseDouble(tfPrice.getText())); } catch (Exception e) { p.setPrice(0.0); }
            try { p.setStock(Integer.parseInt(tfStock.getText())); } catch (Exception e) { p.setStock(0); }
            p.setCategory(tfCategory.getText());
            p.setDescription(taDesc.getText());
            
            boolean ok = (editing == null) ? addProduct(p) : updateProduct(p);
            if (ok) { 
                showSuccess("保存成功"); 
                close(); 
            } else { 
                showError("保存失败"); 
            }
        } catch (Exception e) {
            showError("保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onCancel() { close(); }

    private void close() {
        try { ((javafx.stage.Stage) tfId.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }

    /**
     * 添加商品
     */
    private boolean addProduct(Product product) {
        try {
            String request = "ADD_PRODUCT:" + product.getProductName() + "," + 
                           product.getPrice() + "," + product.getStock() + "," + 
                           product.getCategory() + "," + product.getDescription();
            String response = SocketClient.sendRequest(request);
            return response != null && response.startsWith("SUCCESS:ADD:");
        } catch (Exception e) {
            System.err.println("添加商品失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新商品
     */
    private boolean updateProduct(Product product) {
        try {
            String request = "UPDATE_PRODUCT:" + product.getProductId() + "," + 
                           product.getProductName() + "," + product.getPrice() + "," + 
                           product.getStock() + "," + product.getCategory() + "," + 
                           product.getDescription();
            String response = SocketClient.sendRequest(request);
            return response != null && response.startsWith("SUCCESS:UPDATE:");
        } catch (Exception e) {
            System.err.println("更新商品失败: " + e.getMessage());
            return false;
        }
    }
}
