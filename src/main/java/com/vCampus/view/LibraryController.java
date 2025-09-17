package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Book;
import com.vCampus.service.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LibraryController extends BaseController {
    @FXML private TextField keywordField;
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> authorCol;
    @FXML private TableColumn<Book, String> isbnCol;
    @FXML private TableColumn<Book, Number> availableCol;
    // 我的借阅
    @FXML private TableView<com.vCampus.entity.BorrowRecord> borrowTable;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brTitleCol;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brBorrowDateCol;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brDueCol;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brStatusCol;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brFineCol;
    @FXML private Label infoLabel;
    @FXML private Pagination pagination;

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<Book> data = FXCollections.observableArrayList();
    private int page = 1;
    private final int pageSize = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        authorCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAuthor()));
        isbnCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIsbn()));
        availableCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getAvailableCopies() == null ? 0 : c.getValue().getAvailableCopies()));
        bookTable.setItems(data);
        // 我的借阅表格
        brTitleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(resolveBookTitle(c.getValue().getBookId())));
        brBorrowDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBorrowDate())));
        brDueCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDueDate())));
        brStatusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        brFineCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getFine()==null?0:c.getValue().getFine())));
        loadPage();
        pagination.currentPageIndexProperty().addListener((obs, o, n) -> {
            page = n.intValue() + 1;
            loadPage();
        });
        loadMyBorrows();
    }

    @FXML
    private void onSearch() {
        page = 1;
        loadPage();
    }

    private void loadPage() {
        String kw = keywordField == null ? "" : keywordField.getText();
        List<Book> list = libraryService.searchBooks(kw, page, pageSize);
        data.setAll(list);
        infoLabel.setText("共 " + data.size() + " 条（本页）");
    }

    private void loadMyBorrows() {
        var list = libraryService.listMyBorrows(getCurrentUserId());
        // 这里只显示在借记录
        if (borrowTable != null) {
            borrowTable.getItems().setAll(list);
        }
    }

    private String resolveBookTitle(Integer bookId) {
        try {
            // 简单做法：在当前数据集中查找，找不到就默认显示ID
            for (Book b : data) {
                if (b.getBookId() != null && b.getBookId().equals(bookId)) return b.getTitle();
            }
            // 如需更稳妥，可通过 service 再查一次该书
            var list = libraryService.searchBooks("", 1, 200);
            for (Book b : list) {
                if (b.getBookId() != null && b.getBookId().equals(bookId)) return b.getTitle();
            }
        } catch (Exception ignored) {}
        return String.valueOf(bookId);
    }

    @FXML
    private void onBorrow() {
        Book sel = bookTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本书"); return; }
        int days = askDays("借书时长（天）", 30);
        var res = libraryService.borrowBookWithReason(getCurrentUserId(), sel.getBookId(), days);
        if (res.isSuccess()) { showInformation("提示", res.getMessage()); loadPage(); } else { showError(res.getMessage()); }
    }

    @FXML
    private void onReserve() {
        Book sel = bookTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本书"); return; }
        var res = libraryService.reserveBookWithReason(getCurrentUserId(), sel.getBookId());
        if (res.isSuccess()) { showInformation("提示", res.getMessage()); } else { showError(res.getMessage()); }
    }

    @FXML private void onRenew() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要续借的记录"); return; }
        int days = askDays("续借时长（天）", 30);
        var res = libraryService.renewBorrowWithReason(getCurrentUserId(), sel.getRecordId(), days, 1);
        if (res.isSuccess()) { showInformation("提示", res.getMessage()); loadMyBorrows(); }
        else { showError(res.getMessage()); }
    }

    @FXML private void onReturn() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要归还的记录"); return; }
        var res = libraryService.returnBookWithReason(getCurrentUserId(), sel.getRecordId(), sel.getBookId());
        if (res.isSuccess()) { showInformation("提示", res.getMessage()); loadMyBorrows(); loadPage(); }
        else { showError(res.getMessage()); }
    }

    @FXML private void onRefreshBorrows() { loadMyBorrows(); }

    // 从 BaseController 或登录上下文获取当前用户ID，这里先占位实现
    private String getCurrentUserId() {
        Integer uid = com.vCampus.common.SessionContext.requireCurrentUserId();
        return uid == null ? "48" : String.valueOf(uid);
    }

    private int askDays(String title, int defVal) {
        TextInputDialog dlg = new TextInputDialog(String.valueOf(defVal));
        dlg.setTitle(title);
        dlg.setHeaderText(null);
        dlg.setContentText("请输入天数(1-30)：");
        var r = dlg.showAndWait();
        if (r.isEmpty()) return defVal;
        try {
            int d = Integer.parseInt(r.get());
            if (d < 1) d = 1; if (d > 30) d = 30;
            return d;
        } catch (Exception e) {
            return defVal;
        }
    }
}


