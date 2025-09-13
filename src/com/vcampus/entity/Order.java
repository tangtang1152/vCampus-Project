package com.vcampus.entity;

import java.util.Date;
import java.util.List;

/**
 * 订单实体类
 * 对应数据库表: tbl_order
 */
public class Order {
    private String orderId;
    private String studentId;
    private Date orderDate;
    private Double totalAmount;
    private String status;
    
    // 非数据库字段，用于业务逻辑
    private List<OrderItem> items;
    private Student student; // 订单对应的学生信息

    public Order() {
    }

    public Order(String orderId, String studentId, Date orderDate, 
                Double totalAmount, String status) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getter and Setter methods
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}
