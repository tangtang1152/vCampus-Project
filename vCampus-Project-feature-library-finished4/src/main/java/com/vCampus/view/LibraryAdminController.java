package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Book;
import client.net.SocketClient;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LibraryAdminController extends BaseController {
    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, Number> idCol;
    @FXML private TableColumn<Book, String> isbnCol;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> authorCol;
    @FXML private TableColumn<Book, String> categoryCol;
    @FXML private TableColumn<Book, String> publisherCol;
    @FXML private TableColumn<Book, String> pubDateCol;
    @FXML private TableColumn<Book, Number> totalCol;
    @FXML private TableColumn<Book, Number> availableCol;
    @FXML private TableColumn<Book, String> locationCol;
    @FXML private TableColumn<Book, String> statusCol;
    @FXML private ComboBox<String> statusBox;
    @FXML private TextField keywordField;

    // 移除直接依赖 LibraryService，改为通过 SocketClient 通信
    private final ObservableList<Book> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 权限：基于 activeRole 的显隐与校验
        var user = SessionContext.getCurrentUser();
        boolean canMaintain = com.vCampus.util.RBACUtil.canMaintainLibrary(user);
        if (!canMaintain) { showError("无权限访问图书维护页"); return; }

        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBookId() == null ? 0 : c.getValue().getBookId()));
        isbnCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        authorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        publisherCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPublisher()));
        pubDateCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPubDate())));
        totalCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTotalCopies() == null ? 0 : c.getValue().getTotalCopies()));
        availableCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAvailableCopies() == null ? 0 : c.getValue().getAvailableCopies()));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        table.setItems(data);
        refresh();
    }

    @FXML private void onRefresh() { refresh(); }
    @FXML private void onSearch() { refresh(); }
    @FXML private void onImportCsv() { showInformation("导入CSV", "占位：可在此实现文件选择并批量导入"); }
    @FXML private void onExportCsv() { showInformation("导出CSV", "占位：可在此实现导出当前列表为CSV"); }

    private void refresh() {
        try {
            String kw = keywordField == null ? "" : keywordField.getText();
            String request = "SEARCH_BOOKS:" + kw + ":全部:默认(最新):1:200";
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:BOOKS:")) {
                List<Book> list = parseBooksFromResponse(response);
                data.setAll(list);
            } else {
                showError("获取图书列表失败: " + response);
                data.clear();
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onAdd() { editDialog(null); }
    @FXML private void onEdit() { editDialog(table.getSelectionModel().getSelectedItem()); }

    @FXML private void onDelete() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要删除的图书"); return; }
        if (!showConfirmation("删除图书", "确认删除《" + sel.getTitle() + "》?")) return;
        
        try {
            String request = "DELETE_BOOK:" + sel.getBookId();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:DELETE:")) {
                showInformation("提示", "删除成功");
                refresh();
            } else {
                showError("删除失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onInc() { adjustStock(1); }
    @FXML private void onDec() { adjustStock(-1); }

    private void adjustStock(int delta) {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        
        try {
            String request = delta > 0 ? "INCREASE_STOCK:" + sel.getBookId() + ":" + delta : "DECREASE_STOCK:" + sel.getBookId() + ":" + (-delta);
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:STOCK:")) {
                showInformation("提示", "库存已更新");
                refresh();
            } else {
                showError("库存更新失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onSetStatus() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        String st = statusBox.getValue();
        if (st == null || st.isBlank()) { showWarning("请选择状态"); return; }
        
        try {
            String request = "SET_BOOK_STATUS:" + sel.getBookId() + ":" + st;
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:STATUS:")) {
                showInformation("提示", "状态已更新");
                refresh();
            } else {
                showError("状态更新失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editDialog(Book origin) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(origin == null ? "新增图书" : "编辑图书");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(10));

        TextField isbn = new TextField(origin == null ? "" : origin.getIsbn());
        TextField title = new TextField(origin == null ? "" : origin.getTitle());
        TextField author = new TextField(origin == null ? "" : origin.getAuthor());
        TextField category = new TextField(origin == null ? "" : origin.getCategory());
        TextField publisher = new TextField(origin == null ? "" : origin.getPublisher());
        DatePicker pubDate = new DatePicker(origin == null || origin.getPubDate()==null ? null : origin.getPubDate().toLocalDate());
        Spinner<Integer> total = new Spinner<>(0, 100000, origin == null || origin.getTotalCopies()==null ? 0 : origin.getTotalCopies());
        Spinner<Integer> avail = new Spinner<>(0, 100000, origin == null || origin.getAvailableCopies()==null ? 0 : origin.getAvailableCopies());
        TextField location = new TextField(origin == null ? "" : origin.getLocation());
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("正常", "下架"));
        status.setValue(origin == null ? "正常" : origin.getStatus());

        grid.addRow(0, new Label("ISBN"), isbn);
        grid.addRow(1, new Label("书名"), title);
        grid.addRow(2, new Label("作者"), author);
        grid.addRow(3, new Label("分类"), category);
        grid.addRow(4, new Label("出版社"), publisher);
        grid.addRow(5, new Label("出版日"), pubDate);
        grid.addRow(6, new Label("总数"), total);
        grid.addRow(7, new Label("可借"), avail);
        grid.addRow(8, new Label("位置"), location);
        grid.addRow(9, new Label("状态"), status);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Book b = origin == null ? new Book() : origin;
                b.setIsbn(isbn.getText());
                b.setTitle(title.getText());
                b.setAuthor(author.getText());
                b.setCategory(category.getText());
                b.setPublisher(publisher.getText());
                b.setPubDate(pubDate.getValue()==null?null: java.sql.Date.valueOf(pubDate.getValue()));
                b.setTotalCopies(total.getValue());
                b.setAvailableCopies(avail.getValue());
                b.setLocation(location.getText());
                b.setStatus(status.getValue());
                try {
                    String request;
                    if (origin == null) {
                        // 新增图书
                        request = "ADD_BOOK:" + b.getIsbn() + "," + b.getTitle() + "," + b.getAuthor() + "," + 
                                b.getCategory() + "," + b.getPublisher() + "," + b.getPubDate() + "," + 
                                b.getTotalCopies() + "," + b.getAvailableCopies() + "," + b.getLocation() + "," + b.getStatus();
                    } else {
                        // 更新图书
                        request = "UPDATE_BOOK:" + b.getBookId() + "," + b.getIsbn() + "," + b.getTitle() + "," + b.getAuthor() + "," + 
                                b.getCategory() + "," + b.getPublisher() + "," + b.getPubDate() + "," + 
                                b.getTotalCopies() + "," + b.getAvailableCopies() + "," + b.getLocation() + "," + b.getStatus();
                    }
                    String response = SocketClient.sendRequest(request);
                    if (response != null && (response.startsWith("SUCCESS:ADD:") || response.startsWith("SUCCESS:UPDATE:"))) {
                        showInformation("提示", origin==null?"新增成功":"保存成功");
                        refresh();
                    } else {
                        showError("保存失败: " + response);
                    }
                } catch (Exception e) {
                    showError("网络连接失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 从服务器响应解析图书列表
     */
    private List<Book> parseBooksFromResponse(String response) {
        List<Book> books = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:BOOKS:")) {
                String data = response.substring("SUCCESS:BOOKS:".length());
                if (!data.isEmpty()) {
                    String[] bookStrings = data.split("\\|");
                    for (String bookString : bookStrings) {
                        String[] fields = bookString.split(",");
                        if (fields.length >= 7) {
                            Book book = new Book();
                            book.setBookId(Integer.parseInt(fields[0]));
                            book.setTitle(fields[1]);
                            book.setAuthor(fields[2]);
                            book.setIsbn(fields[3]);
                            book.setAvailableCopies(Integer.parseInt(fields[4]));
                            book.setTotalCopies(Integer.parseInt(fields[5]));
                            book.setStatus(fields[6]);
                            if (fields.length > 7) {
                                book.setCategory(fields[7]);
                            }
                            if (fields.length > 8) {
                                book.setPublisher(fields[8]);
                            }
                            if (fields.length > 9) {
                                book.setLocation(fields[9]);
                            }
                            books.add(book);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析图书数据异常: " + e.getMessage());
        }
        return books;
    }
}


