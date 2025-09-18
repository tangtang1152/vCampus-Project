package com.vCampus.entity;

/**
 * 预约记录实体类，对应表 tbl_reservation
 */
public class Reservation {
    private Integer reservationId;     // 主键
    private Integer bookId;            // 外键 → tbl_book.bookId
    private Integer userId;            // 外键 → tbl_user.userId
    private java.sql.Date reservedAt;  // 预约时间
    private java.sql.Date expiresAt;   // 过期时间
    private Integer queueOrder;        // 排队序号
    private String status;             // 排队中/可借/取消/过期

    public Reservation() {}

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public java.sql.Date getReservedAt() { return reservedAt; }
    public void setReservedAt(java.sql.Date reservedAt) { this.reservedAt = reservedAt; }

    public java.sql.Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(java.sql.Date expiresAt) { this.expiresAt = expiresAt; }

    public Integer getQueueOrder() { return queueOrder; }
    public void setQueueOrder(Integer queueOrder) { this.queueOrder = queueOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}