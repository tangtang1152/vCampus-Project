package com.vCampus.entity;

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
    
    /** 学号 - 学生的唯一标识符 */
    private String studentId;
    
    private String studentName;
    
    /** 班级名称 - 学生所属的班级 */
    private String className;
    
    // 扩展字段
    private java.util.Date enrollDate; // 入学日期
    private String sex;                // 性别：男/女
    private String email;              // 邮箱
    private String idCard;             // 身份证号
    private String status;             // 学籍状态：正常/休学/退学/毕业
    
    // ==================== 构造函数 ====================
    public Student() {
        super();
        this.studentId = "";
        this.studentName = "";
        this.className = "";
        this.enrollDate = null;
        this.sex = "";
        this.email = "";
        this.idCard = "";
        this.status = "";
    }
    
    public Student(int userId, String username, String password, String role,
                   String studentId, String studentName, String className) {
        super(userId, username, password, role);
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
    }
    
    // ==================== Getter 方法 ====================
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public java.util.Date getEnrollDate() { return enrollDate; }
    public String getSex() { return sex; }
    public String getEmail() { return email; }
    public String getIdCard() { return idCard; }
    public String getStatus() { return status; }
    
    // ==================== Setter 方法 ====================
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setClassName(String className) { this.className = className; }
    public void setEnrollDate(java.util.Date enrollDate) { this.enrollDate = enrollDate; }
    public void setSex(String sex) { this.sex = sex; }
    public void setEmail(String email) { this.email = email; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public void setStatus(String status) { this.status = status; }
    
    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", enrollDate=" + enrollDate +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", idCard='" + idCard + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId != null ? studentId.equals(student.studentId) : student.studentId == null;
    }

    @Override
    public int hashCode() {
        return studentId != null ? studentId.hashCode() : 0;
    }
}