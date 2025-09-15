package com.vCampus.entity;

import java.util.Date;

/**
 * 学生实体类
 * 表示系统中的学生信息，包含学生的所有属性
 */
public class Student extends User {
    
    private String studentId;      // 学号
    private String studentName;    // 学生姓名
    private String className;      // 班级名称
    private Date enrollDate;       // 入学日期
    private String sex;            // 性别
    private String email;          // 邮箱
    private String idCard;         // 身份证号
    private String status;         // 学籍状态
    
    // ==================== 构造函数 ====================
    
    /**
     * 默认构造函数
     */
    public Student() {
        super();
        this.studentId = "";
        this.studentName = "";
        this.className = "";
        this.enrollDate = new Date();
        this.sex = "";
        this.email = "";
        this.idCard = "";
        this.status = "正常";
    }
    
    /**
     * 完整参数构造函数
     */
    public Student(int userId, String username, String password, String role,
                  String studentId, String studentName, String className,
                  Date enrollDate, String sex, String email, String idCard, String status) {
        super(userId, username, password, role);
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.enrollDate = enrollDate;
        this.sex = sex;
        this.email = email;
        this.idCard = idCard;
        this.status = status;
    }
    
    // ==================== Getter 方法 ====================
    
    public String getStudentId() {
        return studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public Date getEnrollDate() {
        return enrollDate;
    }
    
    public String getSex() {
        return sex;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getIdCard() {
        return idCard;
    }
    
    public String getStatus() {
        return status;
    }
    
    // ==================== Setter 方法 ====================
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // ==================== 辅助方法 ====================
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", enrollDate=" + enrollDate +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", idCard='" + idCard + '\'' +
                ", status='" + status + '\'' +
                ", " + super.toString() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        Student student = (Student) o;
        return studentId.equals(student.studentId);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + studentId.hashCode();
        return result;
    }
}