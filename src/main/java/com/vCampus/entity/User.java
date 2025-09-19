package com.vCampus.entity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户基类，是所有用户的父类
 * 包含登录认证相关的基本信息
 */
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String role;
    
    // 构造函数
    public User() {}
    
    public User(Integer userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getter 和 Setter 方法
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
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
    
    /**
     * 返回以插入顺序去重的角色集合（按逗号/分号/空白分隔）。
     */
    public Set<String> getRoleSet() {
        LinkedHashSet<String> roles = new LinkedHashSet<>();
        if (role == null || role.isBlank()) return roles;
        String[] parts = role.split("[,;\\s]+");
        for (String part : parts) {
            if (part == null) continue;
            String token = part.trim();
            if (token.isEmpty()) continue;
            String normalized = token.toUpperCase(java.util.Locale.ROOT);
            // 兼容：既支持短码 S/T/A，也支持全称 STUDENT/TEACHER/ADMIN
            switch (normalized) {
                case "S": roles.add("STUDENT"); break;
                case "T": roles.add("TEACHER"); break;
                case "A": roles.add("ADMIN"); break;
                default: roles.add(normalized);
            }
        }
        return roles;
    }

    /**
     * 以逗号拼接的形式设置角色集合，自动去空白与重复。
     */
    public void setRoleSet(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            this.role = null;
            return;
        }
        // 存储压缩为短码（S/T/A），避免超出数据库列长度
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        for (String r : roles) {
            if (r == null) continue;
            String token = r.trim();
            if (token.isEmpty()) continue;
            String upper = token.toUpperCase(java.util.Locale.ROOT);
            if ("STUDENT".equals(upper) || "S".equals(upper)) codes.add("S");
            else if ("TEACHER".equals(upper) || "T".equals(upper)) codes.add("T");
            else if ("ADMIN".equals(upper) || "A".equals(upper)) codes.add("A");
            else codes.add(upper); // 兼容未知自定义角色
        }
        this.role = String.join(",", codes);
    }

    /**
     * 返回首个角色（如果存在）。
     */
    public String getPrimaryRole() {
        for (String r : getRoleSet()) {
            return r;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}