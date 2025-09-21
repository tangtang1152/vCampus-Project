package com.vCampus.entity;

/**
 * 部门实体类
 * 表示系统中的部门信息，包含部门的基本属性
 */
public class Department {
    private String departmentId;      // 部门编号（主键，如 "D001"）
    private String departmentName;    // 部门名称

    // ==================== 构造函数 ====================
    public Department() {
        this.departmentId = "";
        this.departmentName = "";
    }

    public Department(String departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    // ==================== Getter 方法 ====================
    public String getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    // ==================== Setter 方法 ====================
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Department{" +
                "departmentId='" + departmentId + '\'' +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return departmentId.equals(that.departmentId);
    }

    @Override
    public int hashCode() {
        return departmentId.hashCode();
    }
}