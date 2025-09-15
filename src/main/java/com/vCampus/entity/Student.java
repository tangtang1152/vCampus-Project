package com.vCampus.entity;

public class Student extends User {
    // 学生表核心属性（与数据库表结构严格一致）
    private String studentId;  // 学生主键（如：2024001）
    private String studentName; // 学生姓名（与User的userName保持一致）
    private String classId;     // 关联CourseClass表的班级ID

    // 无参构造（满足DAO层映射需求）
    public Student() {
        super();
    }

    // 有参构造（仅初始化Student表自身字段，User属性通过setter设置）
    public Student(String studentId, Integer userId, String studentName, String classId) {
        // 调用父类构造：userId关联User表，userName与studentName同步，password暂存null（后续通过UserDAO设置）
        super(userId, studentName, null, "student"); 
        this.studentId = studentId;
        this.studentName = studentName;
        this.classId = classId;
    }

    // Getter & Setter（仅针对Student自身字段）
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
        // 同步更新父类userName：确保学生姓名与用户名一致（业务约束）
        super.setUserName(studentName);
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    // 优化toString：删除email引用，展示完整关联关系
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", userId=" + getUserId() +  // 继承自User的字段
                ", studentName='" + studentName + '\'' +
                ", classId='" + classId + '\'' +
                ", role='" + getRole() + '\'' +  // 继承自User的角色（固定为student）
                ", userName（继承）='" + (getUserName() != null ? getUserName() : "未设置") + '\'' +
                ", password（继承）='" + (getPassword() != null ? "******" : "未设置") + '\'' +  // 密码脱敏
                '}';
    }
}