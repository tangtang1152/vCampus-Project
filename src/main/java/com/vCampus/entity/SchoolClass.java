package com.vCampus.entity;

/**
 * 班级实体类
 * 表示系统中的班级信息，包含班级的基本属性和所属部门信息
 */
public class SchoolClass {
    private String classId;          // 班级编号（主键，如 "C2024001"）
    private String className;        // 班级名称
    private String departmentId;     // 所属部门（外键，关联 Department.departmentId）

    // ==================== 构造函数 ====================
    public SchoolClass() {
        this.classId = "";
        this.className = "";
        this.departmentId = "";
    }

    public SchoolClass(String classId, String className, String departmentId) {
        this.classId = classId;
        this.className = className;
        this.departmentId = departmentId;
    }

    // ==================== Getter 方法 ====================
    public String getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    // ==================== Setter 方法 ====================
    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "SchoolClass{" +
                "classId='" + classId + '\'' +
                ", className='" + className + '\'' +
                ", departmentId='" + departmentId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchoolClass that = (SchoolClass) o;
        return classId.equals(that.classId);
    }

    @Override
    public int hashCode() {
        return classId.hashCode();
    }
}