package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Product;
import com.vCampus.service.IProductService;
import com.vCampus.service.ProductServiceImpl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopManagementController extends BaseController {
    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, String> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, Number> priceCol;
    @FXML private TableColumn<Product, Number> stockCol;
    @FXML private TableColumn<Product, String> descCol;
    @FXML private TextField keywordField;

    private final IProductService service = new ProductServiceImpl();
    private final ObservableList<Product> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductId()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        priceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()));
        stockCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStock()));
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        table.setItems(data);
        refresh();
    }

    @FXML private void onRefresh() { refresh(); }
    @FXML private void onSearch() { refresh(); }

    private void refresh() {
        String kw = keywordField == null ? "" : keywordField.getText();
        List<Product> list;
        if (kw == null || kw.isBlank()) list = service.getAllProducts();
        else list = service.searchProductsByName(kw);
        data.setAll(list == null ? List.of() : list);
    }

    @FXML private void onAdd() { editDialog(null); }
    @FXML private void onEdit() { editDialog(table.getSelectionModel().getSelectedItem()); }

    @FXML private void onDelete() {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要删除的商品"); return; }
        if (!showConfirmation("删除商品", "确认删除《" + sel.getProductName() + "》?")) return;
        boolean ok = service.deleteProduct(sel.getProductId());
        if (ok) { showInformation("提示", "删除成功"); refresh(); } else { showError("删除失败"); }
    }

    @FXML private void onInc() { adjustStock(+1); }
    @FXML private void onDec() { adjustStock(-1); }

    private void adjustStock(int delta) {
        Product sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择商品"); return; }
        boolean ok = service.updateProductStock(sel.getProductId(), delta);
        if (ok) { showInformation("提示", "库存已更新"); refresh(); } else { showError("库存更新失败"); }
    }

    private void editDialog(Product origin) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(origin == null ? "新增商品" : "编辑商品");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(10));

        TextField id = new TextField(origin == null ? "" : origin.getProductId());
        TextField name = new TextField(origin == null ? "" : origin.getProductName());
        TextField category = new TextField(origin == null ? "" : origin.getCategory());
        TextField price = new TextField(origin == null ? "0" : String.valueOf(origin.getPrice()));
        TextField stock = new TextField(origin == null ? "0" : String.valueOf(origin.getStock()));
        TextArea desc = new TextArea(origin == null ? "" : origin.getDescription());
        desc.setPrefRowCount(3);

        grid.addRow(0, new Label("商品ID"), id);
        grid.addRow(1, new Label("名称"), name);
        grid.addRow(2, new Label("分类"), category);
        grid.addRow(3, new Label("价格"), price);
        grid.addRow(4, new Label("库存"), stock);
        grid.addRow(5, new Label("描述"), desc);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Product p = origin == null ? new Product() : origin;
                p.setProductId(id.getText());
                p.setProductName(name.getText());
                p.setCategory(category.getText());
                try { p.setPrice(Double.parseDouble(price.getText())); } catch (Exception e) { p.setPrice(0.0); }
                try { p.setStock(Integer.parseInt(stock.getText())); } catch (Exception e) { p.setStock(0); }
                p.setDescription(desc.getText());
                boolean ok = origin == null ? service.addProduct(p) : service.updateProduct(p);
                if (ok) { showInformation("提示", origin==null?"新增成功":"保存成功"); refresh(); } else { showError("保存失败"); }
            }
        });
    }
}
