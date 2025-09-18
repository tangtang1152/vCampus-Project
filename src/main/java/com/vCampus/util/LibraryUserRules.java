package com.vCampus.util;

import com.vCampus.entity.User;

/**
 * 图书馆用户规则管理类
 * 根据用户类型定义不同的借阅和续借规则
 */
public class LibraryUserRules {
    
    // 用户类型常量
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_ADMIN = "admin";
    
    /**
     * 获取用户的最大借阅数量
     */
    public static int getMaxBorrowCount(User user) {
        if (user == null || user.getRole() == null) {
            return 3; // 默认值
        }
        
        String role = user.getRole().toLowerCase();
        if (role.contains("admin") || role.contains("管理员")) {
            return 10; // 管理员可以借更多书
        } else if (role.contains("teacher") || role.contains("教师")) {
            return 8; // 教师
        } else if (role.contains("student") || role.contains("学生")) {
            return 5; // 学生
        }
        return 3; // 默认值
    }
    
    /**
     * 获取用户的最大续借次数
     */
    public static int getMaxRenewCount(User user) {
        if (user == null || user.getRole() == null) {
            return 1; // 默认值
        }
        
        String role = user.getRole().toLowerCase();
        if (role.contains("admin") || role.contains("管理员")) {
            return 3; // 管理员可以续借3次
        } else if (role.contains("teacher") || role.contains("教师")) {
            return 2; // 教师可以续借2次
        } else if (role.contains("student") || role.contains("学生")) {
            return 1; // 学生只能续借1次
        }
        return 1; // 默认值
    }
    
    /**
     * 获取用户的最大续借天数
     */
    public static int getMaxRenewDays(User user) {
        if (user == null || user.getRole() == null) {
            return 30; // 默认值
        }
        
        String role = user.getRole().toLowerCase();
        if (role.contains("admin") || role.contains("管理员")) {
            return 60; // 管理员可以续借60天
        } else if (role.contains("teacher") || role.contains("教师")) {
            return 45; // 教师可以续借45天
        } else if (role.contains("student") || role.contains("学生")) {
            return 30; // 学生可以续借30天
        }
        return 30; // 默认值
    }
    
    /**
     * 获取用户的最大借阅天数
     */
    public static int getMaxBorrowDays(User user) {
        if (user == null || user.getRole() == null) {
            return 30; // 默认值
        }
        
        String role = user.getRole().toLowerCase();
        if (role.contains("admin") || role.contains("管理员")) {
            return 60; // 管理员可以借60天
        } else if (role.contains("teacher") || role.contains("教师")) {
            return 45; // 教师可以借45天
        } else if (role.contains("student") || role.contains("学生")) {
            return 30; // 学生可以借30天
        }
        return 30; // 默认值
    }
    
    /**
     * 检查用户是否可以续借
     */
    public static boolean canRenew(User user, int currentRenewCount) {
        return currentRenewCount < getMaxRenewCount(user);
    }
    
    /**
     * 获取用户类型的中文描述
     */
    public static String getUserTypeDescription(User user) {
        if (user == null || user.getRole() == null) {
            return "普通用户";
        }
        
        String role = user.getRole().toLowerCase();
        if (role.contains("admin") || role.contains("管理员")) {
            return "管理员";
        } else if (role.contains("teacher") || role.contains("教师")) {
            return "教师";
        } else if (role.contains("student") || role.contains("学生")) {
            return "学生";
        }
        return "普通用户";
    }
}
