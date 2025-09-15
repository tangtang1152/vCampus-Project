package com.vCampus.entity;

/**
 * 用户基类
 * 包含所有用户的共同属性
 */
public class User {
    private int userId;        // 用户ID
    private String username;   // 用户名
    private String password;   // 密码
    private String role;       // 角色
    
    // ==================== 构造函数 ====================
    
    public User() {
        this.userId = 0;
        this.username = "";
        this.password = "";
        this.role = "";
    }
    
    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // ==================== 辅助方法 ====================
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        return userId == user.userId;
    }
    
    @Override
    public int hashCode() {
        return userId;
    }
}