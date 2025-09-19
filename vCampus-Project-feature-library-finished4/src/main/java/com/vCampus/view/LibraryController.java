package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Book;
import com.vCampus.entity.BorrowRecord;
import client.net.SocketClient;
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

    // 移除直接依赖 LibraryService，改为通过 SocketClient 通信
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
        loadPage();
        pagination.currentPageIndexProperty().addListener((obs, o, n) -> {
            page = n.intValue() + 1;
            loadPage();
        });
        loadMyBorrows();
        // 恢复行样式设置
        setupBorrowTableRowFactory();
    }

    @FXML
    private void onSearch() {
        page = 1;
        loadPage();
    }

    private void loadPage() {
        try {
            String kw = keywordField == null ? "" : keywordField.getText();
            String st = bookStatusBox == null ? "全部" : String.valueOf(bookStatusBox.getValue());
            String sort = sortBox == null ? "默认(最新)" : String.valueOf(sortBox.getValue());
            
            // 构建搜索图书请求
            String request = "SEARCH_BOOKS:" + kw + ":" + st + ":" + sort + ":" + page + ":" + pageSize;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:BOOKS:")) {
                List<Book> books = parseBooksFromResponse(response);
                data.setAll(books);
                infoLabel.setText("共 " + data.size() + " 条（本页）");
            } else {
                showError("搜索图书失败: " + response);
                data.clear();
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMyBorrows() {
        try {
            String status = statusFilter == null ? "借出" : statusFilter.getValue();
            
            // 构建获取借阅记录请求
            String request = "GET_MY_BORROWS:" + getCurrentUserId() + ":" + status;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:BORROWS:")) {
                List<BorrowRecord> borrows = parseBorrowRecordsFromResponse(response);
                if (borrowTable != null) {
                    borrowTable.getItems().setAll(borrows);
                }
            } else {
                showError("获取借阅记录失败: " + response);
                if (borrowTable != null) {
                    borrowTable.getItems().clear();
                }
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 顶部筛选按钮已移除，这里不再需要 onFilter

    private String resolveBookTitle(Integer bookId) {
        try {
            // 简单做法：在当前数据集中查找，找不到就默认显示ID
            for (Book b : data) {
                if (b.getBookId() != null && b.getBookId().equals(bookId)) return b.getTitle();
            }
            // 如需更稳妥，可通过 SocketClient 再查一次该书
            String request = "SEARCH_BOOKS::全部:默认(最新):1:200";
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:BOOKS:")) {
                List<Book> books = parseBooksFromResponse(response);
                for (Book b : books) {
                    if (b.getBookId() != null && b.getBookId().equals(bookId)) return b.getTitle();
                }
            }
        } catch (Exception ignored) {}
        return String.valueOf(bookId);
    }

    @FXML
    private void onBorrow() {
        Book sel = bookTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本书"); return; }
        int days = askDays("借书时长（天）", 30);
        
        try {
            // 构建借书请求
            String request = "BORROW_BOOK:" + getCurrentUserId() + ":" + sel.getBookId() + ":" + days;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:BORROW:")) {
                String message = response.substring("SUCCESS:BORROW:".length());
                showInformation("提示", message);
                loadPage();
            } else {
                String errorMsg = response != null ? response.substring(response.indexOf(":") + 1) : "借书失败";
                showError(errorMsg);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onReserve() {
        Book sel = bookTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请先选择一本书"); return; }
        
        try {
            // 构建预约图书请求
            String request = "RESERVE_BOOK:" + getCurrentUserId() + ":" + sel.getBookId();
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:RESERVE:")) {
                String message = response.substring("SUCCESS:RESERVE:".length());
                showInformation("提示", message);
            } else {
                String errorMsg = response != null ? response.substring(response.indexOf(":") + 1) : "预约失败";
                showError(errorMsg);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onRenew() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要续借的记录"); return; }
        
        int days = askDays("续借时长（天）", 30);
        
        try {
            // 构建续借请求
            String request = "RENEW_BOOK:" + getCurrentUserId() + ":" + sel.getRecordId() + ":" + days;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:RENEW:")) {
                String message = response.substring("SUCCESS:RENEW:".length());
                showInformation("提示", message);
                loadMyBorrows();
            } else {
                String errorMsg = response != null ? response.substring(response.indexOf(":") + 1) : "续借失败";
                showError(errorMsg);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onReturn() {
        var sel = borrowTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要归还的记录"); return; }
        
        try {
            // 构建还书请求
            String request = "RETURN_BOOK:" + getCurrentUserId() + ":" + sel.getRecordId() + ":" + sel.getBookId();
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:RETURN:")) {
                String message = response.substring("SUCCESS:RETURN:".length());
                showInformation("提示", message);
                loadMyBorrows();
                loadPage();
            } else {
                String errorMsg = response != null ? response.substring(response.indexOf(":") + 1) : "还书失败";
                showError(errorMsg);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onRefreshBorrows() { loadMyBorrows(); }

    // 从 BaseController 或登录上下文获取当前用户ID
    private String getCurrentUserId() {
        var user = com.vCampus.common.SessionContext.getCurrentUser();
        if (user == null) {
            showError("用户未登录，请重新登录");
            return null;
        }
        
        // 如果用户有userId且不为0，直接使用
        if (user.getUserId() > 0) {
            return String.valueOf(user.getUserId());
        }
        
        // 如果没有userId，根据角色类型获取对应的ID
        if (user instanceof com.vCampus.entity.Student) {
            return ((com.vCampus.entity.Student) user).getStudentId();
        } else if (user instanceof com.vCampus.entity.Teacher) {
            return ((com.vCampus.entity.Teacher) user).getTeacherId();
        } else if (user instanceof com.vCampus.entity.Admin) {
            return ((com.vCampus.entity.Admin) user).getAdminId();
        }
        
        // 如果都没有，返回默认值（临时解决方案）
        return "48";
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

    /**
     * 从服务器响应解析图书列表
     */
    private List<Book> parseBooksFromResponse(String response) {
        List<Book> books = new java.util.ArrayList<>();
        try {
            String data = response.substring("SUCCESS:BOOKS:".length());
            if (data.isEmpty()) return books;
            
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
                    books.add(book);
                }
            }
        } catch (Exception e) {
            System.err.println("解析图书数据异常: " + e.getMessage());
        }
        return books;
    }

    /**
     * 从服务器响应解析借阅记录列表
     */
    private List<BorrowRecord> parseBorrowRecordsFromResponse(String response) {
        List<BorrowRecord> records = new java.util.ArrayList<>();
        try {
            String data = response.substring("SUCCESS:BORROWS:".length());
            if (data.isEmpty()) return records;
            
            String[] recordStrings = data.split("\\|");
            for (String recordString : recordStrings) {
                String[] fields = recordString.split(",");
                if (fields.length >= 8) {
                    BorrowRecord record = new BorrowRecord();
                    record.setRecordId(Integer.parseInt(fields[0]));
                    record.setBookId(Integer.parseInt(fields[1]));
                    if (!fields[2].isEmpty()) {
                        record.setBorrowDate(java.sql.Date.valueOf(fields[2]));
                    }
                    if (!fields[3].isEmpty()) {
                        record.setDueDate(java.sql.Date.valueOf(fields[3]));
                    }
                    if (!fields[4].isEmpty()) {
                        record.setReturnDate(java.sql.Date.valueOf(fields[4]));
                    }
                    record.setStatus(fields[5]);
                    record.setFine(Double.parseDouble(fields[6]));
                    record.setRenewTimes(Integer.parseInt(fields[7]));
                    records.add(record);
                }
            }
        } catch (Exception e) {
            System.err.println("解析借阅记录数据异常: " + e.getMessage());
        }
        return records;
    }
}


