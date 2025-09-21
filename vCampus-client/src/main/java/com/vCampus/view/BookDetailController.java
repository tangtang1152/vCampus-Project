package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Book;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.service.LibraryService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BookDetailController extends BaseController {
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label isbnLabel;
    @FXML private Label publisherLabel;
    @FXML private Label pubDateLabel;
    @FXML private Label categoryLabel;
    @FXML private Label locationLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalCopiesLabel;
    @FXML private Label availableCopiesLabel;
    @FXML private Label borrowedCopiesLabel;
    @FXML private Label totalBorrowsLabel;
    @FXML private Label monthlyBorrowsLabel;
    @FXML private Label currentBorrowsLabel;
    @FXML private TableView<Book> recommendTable;
    @FXML private TableColumn<Book, String> recTitleCol;
    @FXML private TableColumn<Book, String> recAuthorCol;
    @FXML private TableColumn<Book, Number> recAvailableCol;
    @FXML private Button borrowButton;
    @FXML private Button reserveButton;
    @FXML private Button favoriteButton;

    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<Book> recommendData = FXCollections.observableArrayList();
    private Book currentBook;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置推荐表格
        recTitleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        recAuthorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));
        recAvailableCol.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getAvailableCopies() == null ? 0 : c.getValue().getAvailableCopies()));
        recommendTable.setItems(recommendData);
    }

    /**
     * 设置要显示的图书
     */
    public void setBook(Book book) {
        this.currentBook = book;
        if (book == null) return;

        // 基本信息
        titleLabel.setText(book.getTitle() == null ? "未知" : book.getTitle());
        authorLabel.setText(book.getAuthor() == null ? "未知" : book.getAuthor());
        isbnLabel.setText(book.getIsbn() == null ? "未知" : book.getIsbn());
        publisherLabel.setText(book.getPublisher() == null ? "未知" : book.getPublisher());
        pubDateLabel.setText(book.getPubDate() == null ? "未知" : 
                book.getPubDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        categoryLabel.setText(book.getCategory() == null ? "未知" : book.getCategory());
        locationLabel.setText(book.getLocation() == null ? "未知" : book.getLocation());
        statusLabel.setText(book.getStatus() == null ? "正常" : book.getStatus());

        // 库存信息
        int total = book.getTotalCopies() == null ? 0 : book.getTotalCopies();
        int available = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
        int borrowed = total - available;
        
        totalCopiesLabel.setText(String.valueOf(total));
        availableCopiesLabel.setText(String.valueOf(available));
        borrowedCopiesLabel.setText(String.valueOf(borrowed));

        // 借阅统计（调用服务层统计）
        try {
            int[] stats = libraryService.statsForBook(book.getBookId());
            totalBorrowsLabel.setText(String.valueOf(stats[0]));
            monthlyBorrowsLabel.setText(String.valueOf(stats[1]));
            currentBorrowsLabel.setText(String.valueOf(stats[2]));
        } catch (Exception e) {
            totalBorrowsLabel.setText("--");
            monthlyBorrowsLabel.setText("--");
            currentBorrowsLabel.setText(String.valueOf(borrowed));
        }

        // 加载相关推荐
        loadRecommendations();

        // 更新按钮状态
        updateButtonStates();
    }

    /**
     * 加载相关推荐图书
     */
    private void loadRecommendations() {
        if (currentBook == null) return;

        try {
            // 按作者推荐
            List<Book> authorBooks = libraryService.searchBooks(currentBook.getAuthor(), 1, 10);
            // 按分类推荐
            List<Book> categoryBooks = libraryService.searchBooks(currentBook.getCategory(), 1, 10);
            
            // 合并并去重（排除当前图书）
            ObservableList<Book> allBooks = FXCollections.observableArrayList();
            for (Book book : authorBooks) {
                if (!book.getBookId().equals(currentBook.getBookId()) && !allBooks.contains(book)) {
                    allBooks.add(book);
                }
            }
            for (Book book : categoryBooks) {
                if (!book.getBookId().equals(currentBook.getBookId()) && !allBooks.contains(book)) {
                    allBooks.add(book);
                }
            }
            
            // 智能去重：用书名+作者判断；数量限制为最多 8 条
            ObservableList<Book> dedup = FXCollections.observableArrayList();
            for (Book b : allBooks) {
                boolean exists = false;
                for (Book d : dedup) {
                    if ((d.getTitle()+"|"+d.getAuthor()).equals((b.getTitle()+"|"+b.getAuthor()))) { exists = true; break; }
                }
                if (!exists) dedup.add(b);
                if (dedup.size() >= 8) break;
            }
            allBooks = dedup;
            
            recommendData.setAll(allBooks);
        } catch (Exception e) {
            showError("加载推荐图书失败：" + e.getMessage());
        }
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        if (currentBook == null) {
            borrowButton.setDisable(true);
            reserveButton.setDisable(true);
            return;
        }

        boolean canBorrow = currentBook.getAvailableCopies() != null && currentBook.getAvailableCopies() > 0
                && !"下架".equals(currentBook.getStatus());
        boolean canReserve = currentBook.getAvailableCopies() != null && currentBook.getAvailableCopies() <= 0
                && !"下架".equals(currentBook.getStatus());

        borrowButton.setDisable(!canBorrow);
        reserveButton.setDisable(!canReserve);
    }

    @FXML
    private void onBorrow() {
        if (currentBook == null) return;
        
        // 这里应该调用借书逻辑，暂时显示提示
        showInformation("借书", "借书功能将在主界面实现");
    }

    @FXML
    private void onReserve() {
        if (currentBook == null) return;
        
        // 这里应该调用预约逻辑，暂时显示提示
        showInformation("预约", "预约功能将在主界面实现");
    }

    @FXML
    private void onFavorite() {
        if (currentBook == null) return;
        
        showInformation("收藏", "收藏功能暂未实现");
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
