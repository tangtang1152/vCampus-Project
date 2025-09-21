package com.vCampus.entity;

/**
 * 借阅记录实体类，对应表 tbl_borrow_record
 */
public class BorrowRecord {
    private Integer recordId;          // 主键
    private Integer bookId;            // 外键 → tbl_book.bookId
    private Integer userId;            // 外键 → tbl_user.userId
    private java.sql.Date borrowDate;  // 借出日期
    private java.sql.Date dueDate;     // 到期日期
    private java.sql.Date returnDate;  // 归还日期
    private Integer renewTimes;        // 续借次数
    private Double fine;               // 罚金
    private String status;             // 借出/已还/逾期

    public BorrowRecord() {}

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public java.sql.Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(java.sql.Date borrowDate) { this.borrowDate = borrowDate; }

    public java.sql.Date getDueDate() { return dueDate; }
    public void setDueDate(java.sql.Date dueDate) { this.dueDate = dueDate; }

    public java.sql.Date getReturnDate() { return returnDate; }
    public void setReturnDate(java.sql.Date returnDate) { this.returnDate = returnDate; }

    public Integer getRenewTimes() { return renewTimes; }
    public void setRenewTimes(Integer renewTimes) { this.renewTimes = renewTimes; }

    public Double getFine() { return fine; }
    public void setFine(Double fine) { this.fine = fine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}