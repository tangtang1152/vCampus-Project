package com.vCampus.entity;

/**
 * 管理员实体类
 * 表示系统中的管理员信息，包含管理员的基本属性
 */
public class Admin extends User {
    private String adminId;      // 管理员工号（主键）
    private String adminName;    // 管理员真实姓名

    // ==================== 构造函数 ====================
    public Admin() {
        super();
        this.adminId = "";
        this.adminName = "";
    }

    public Admin(Integer userId, String username, String password, String role,
                String adminId, String adminName) {
        super(userId, username, password, role);
        this.adminId = adminId;
        this.adminName = adminName;
    }

    // ==================== Getter 方法 ====================
    public String getAdminId() {
        return adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    // ==================== Setter 方法 ====================
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    // ==================== 辅助方法 ====================
    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", adminName='" + adminName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Admin admin = (Admin) o;
        return adminId.equals(admin.adminId);
    }

    @Override
    public int hashCode() {
        return adminId.hashCode();
    }
}