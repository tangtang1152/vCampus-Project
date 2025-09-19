package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Book;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.common.LibrarySession;
import com.vCampus.util.TransactionManager;
import com.vCampus.util.LibraryUserRules;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brRemainingDaysCol;
    @FXML private TableColumn<com.vCampus.entity.BorrowRecord, String> brRenewTimesCol;
    @FXML private Label infoLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Pagination pagination;
    @FXML private ComboBox<String> bookStatusBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button btnRenew;
    @FXML private Button btnReturn;

    private final LibraryService libraryService = ServiceFactory.getLibraryService();
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
        
        // 行右键菜单：查看详情
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            MenuItem viewItem = new MenuItem("查看详情");
            viewItem.setOnAction(e -> {
                Book book = row.getItem();
                if (book != null) {
                    showBookDetail(book);
                }
            });
            ContextMenu menu = new ContextMenu(viewItem);
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );
            return row;
        });
        // 我的借阅表格
        brTitleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(resolveBookTitle(c.getValue().getBookId())));
        brBorrowDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getBorrowDate())));
        brDueCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDueDate())));
        brStatusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        brFineCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getFine()==null?0:c.getValue().getFine())));
        // 恢复增强列
        if (brRemainingDaysCol != null) {
            brRemainingDaysCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(calculateRemainingDays(c.getValue())));
        }
        if (brRenewTimesCol != null) {
            brRenewTimesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatRenewTimes(c.getValue())));
        }
        // 借阅记录状态筛选初始化
        if (statusFilter != null) {
            statusFilter.getItems().setAll("全部", "借出", "已还", "逾期");
            statusFilter.setValue("借出");
            statusFilter.valueProperty().addListener((obs, o, n) -> loadMyBorrows());
        }
        // 图书列表额外筛选/排序
        if (bookStatusBox != null) {
            bookStatusBox.getItems().setAll("全部", "正常", "下架");
            bookStatusBox.setValue("全部");
            bookStatusBox.valueProperty().addListener((o,ov,nv)-> { page=1; loadPage(); });
        }
        if (sortBox != null) {
            sortBox.getItems().setAll("默认(最新)", "书名↑", "书名↓", "可借↑", "可借↓");
            sortBox.setValue("默认(最新)");
            sortBox.valueProperty().addListener((o,ov,nv)-> { page=1; loadPage(); });
        }

        // 前端按钮权限（activeRole）
        var user = com.vCampus.common.SessionContext.getCurrentUser();
        boolean canUse = com.vCampus.util.RBACUtil.canUseLibrary(user);
        boolean canMaintain = com.vCampus.util.RBACUtil.canMaintainLibrary(user);
        if (btnRenew != null) btnRenew.setDisable(!canUse);
        if (btnReturn != null) btnReturn.setDisable(!canUse);
        asyncLoadPage();
        pagination.currentPageIndexProperty().addListener((obs, o, n) -> {
            page = n.intValue() + 1;
            asyncLoadPage();
        });
        asyncLoadMyBorrows();
        // 恢复行样式设置
        setupBorrowTableRowFactory();
    }

    @FXML
    private void onSearch() {
        page = 1;
        asyncLoadPage();
    }

    private void asyncLoadPage() {
        String kw = keywordField == null ? "" : keywordField.getText();
        String st = bookStatusBox == null ? "全部" : String.valueOf(bookStatusBox.getValue());
        String sort = sortBox == null ? "默认(最新)" : String.valueOf(sortBox.getValue());
        int curPage = page;
        int size = pageSize;
        LibrarySession.setKeyword(kw);
        LibrarySession.setBookStatusFilter(st);
        LibrarySession.setSortOption(sort);
        LibrarySession.setCurrentPage(curPage);
        LibrarySession.setPageSize(size);

        new Thread(() -> {
            List<Book> list = libraryService.searchBooksAdvanced(kw, st, sort, curPage, size);
            TransactionManager.runLaterSafe(() -> {
                data.setAll(list);
                infoLabel.setText("共 " + data.size() + " 条（本页）");
                LibrarySession.setLastRefreshMillis(System.currentTimeMillis());
            });
        }, "lib-loadPage").start();
    }

    private void asyncLoadMyBorrows() {
        String status = statusFilter == null ? "借出" : statusFilter.getValue();
        LibrarySession.setBorrowStatus(status);
        String uid = getCurrentUserId();
        new Thread(() -> {
            var list = libraryService.listMyBorrowsByStatus(uid, status);
            TransactionManager.runLaterSafe(() -> {
                if (borrowTable != null) {
                    borrowTable.getItems().setAll(list);
                }
            });
        }, "lib-loadMyBorrows").start();
    }

    // 顶部筛选按钮已移除，这里不再需要 onFilter

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
        new Thread(() -> {
            var res = libraryService.borrowBookWithReason(getCurrentUserId(), sel.getBookId(), days);
            TransactionManager.runLaterSafe(() -> {
                if (res.isSuccess()) { showInformation("提示", res.getMessage()); asyncLoadPage(); asyncLoadMyBorrows(); }
                else { showError(res.getMessage()); }
            });
        }, "lib-borrow").start();
    }

    @FXML
    private void onReserve() {
        Book sel = bookTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本书"); return; }
        new Thread(() -> {
            var res = libraryService.reserveBookWithReason(getCurrentUserId(), sel.getBookId());
            TransactionManager.runLaterSafe(() -> {
                if (res.isSuccess()) { showInformation("提示", res.getMessage()); }
                else { showError(res.getMessage()); }
            });
        }, "lib-reserve").start();
    }

    @FXML private void onRenew() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要续借的记录"); return; }
        
        int days = askDays("续借时长（天）", 30);
        // maxTimes 由服务层按角色判断，这里传1不再生效，但保持参数兼容
        new Thread(() -> {
            var res = libraryService.renewBorrowWithReason(getCurrentUserId(), sel.getRecordId(), days, 1);
            TransactionManager.runLaterSafe(() -> {
                if (res.isSuccess()) { showInformation("提示", res.getMessage()); asyncLoadMyBorrows(); }
                else { showError(res.getMessage()); }
            });
        }, "lib-renew").start();
    }

    @FXML private void onReturn() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要归还的记录"); return; }
        new Thread(() -> {
            var res = libraryService.returnBookWithReason(getCurrentUserId(), sel.getRecordId(), sel.getBookId());
            TransactionManager.runLaterSafe(() -> {
                if (res.isSuccess()) { showInformation("提示", res.getMessage()); asyncLoadMyBorrows(); asyncLoadPage(); }
                else { showError(res.getMessage()); }
            });
        }, "lib-return").start();
    }

    @FXML private void onRefreshBorrows() { asyncLoadMyBorrows(); }

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
    
    /**
     * 计算剩余天数
     */
    private String calculateRemainingDays(BorrowRecord record) {
        if (record.getDueDate() == null) return "未知";
        
        LocalDate dueDate = record.getDueDate().toLocalDate();
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, dueDate);
        
        if (days < 0) {
            return "逾期 " + Math.abs(days) + " 天";
        } else if (days == 0) {
            return "今天到期";
        } else {
            return days + " 天";
        }
    }
    
    /**
     * 格式化续借次数显示
     */
    private String formatRenewTimes(BorrowRecord record) {
        int currentRenew = record.getRenewTimes() == null ? 0 : record.getRenewTimes();
        // 暂时使用默认值，避免复杂的字符串解析
        int maxRenew = 1; // 默认值
        return currentRenew + "/" + maxRenew;
    }
    
    /**
     * 设置借阅记录表格的行样式
     */
    private void setupBorrowTableRowFactory() {
        borrowTable.setRowFactory(tv -> {
            TableRow<BorrowRecord> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && newItem.getDueDate() != null) {
                    LocalDate dueDate = newItem.getDueDate().toLocalDate();
                    LocalDate today = LocalDate.now();
                    long days = ChronoUnit.DAYS.between(today, dueDate);
                    
                    if (days < 0) {
                        // 逾期：红色背景
                        row.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    } else if (days <= 3) {
                        // 即将到期（3天内）：黄色背景
                        row.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                    } else {
                        // 正常状态：绿色背景
                        row.setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                    }
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
    }
    
    /**
     * 显示图书详情页面（暂时注释掉）
     */
    private void showBookDetail(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book-detail-view.fxml"));
            Stage detailStage = new Stage();
            detailStage.setScene(new Scene(loader.load()));
            detailStage.setTitle("图书详情 - " + book.getTitle());
            
            BookDetailController controller = loader.getController();
            controller.setBook(book);
            
            detailStage.show();
        } catch (IOException e) {
            showError("打开图书详情失败：" + e.getMessage());
        }
    }
}


