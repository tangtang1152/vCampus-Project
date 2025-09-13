package com.vcampus.entity;

import java.util.Date;

/**
 * 学生实体类
 * 对应数据库表: tbl_student
 */
public class Student {
    private String studentId;   // 学号，主键
    private Integer userId;     // 外键，关联tbl_user.userId
    private String studentName; // 学生姓名
    private String classId;     // 班级ID
    private Date enrollDate;    // 入学日期
    private String sex;         // 性别
    private String email;       // 邮箱
    private String idCard;      // 身份证号
    private String status;      // 学籍状态

    // 构造方法
    public Student() {}

    public Student(String studentId, Integer userId, String studentName, String classId, Date enrollDate, String sex, String email, String idCard, String status) {
        this.studentId = studentId;
        this.userId = userId;
        this.studentName = studentName;
        this.classId = classId;
        this.enrollDate = enrollDate;
        this.sex = sex;
        this.email = email;
        this.idCard = idCard;
        this.status = status;
    }

    // Getter 和 Setter 方法
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Date getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", userId=" + userId +
                ", studentName='" + studentName + '\'' +
                ", classId='" + classId + '\'' +
                ", enrollDate=" + enrollDate +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", idCard='" + idCard + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}