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
        boolean ok = (editing == null) ? productService.addProduct(p) : productService.updateProduct(p);
        if (ok) { showSuccess("保存成功"); close(); } else { showError("保存失败"); }
    }

    @FXML private void onCancel() { close(); }

    private void close() {
        try { ((javafx.stage.Stage) tfId.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }
}


