package com.vCampus.entity;  // 注意包声明

import java.util.Date;

/**
 * 学籍状态变更记录实体类
 */
public class StatusChange {
    private Integer changeId;
    private String studentId;
    private String oldStatus;
    private String newStatus;
    private Date changeDate;
    private String reason;
    
    // 构造方法
    public StatusChange() {}
    
    // Getter和Setter方法
    public Integer getChangeId() { return changeId; }
    public void setChangeId(Integer changeId) { this.changeId = changeId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    
    public Date getChangeDate() { return changeDate; }
    public void setChangeDate(Date changeDate) { this.changeDate = changeDate; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}