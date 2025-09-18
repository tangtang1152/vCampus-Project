package com.vCampus.entity;

import java.util.Date;

/**
 * 用户角色关联实体类
 * 支持用户拥有多个角色
 */
public class UserRole {
    private Integer userRoleId;
    private Integer userId;
    private Integer roleId;
    private String roleCode;
    private Date assignedDate;
    private Date expireDate;
    private Boolean isActive;
    private String assignedBy;
    
    // 构造函数
    public UserRole() {
        this.isActive = true;
        this.assignedDate = new Date();
    }
    
    public UserRole(Integer userId, Integer roleId, String roleCode) {
        this();
        this.userId = userId;
        this.roleId = roleId;
        this.roleCode = roleCode;
    }
    
    public UserRole(Integer userId, Integer roleId, String roleCode, Date expireDate, String assignedBy) {
        this(userId, roleId, roleCode);
        this.expireDate = expireDate;
        this.assignedBy = assignedBy;
    }
    
    // Getter 和 Setter 方法
    public Integer getUserRoleId() {
        return userRoleId;
    }
    
    public void setUserRoleId(Integer userRoleId) {
        this.userRoleId = userRoleId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
    
    public Date getAssignedDate() {
        return assignedDate;
    }
    
    public void setAssignedDate(Date assignedDate) {
        this.assignedDate = assignedDate;
    }
    
    public Date getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    /**
     * 检查角色是否有效（未过期且激活）
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        if (expireDate != null && expireDate.before(new Date())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "UserRole{" +
                "userRoleId=" + userRoleId +
                ", userId=" + userId +
                ", roleId=" + roleId +
                ", roleCode='" + roleCode + '\'' +
                ", assignedDate=" + assignedDate +
                ", expireDate=" + expireDate +
                ", isActive=" + isActive +
                ", assignedBy='" + assignedBy + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return userRoleId != null ? userRoleId.equals(userRole.userRoleId) : userRole.userRoleId == null;
    }
    
    @Override
    public int hashCode() {
        return userRoleId != null ? userRoleId.hashCode() : 0;
    }
}
