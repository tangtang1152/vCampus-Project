package com.vCampus.common;

import com.vCampus.entity.User;

/**
 * 简单会话上下文：进程内保存当前登录用户。
 * 非分布式、单客户端示例场景足够使用。
 */
public final class SessionContext {
    private static volatile User currentUser;

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
    }
}


