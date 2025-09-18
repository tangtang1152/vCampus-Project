package com.vCampus.entity;

import java.util.Set;
import java.util.HashSet;

/**
 * 角色实体类
 * 定义系统中的角色类型和权限
 */
public class Role {
    private Integer roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Set<Permission> permissions;
    
    // 预定义角色常量
    public static final String STUDENT_ROLE = "STUDENT";
    public static final String TEACHER_ROLE = "TEACHER";
    public static final String ADMIN_ROLE = "ADMIN";
    
    // 构造函数
    public Role() {
        this.permissions = new HashSet<>();
    }
    
    public Role(Integer roleId, String roleName, String roleCode, String description) {
        this();
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.description = description;
    }
    
    // Getter 和 Setter 方法
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Set<Permission> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }
    
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }
    
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getPermissionCode().equals(permissionCode));
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleCode='" + roleCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return roleId != null ? roleId.equals(role.roleId) : role.roleId == null;
    }
    
    @Override
    public int hashCode() {
        return roleId != null ? roleId.hashCode() : 0;
    }
}
