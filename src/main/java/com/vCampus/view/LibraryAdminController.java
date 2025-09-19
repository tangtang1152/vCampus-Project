package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Book;
import com.vCampus.service.LibraryService;
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

    private final LibraryService service = new LibraryService();
    private final ObservableList<Book> data = FXCollections.observableArrayList();
    private final com.vCampus.dao.IBorrowRecordDao borrowDao = new com.vCampus.dao.BorrowRecordDao();

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
        installContextMenu();
        refresh();
    }

    @FXML private void onRefresh() { refresh(); }
    @FXML private void onSearch() { refresh(); }
    @FXML private void onImportCsv() { showInformation("导入CSV", "占位：可在此实现文件选择并批量导入"); }
    @FXML private void onExportCsv() { showInformation("导出CSV", "占位：可在此实现导出当前列表为CSV"); }

    private void refresh() {
        String kw = keywordField == null ? "" : keywordField.getText();
        List<Book> list = service.searchBooks(kw, 1, 200);
        data.setAll(list);
    }

    @FXML private void onAdd() { editDialog(null); }
    @FXML private void onEdit() { editDialog(table.getSelectionModel().getSelectedItem()); }

    @FXML private void onDelete() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要删除的图书"); return; }
        if (!showConfirmation("删除图书", "确认删除《" + sel.getTitle() + "》?")) return;
        boolean ok = service.deleteBook(sel.getBookId());
        if (ok) { showInformation("提示", "删除成功"); refresh(); } else { showError("删除失败"); }
    }

    @FXML private void onInc() { adjustStock(1); }
    @FXML private void onDec() { adjustStock(-1); }

    private void adjustStock(int delta) {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        boolean ok = delta > 0 ? service.increaseStock(sel.getBookId(), delta) : service.decreaseStock(sel.getBookId(), -delta);
        if (ok) { showInformation("提示", "库存已更新"); refresh(); } else { showError("库存更新失败"); }
    }

    private void installContextMenu() {
        MenuItem viewBorrow = new MenuItem("查看借阅情况");
        viewBorrow.setOnAction(e -> showBorrowDialog());
        ContextMenu menu = new ContextMenu(viewBorrow);
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setContextMenu(menu);
            return row;
        });
    }

    private void showBorrowDialog() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本图书"); return; }
        try {
            java.util.List<com.vCampus.entity.BorrowRecord> records = com.vCampus.util.TransactionManager.executeInTransaction(conn ->
                borrowDao.listByBook(sel.getBookId(), conn)
            );
            int total = com.vCampus.util.TransactionManager.executeInTransaction(conn ->
                borrowDao.countTotalByBook(sel.getBookId(), conn)
            );
            java.time.LocalDate now = java.time.LocalDate.now();
            int month = com.vCampus.util.TransactionManager.executeInTransaction(conn ->
                borrowDao.countMonthlyByBook(sel.getBookId(), java.sql.Date.valueOf(now.withDayOfMonth(1)), java.sql.Date.valueOf(now.withDayOfMonth(1).plusMonths(1)), conn)
            );
            int current = com.vCampus.util.TransactionManager.executeInTransaction(conn ->
                borrowDao.countCurrentBorrowedByBook(sel.getBookId(), conn)
            );

            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("借阅情况 - 《" + sel.getTitle() + "》");

            TableView<com.vCampus.entity.BorrowRecord> tv = new TableView<>();
            TableColumn<com.vCampus.entity.BorrowRecord, Number> cId = new TableColumn<>("记录ID");
            cId.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getRecordId()==null?0:x.getValue().getRecordId()));
            TableColumn<com.vCampus.entity.BorrowRecord, Number> cUser = new TableColumn<>("用户ID");
            cUser.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getUserId()==null?0:x.getValue().getUserId()));
            TableColumn<com.vCampus.entity.BorrowRecord, String> cUserName = new TableColumn<>("用户名");
            cUserName.setCellValueFactory(x -> new SimpleStringProperty(resolveUsername(x.getValue().getUserId())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> cBorrow = new TableColumn<>("借出");
            cBorrow.setCellValueFactory(x -> new SimpleStringProperty(String.valueOf(x.getValue().getBorrowDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> cDue = new TableColumn<>("到期");
            cDue.setCellValueFactory(x -> new SimpleStringProperty(String.valueOf(x.getValue().getDueDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> cReturn = new TableColumn<>("归还");
            cReturn.setCellValueFactory(x -> new SimpleStringProperty(String.valueOf(x.getValue().getReturnDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> cStatus = new TableColumn<>("状态");
            cStatus.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getStatus()));
            tv.getColumns().addAll(cId, cUser, cUserName, cBorrow, cDue, cReturn, cStatus);
            tv.setItems(FXCollections.observableArrayList(records));

            Label summary = new Label("总借阅: " + total + "    本月: " + month + "    当前借出: " + current);
            summary.setStyle("-fx-font-weight: bold;");

            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10, summary, tv);
            box.setPadding(new javafx.geometry.Insets(10));
            dlg.getDialogPane().setContent(box);
            dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dlg.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("加载借阅情况失败: " + ex.getMessage());
        }
    }

    // 简单用户名缓存，避免重复查询
    private final java.util.Map<Integer,String> userNameCache = new java.util.HashMap<>();
    private String resolveUsername(Integer userId) {
        if (userId == null) return "";
        if (userNameCache.containsKey(userId)) return userNameCache.get(userId);
        try {
            String name = com.vCampus.util.TransactionManager.executeInTransaction(conn -> {
                com.vCampus.dao.IUserDao ud = new com.vCampus.dao.UserDaoImpl();
                com.vCampus.entity.User u = ud.findById(userId, conn);
                return u == null ? String.valueOf(userId) : u.getUsername();
            });
            userNameCache.put(userId, name);
            return name;
        } catch (Exception e) {
            return String.valueOf(userId);
        }
    }

    @FXML private void onSetStatus() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        String st = statusBox.getValue();
        if (st == null || st.isBlank()) { showWarning("请选择状态"); return; }
        boolean ok = service.setBookStatus(sel.getBookId(), st);
        if (ok) { showInformation("提示", "状态已更新"); refresh(); } else { showError("状态更新失败"); }
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
                boolean ok = origin == null ? service.addBook(b) : service.updateBook(b);
                if (ok) { showInformation("提示", origin==null?"新增成功":"保存成功"); refresh(); } else { showError("保存失败"); }
            }
        });
    }
}


