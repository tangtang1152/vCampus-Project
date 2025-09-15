
package com.vCampus.entity;

/**
 * 教师实体类
 * 表示系统中的教师信息，包含教师的基本属性
 */
public class Teacher extends User {
    private String teacherId;    // 教师编号（主键，如 "T2024001"）
    private String teacherName;  // 教师姓名
    private String sex;          // 性别
    private String technical;    // 职称（如 "讲师 / 教授"）
    private String departmentId; // 所属部门（外键，关联 Department.deptid）

    // ==================== 构造函数 ====================
    public Teacher() {
        super();
        this.teacherId = "";
        this.teacherName = "";
        this.sex = "";
        this.technical = "";
        this.departmentId = "";
    }

    public Teacher(Integer userId, String username, String password, String role,
                  String teacherId, String teacherName, String sex, 
                  String technical, String departmentId) {
        super(userId, username, password, role);
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.sex = sex;
        this.technical = technical;
        this.departmentId = departmentId;
    }

    // ==================== Getter 方法 ====================
    public String getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getSex() {
        return sex;
    }

    public String getTechnical() {
        return technical;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    // ==================== Setter 方法 ====================
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setTechnical(String technical) {
        this.technical = technical;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId='" + teacherId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", sex='" + sex + '\'' +
                ", technical='" + technical + '\'' +
                ", departmentId='" + departmentId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return teacherId.equals(teacher.teacherId);
    }

    @Override
    public int hashCode() {
        return teacherId.hashCode();
    }
}
