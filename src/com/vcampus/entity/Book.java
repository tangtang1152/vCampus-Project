package com.vcampus.entity;

import java.io.Serializable;

/**
 * 图书实体类
 * @author YourName
 */
public class Book implements Serializable {
    private Integer bookId;      // 图书ID (改为Integer，对应自动编号)
    private String isbn;         // ISBN号
    private String title;        // 书名
    private String author;       // 作者
    private String publisher;    // 出版社
    private Integer publisherYear; // 出版年份 (新增字段)
    private String category;     // 图书分类
    private Integer totalCount;  // 总数量
    private Integer available;   // 可借数量
    private String location;     // 位置信息 (新增字段)
    private static final long serialVersionUID = 1L;
    // 无参构造方法
    public Book() {
    }

    // 带参构造方法
    public Book(Integer bookId, String isbn, String title, String author, 
                String publisher, Integer publisherYear, String category, 
                Integer totalCount, Integer available, String location) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publisherYear = publisherYear;
        this.category = category;
        this.totalCount = totalCount;
        this.available = available;
        this.location = location;
    }

    // Getter 和 Setter 方法
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublisherYear() {
        return publisherYear;
    }

    public void setPublisherYear(Integer publishYear) {
        this.publisherYear = publishYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishYear=" + publisherYear +
                ", category='" + category + '\'' +
                ", totalCount=" + totalCount +
                ", available=" + available +
                ", location='" + location + '\'' +
                '}';
    }
}