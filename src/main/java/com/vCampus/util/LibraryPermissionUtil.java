package com.vCampus.util;

import com.vCampus.entity.User;

/**
 * 图书馆权限验证工具类
 */
public class LibraryPermissionUtil {

    /**
     * 检查用户是否有图书查询权限
     */
    public static boolean canSearchBooks(User user) {
        return user != null && (isStudent(user) || isAdmin(user));
    }

    /**
     * 检查用户是否有借书权限
     */
    public static boolean canBorrowBooks(User user) {
        return user != null && (isStudent(user) || isAdmin(user));
    }

    /**
     * 检查用户是否有还书权限
     */
    public static boolean canReturnBooks(User user) {
        return user != null && (isStudent(user) || isAdmin(user));
    }

    /**
     * 检查用户是否有续借权限
     */
    public static boolean canRenewBooks(User user) {
        return user != null && (isStudent(user) || isAdmin(user));
    }

    /**
     * 检查用户是否有预约权限
     */
    public static boolean canReserveBooks(User user) {
        return user != null && (isStudent(user) || isAdmin(user));
    }

    /**
     * 检查用户是否有图书管理权限（仅管理员）
     */
    public static boolean canManageBooks(User user) {
        return user != null && isAdmin(user);
    }

    /**
     * 检查用户是否有借阅管理权限（仅管理员）
     */
    public static boolean canManageBorrows(User user) {
        return user != null && isAdmin(user);
    }

    /**
     * 检查用户是否是学生
     */
    private static boolean isStudent(User user) {
        return "STUDENT".equalsIgnoreCase(user.getRole());
    }

    /**
     * 检查用户是否是管理员
     */
    private static boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * 检查用户是否是教师
     */
    private static boolean isTeacher(User user) {
        return "TEACHER".equalsIgnoreCase(user.getRole());
    }
}