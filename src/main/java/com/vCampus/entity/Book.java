package com.vCampus.entity;

/**
 * 图书实体类，与表 tbl_book 对应
 */
public class Book {
    private Integer bookId;            // 自增主键
    private String isbn;               // 国际标准书号
    private String title;              // 书名
    private String author;             // 作者
    private String category;           // 分类
    private String publisher;          // 出版社
    private java.sql.Date pubDate;     // 出版日期
    private Integer totalCopies;       // 馆藏总数
    private Integer availableCopies;   // 可借数量
    private String location;           // 馆藏位置
    private String status;             // 状态：正常/下架 等

    public Book() {}

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public java.sql.Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(java.sql.Date pubDate) {
        this.pubDate = pubDate;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}