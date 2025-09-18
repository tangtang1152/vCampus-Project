package com.vCampus.entity;

import java.util.Date;
import java.util.Objects; // 导入 Objects 类

/**
 * 学生实体类
 * 表示系统中的学生信息，包含学生的基本属性
 *
 * 设计说明：
 * - 简化了学生类，只保留核心属性
 * - 使用驼峰命名法规范变量命名
 * - 提供了完整的构造函数和getter/setter方法
 */
public class Student extends User {

    /**
     * 学号 - 学生的唯一标识符
     * 通常由学校分配，用于唯一识别每个学生
     */
    private String studentId;

    private String studentName;

    /**
     * 班级名称 - 学生所属的班级
     * 用于组织和管理学生分组
     */
    private String className;

    // --
    // private Integer userId; // 外键，关联tbl_user.userId - 已在父类User中定义，不需要重复

    private Date enrollDate;    // 入学日期
    private String sex;         // 性别
    private String email;       // 邮箱
    private String idCard;      // 身份证号
    private String status;      // 学籍状态

    // == 构造函数 ==

    /**
     * 默认构造函数
     * 创建一个空的学生对象，所有属性为默认值
     */
    public Student() {
        super(); // 调用父类的默认构造函数
        this.studentId = "";
        this.studentName = "";
        this.className = "";
        this.enrollDate = null;
        this.sex = "";
        this.email = "";
        this.idCard = "";
        this.status = "";
    }

    /**
     * 带参数的构造函数
     * 使用提供的参数创建一个完整的学生对象
     *
     * @param userId 用户ID，从父类继承
     * @param username 用户名，从父类继承
     * @param password 密码，从父类继承
     * @param role 角色，从父类继承
     * @param studentId 学号，学生的学号标识
     * @param studentName 学生姓名，学生的全名
     * @param className 班级名称，学生所属的班级
     * @param enrollDate 入学日期
     * @param sex 性别
     * @param email 邮箱
     * @param idCard 身份证号
     * @param status 学籍状态
     */
    public Student(Integer userId, String username, String password, String role,
                   String studentId, String studentName, String className,
                   Date enrollDate, String sex, String email, String idCard, String status) {
        super(userId, username, password, role); // 调用父类的构造函数
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.enrollDate = enrollDate;
        this.sex = sex;
        this.email = email;
        this.idCard = idCard;
        this.status = status;
    }

    // == Getter 方法 ==


    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }
    //--

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

    // == Setter 方法 ==


    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }


    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }


    public void setClassName(String className) {
        this.className = className;
    }
    //--
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

    // == 辅助方法 ==

    /**
     * 返回对象的字符串表示形式
     * 用于调试和日志记录
     *
     * @return 包含所有属性值的字符串
     */
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", userId=" + getUserId() + // 使用父类的 getUserId()
                ", username='" + getUsername() + '\'' + // 使用父类的 getUsername()
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", enrollDate=" + enrollDate +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", idCard='" + idCard + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * 比较两个学生对象是否相等
     * 基于学号进行比较，因为学号是唯一标识
     *
     * @param o 要比较的对象
     * @return 如果学号相同则返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false; // 调用父类的 equals 方法
        Student student = (Student) o;
        // 比较字符串内容，并处理可能为null的情况
        return Objects.equals(studentId, student.studentId);
    }

    /**
     * 返回对象的哈希码值
     * 基于学号生成哈希码
     *
     * @return 对象的哈希码值
     */
    @Override
    public int hashCode() {
        // 结合父类的 hashCode 和 studentId 的 hashCode
        return Objects.hash(super.hashCode(), studentId);
    }
}