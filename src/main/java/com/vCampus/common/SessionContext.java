package com.vCampus.common;

import com.vCampus.entity.User;

/**
 * 简单会话上下文：进程内保存当前登录用户。
 * 非分布式、单客户端示例场景足够使用。
 */
public final class SessionContext {
    private static volatile User currentUser;
<<<<<<< HEAD
    private static volatile String activeRole; // 当前激活角色（用于多角色切换）

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
        // 默认激活首个角色
        if (user != null) {
            activeRole = user.getPrimaryRole();
        } else {
            activeRole = null;
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Integer requireCurrentUserId() {
        if (currentUser == null) return null;
        return currentUser.getUserId();
    }

    public static void clear() {
        currentUser = null;
        activeRole = null;
    }

    /**
     * 获取当前激活角色；若未显式设置则回退到用户主角色。
     */
    public static String getActiveRole() {
        if (activeRole != null && !activeRole.isBlank()) return activeRole;
        return currentUser == null ? null : currentUser.getPrimaryRole();
    }

    /**
     * 设置当前激活角色（需属于用户角色集合）。
     */
    public static boolean setActiveRole(String role) {
        if (currentUser == null) return false;
        if (role == null || role.isBlank()) {
            activeRole = currentUser.getPrimaryRole();
            return true;
        }
        if (currentUser.getRoleSet().contains(role)) {
            activeRole = role;
            return true;
        }
        return false;
=======

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Integer requireCurrentUserId() {
        if (currentUser == null) return null;
        return currentUser.getUserId();
    }

    public static void clear() {
        currentUser = null;
>>>>>>> refs/heads/feature/course-selection-finalllll
    }
}


