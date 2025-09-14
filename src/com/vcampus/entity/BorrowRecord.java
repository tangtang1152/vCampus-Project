package com.vcampus.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 借阅记录实体类
 * @author YourName
 */
public class BorrowRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer recordId;     // 借阅记录ID
    private String studentId;     // 学生ID
    private Integer bookId;       // 图书ID
    private Date borrowDate;      // 借出日期
    private Date dueDate;         // 应还日期
    private Date returnDate;      // 实际归还日期
    private String status;        // 借阅状态：borrowing, returned, overdue
    
    // 无参构造方法
    public BorrowRecord() {}

    // 带参构造方法
    public BorrowRecord(Integer recordId, String studentId, Integer bookId, 
                       Date borrowDate, Date dueDate, Date returnDate, String status) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getter 和 Setter 方法
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String calculateStatus() {
        if (returnDate != null) {
            return "已归还";
        } else {
            // 判断是否超期（比如借期 30 天）
            long borrowTime = borrowDate.getTime();
            long now = System.currentTimeMillis();
            long days = (now - borrowTime) / (1000 * 60 * 60 * 24);

            if (days > 30) {   // 假设超期标准是 30 天
                return "已超期";
            } else {
                return "未归还";
            }
        }
    }
    // ============ 状态计算方法 ============
    
    /**
     * 自动计算并更新借阅状态
     * @return 更新后的状态
     */
    public String calculateAndUpdateStatus() {
        if (returnDate != null) {
            status = "returned";
        } else if (dueDate != null && dueDate.before(new Date())) {
            status = "overdue";
        } else {
            status = "borrowing";
        }
        return status;
    }

    /**
     * 检查是否超期
     * @return true如果超期且未归还
     */
    public boolean isOverdue() {
        return "overdue".equals(status);
    }

    /**
     * 检查是否已归还
     * @return true如果已归还
     */
    public boolean isReturned() {
        return "returned".equals(status);
    }

    /**
     * 检查是否借阅中
     * @return true如果借阅中且未超期
     */
    public boolean isBorrowing() {
        return "borrowing".equals(status);
    }

    /**
     * 获取借阅天数
     * @return 借阅天数
     */
    public int getBorrowDays() {
        if (borrowDate == null) return 0;
        
        long endTime = (returnDate != null) ? returnDate.getTime() : System.currentTimeMillis();
        long days = (endTime - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
        return (int) Math.max(1, days);
    }

    /**
     * 获取剩余天数
     * @return 剩余天数，负数表示超期天数
     */
    public int getRemainingDays() {
        if (dueDate == null || returnDate != null) return 0;
        
        long currentTime = System.currentTimeMillis();
        long dueTime = dueDate.getTime();
        
        // 计算剩余毫秒数
        long remainingMillis = dueTime - currentTime;
        
        // 转换为天数（向上取整）
        long days = (long) Math.ceil(remainingMillis / (1000.0 * 60 * 60 * 24));
        
        return (int) days;
    }

    /**
     * 获取超期天数（正数表示超期天数）
     */
    public int getOverdueDays() {
        if (dueDate == null || returnDate != null) return 0;
        
        long currentTime = System.currentTimeMillis();
        long dueTime = dueDate.getTime();
        
        if (currentTime <= dueTime) return 0;
        
        // 计算超期毫秒数
        long overdueMillis = currentTime - dueTime;
        
        // 转换为天数（向上取整）
        long days = (long) Math.ceil(overdueMillis / (1000.0 * 60 * 60 * 24));
        
        return (int) days;
    }

    /**
     * 检查是否可以续借
     * @return true如果可以续借
     */
    public boolean canRenew() {
        return isBorrowing() && !isOverdue();
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "recordId=" + recordId +
                ", studentId='" + studentId + '\'' +
                ", bookId=" + bookId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BorrowRecord that = (BorrowRecord) o;
        return recordId != null && recordId.equals(that.recordId);
    }

    @Override
    public int hashCode() {
        return recordId != null ? recordId.hashCode() : 0;
    }

    /**
     * 复制对象
     */
    public BorrowRecord copy() {
        return new BorrowRecord(recordId, studentId, bookId, borrowDate, dueDate, returnDate, status);
    }

    /**
     * 检查数据有效性
     */
    public boolean isValid() {
        return studentId != null && !studentId.trim().isEmpty() &&
               bookId != null && bookId > 0 &&
               borrowDate != null && dueDate != null &&
               dueDate.after(borrowDate) &&
               status != null && !status.trim().isEmpty();
    }
}