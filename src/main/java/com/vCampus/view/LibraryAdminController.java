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
    private javafx.animation.Timeline autoRefresh;
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
        // 行右键：查看借阅详情
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            MenuItem viewBorrowers = new MenuItem("查看借阅详情");
            viewBorrowers.setOnAction(e -> {
                Book book = row.getItem();
                if (book != null) showBorrowersDialog(book);
            });
            ContextMenu menu = new ContextMenu(viewBorrowers);
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(menu)
            );
            return row;
        });
        refresh();
        startAutoRefresh();
    }

    @FXML private void onRefresh() { refresh(); }
    @FXML private void onSearch() { refresh(); }
    @FXML private void onImportCsv() { showInformation("导入CSV", "占位：可在此实现文件选择并批量导入"); }
    @FXML private void onExportCsv() { showInformation("导出CSV", "占位：可在此实现导出当前列表为CSV"); }

    private final java.util.concurrent.atomic.AtomicBoolean loading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private void refresh() {
        if (!loading.compareAndSet(false, true)) return;
        String kw = keywordField == null ? "" : keywordField.getText();
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            startDaemon(() -> {
                java.util.List<Book> list = null;
                try {
                    var req = new com.vCampus.net.dto.SocketRequest("LIB_LIST").put("keyword", kw).put("status", "全部").put("sort", "默认(最新)").put("page", "1").put("size", "500");
                    java.net.Socket s = new java.net.Socket();
                    s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
                    s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
                    try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                         java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                        out.writeObject(req); out.flush();
                        Object obj = in.readObject();
                        if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m && m.get("rows") instanceof java.util.List<?> rows) {
                            list = new java.util.ArrayList<>();
                            for (Object r : rows) {
                                if (r instanceof java.util.Map<?,?> rm) {
                                    Book b = new Book();
                                    Object id = rm.get("bookId"); if (id != null) b.setBookId(((Number)id).intValue());
                                    b.setIsbn(String.valueOf(rm.get("isbn")));
                                    b.setTitle(String.valueOf(rm.get("title")));
                                    b.setAuthor(String.valueOf(rm.get("author")));
                                    b.setCategory(String.valueOf(rm.get("category")));
                                    b.setPublisher(String.valueOf(rm.get("publisher")));
                                    Object tc = rm.get("totalCopies"); if (tc != null) b.setTotalCopies(((Number)tc).intValue());
                                    Object av = rm.get("availableCopies"); if (av != null) b.setAvailableCopies(((Number)av).intValue());
                                    b.setLocation(String.valueOf(rm.get("location")));
                                    b.setStatus(String.valueOf(rm.get("status")));
                                    list.add(b);
                                }
                            }
                        }
                    } finally { s.close(); }
                } catch (Exception ignored) {}
                final java.util.List<Book> flist = list;
                javafx.application.Platform.runLater(() -> {
                    if (flist == null) {
                        if (keywordField != null) keywordField.setPromptText("服务器不可用");
                    } else {
                        data.setAll(flist);
                    }
                });
                loading.set(false);
            }, "libadmin-refresh");
            return;
        }
        List<Book> list = service.searchBooks(kw, 1, 200);
        data.setAll(list);
        loading.set(false);
    }

    @FXML private void onAdd() { editDialog(null); }
    @FXML private void onEdit() { editDialog(table.getSelectionModel().getSelectedItem()); }

    @FXML private void onDelete() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要删除的图书"); return; }
        if (!showConfirmation("删除图书", "确认删除《" + sel.getTitle() + "》?")) return;
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            ok = sendBookOp("LIB_DELETE", sel);
        } else {
            ok = service.deleteBook(sel.getBookId());
        }
        if (ok) { showInformation("提示", "删除成功"); refresh(); } else { showError("删除失败"); }
    }

    @FXML private void onInc() { adjustStock(1); }
    @FXML private void onDec() { adjustStock(-1); }

    private void adjustStock(int delta) {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            // 通过更新接口提交 total/available 的新值
            Book b = new Book();
            b.setBookId(sel.getBookId());
            b.setIsbn(sel.getIsbn()); b.setTitle(sel.getTitle()); b.setAuthor(sel.getAuthor());
            b.setCategory(sel.getCategory()); b.setPublisher(sel.getPublisher()); b.setPubDate(sel.getPubDate());
            int newTotal = (sel.getTotalCopies()==null?0:sel.getTotalCopies()) + (delta>0?delta:-delta);
            int newAvail = (sel.getAvailableCopies()==null?0:sel.getAvailableCopies()) + (delta>0?delta:-delta);
            if (delta < 0) { newTotal = (sel.getTotalCopies()==null?0:sel.getTotalCopies()) - (-delta); newAvail = (sel.getAvailableCopies()==null?0:sel.getAvailableCopies()) - (-delta); }
            b.setTotalCopies(newTotal); b.setAvailableCopies(newAvail);
            ok = sendBookOp("LIB_UPDATE", b);
        } else {
            ok = delta > 0 ? service.increaseStock(sel.getBookId(), delta) : service.decreaseStock(sel.getBookId(), -delta);
        }
        if (ok) { showInformation("提示", "库存已更新"); refresh(); } else { showError("库存更新失败"); }
    }

    @FXML private void onSetStatus() {
        Book sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择图书"); return; }
        String st = statusBox.getValue();
        if (st == null || st.isBlank()) { showWarning("请选择状态"); return; }
        boolean ok;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            Book b = new Book(); b.setBookId(sel.getBookId()); b.setStatus(st);
            ok = sendBookOp("LIB_SET_STATUS", b);
        } else {
            ok = service.setBookStatus(sel.getBookId(), st);
        }
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
                boolean ok;
                if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
                    ok = sendBookOp(origin==null?"LIB_ADD":"LIB_UPDATE", b);
                } else {
                    ok = origin == null ? service.addBook(b) : service.updateBook(b);
                }
                if (ok) { showInformation("提示", origin==null?"新增成功":"保存成功"); refresh(); } else { showError("保存失败"); }
            }
        });
    }

    private boolean sendBookOp(String action, Book b) {
        try {
            java.util.Map<String,String> pm = new java.util.HashMap<>();
            if (b.getBookId() != null) pm.put("bookId", String.valueOf(b.getBookId()));
            if (b.getIsbn() != null) pm.put("isbn", b.getIsbn());
            if (b.getTitle() != null) pm.put("title", b.getTitle());
            if (b.getAuthor() != null) pm.put("author", b.getAuthor());
            if (b.getCategory() != null) pm.put("category", b.getCategory());
            if (b.getPublisher() != null) pm.put("publisher", b.getPublisher());
            if (b.getPubDate() != null) pm.put("pubDate", b.getPubDate().toString());
            if (b.getTotalCopies() != null) pm.put("totalCopies", String.valueOf(b.getTotalCopies()));
            if (b.getAvailableCopies() != null) pm.put("availableCopies", String.valueOf(b.getAvailableCopies()));
            if (b.getLocation() != null) pm.put("location", b.getLocation());
            if (b.getStatus() != null) pm.put("status", b.getStatus());
            var req = new com.vCampus.net.dto.SocketRequest(action, pm);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                return (obj instanceof com.vCampus.net.dto.SocketResponse resp) && resp.isSuccess();
            } finally { s.close(); }
        } catch (Exception e) { return false; }
    }

    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(8), e -> refresh())
        );
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
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

    private void showBorrowersDialog(Book book) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("借阅详情 - " + (book.getTitle()==null?"":book.getTitle()));
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        TabPane tabs = new TabPane();
        Tab tabActive = new Tab("当前借出"); tabActive.setClosable(false);
        Tab tabAll = new Tab("全部记录"); tabAll.setClosable(false);

        TableView<com.vCampus.entity.BorrowRecord> tvActive = new TableView<>();
        TableView<com.vCampus.entity.BorrowRecord> tvAll = new TableView<>();

        // 公用列构造函数
        java.util.function.Function<TableView<com.vCampus.entity.BorrowRecord>, Void> buildCols = tv -> {
            TableColumn<com.vCampus.entity.BorrowRecord, Number> colRid = new TableColumn<>("记录ID");
            colRid.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getRecordId()==null?0:c.getValue().getRecordId()));
            TableColumn<com.vCampus.entity.BorrowRecord, Number> colUid = new TableColumn<>("用户ID");
            colUid.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getUserId()==null?0:c.getValue().getUserId()));
            TableColumn<com.vCampus.entity.BorrowRecord, String> colBorrow = new TableColumn<>("借出日");
            colBorrow.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getBorrowDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> colDue = new TableColumn<>("到期日");
            colDue.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getDueDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> colRet = new TableColumn<>("归还日");
            colRet.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getReturnDate())));
            TableColumn<com.vCampus.entity.BorrowRecord, String> colStatus = new TableColumn<>("状态");
            colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getStatus())));
            tv.getColumns().setAll(colRid, colUid, colBorrow, colDue, colRet, colStatus);
            tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            return null;
        };

        buildCols.apply(tvActive); buildCols.apply(tvAll);

        tabActive.setContent(tvActive);
        tabAll.setContent(tvAll);
        tabs.getTabs().addAll(tabActive, tabAll);

        dlg.getDialogPane().setContent(tabs);

        // 加载数据
        java.util.List<com.vCampus.entity.BorrowRecord> actives = service.listActiveBorrowsByBook(book.getBookId());
        java.util.List<com.vCampus.entity.BorrowRecord> all = service.listBorrowsByBook(book.getBookId());
        tvActive.setItems(FXCollections.observableArrayList(actives));
        tvAll.setItems(FXCollections.observableArrayList(all));

        dlg.showAndWait();
    }
}


