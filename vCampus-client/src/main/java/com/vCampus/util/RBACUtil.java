package com.vCampus.util;

import com.vCampus.common.SessionContext;
import com.vCampus.entity.User;

import java.util.Locale;

/**
 * 基于简单角色字符串的 RBAC 工具类。
 * 为尽量少改动，沿用已有 User.role 文本，支持多角色并集与激活角色。
 */
public final class RBACUtil {

    private RBACUtil() {}

    // 标准化：ADMIN/TEACHER/STUDENT
    private static boolean hasRole(User user, String role) {
        if (user == null || role == null) return false;
        String target = role.trim().toUpperCase(Locale.ROOT);
        for (String r : user.getRoleSet()) {
            if (target.equals(r.toUpperCase(Locale.ROOT))) return true;
        }
        return false;
    }

    public static boolean isAdmin(User u) { return hasRole(u, "ADMIN"); }
    public static boolean isTeacher(User u) { return hasRole(u, "TEACHER"); }
    public static boolean isStudent(User u) { return hasRole(u, "STUDENT"); }

    // 示例权限域：系统用户管理、课程管理、图书馆浏览、图书维护
    public static boolean canManageUsers(User u) { return isAdmin(u); }
    public static boolean canManageCourses(User u) { return isTeacher(u) || isAdmin(u); }
    public static boolean canUseLibrary(User u) { return isStudent(u) || isTeacher(u) || isAdmin(u); }
    public static boolean canMaintainLibrary(User u) { return isAdmin(u); }

    /**
     * 当前登录用户是否具备权限
     */
    public static boolean currentUserCan(java.util.function.Function<User, Boolean> predicate) {
        User u = SessionContext.getCurrentUser();
        return u != null && Boolean.TRUE.equals(predicate.apply(u));
    }
}


