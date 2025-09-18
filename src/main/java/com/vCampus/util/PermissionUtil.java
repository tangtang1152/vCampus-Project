package com.vCampus.util;

import com.vCampus.common.SessionContext;
import com.vCampus.entity.Permission;
import com.vCampus.entity.Role;
import com.vCampus.entity.User;
import com.vCampus.entity.UserRole;
import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限工具类
 * 提供权限检查和角色管理的便捷方法
 */
public class PermissionUtil {
    
    private static final IPermissionService permissionService = ServiceFactory.getPermissionService();
    
    /**
     * 检查当前用户是否有指定权限
     */
    public static boolean hasPermission(String permissionCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return permissionService.hasPermission(currentUser.getUserId(), permissionCode);
    }
    
    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String roleCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return permissionService.hasRole(currentUser.getUserId(), roleCode);
    }
    
    /**
     * 检查当前用户是否有任意一个指定角色
     */
    public static boolean hasAnyRole(String... roleCodes) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return permissionService.hasAnyRole(currentUser.getUserId(), roleCodes);
    }
    
    /**
     * 检查当前用户是否有所有指定角色
     */
    public static boolean hasAllRoles(String... roleCodes) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return permissionService.hasAllRoles(currentUser.getUserId(), roleCodes);
    }
    
    /**
     * 获取当前用户的所有权限代码
     */
    public static Set<String> getCurrentUserPermissions() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return new HashSet<>();
        }
        return permissionService.getUserPermissionCodes(currentUser.getUserId());
    }
    
    /**
     * 获取当前用户的所有角色代码
     */
    public static Set<String> getCurrentUserRoles() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return new HashSet<>();
        }
        return permissionService.getUserRoleCodes(currentUser.getUserId());
    }
    
    /**
     * 检查当前用户是否可以访问指定资源
     */
    public static boolean canAccess(String resource, String action) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return permissionService.validateAccess(resource, action, currentUser.getUserId());
    }
    
    /**
     * 检查用户是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN_ROLE);
    }
    
    /**
     * 检查用户是否为教师
     */
    public static boolean isTeacher() {
        return hasRole(Role.TEACHER_ROLE);
    }
    
    /**
     * 检查用户是否为学生
     */
    public static boolean isStudent() {
        return hasRole(Role.STUDENT_ROLE);
    }
    
    /**
     * 检查用户是否为教师或管理员
     */
    public static boolean isTeacherOrAdmin() {
        return hasAnyRole(Role.TEACHER_ROLE, Role.ADMIN_ROLE);
    }
    
    /**
     * 检查用户是否可以管理用户
     */
    public static boolean canManageUsers() {
        return hasPermission(Permission.USER_MANAGE_ROLES);
    }
    
    /**
     * 检查用户是否可以管理学生
     */
    public static boolean canManageStudents() {
        return hasPermission(Permission.STUDENT_MANAGE);
    }
    
    /**
     * 检查用户是否可以管理教师
     */
    public static boolean canManageTeachers() {
        return hasPermission(Permission.TEACHER_MANAGE);
    }
    
    /**
     * 检查用户是否可以管理图书馆
     */
    public static boolean canManageLibrary() {
        return hasPermission(Permission.LIBRARY_MANAGE);
    }
    
    /**
     * 检查用户是否可以管理商店
     */
    public static boolean canManageShop() {
        return hasPermission(Permission.SHOP_MANAGE);
    }
    
    /**
     * 检查用户是否可以访问系统管理
     */
    public static boolean canAccessSystemAdmin() {
        return hasPermission(Permission.SYSTEM_ADMIN);
    }
    
    /**
     * 获取用户可访问的菜单项
     */
    public static List<String> getAccessibleMenus() {
        List<String> menus = new ArrayList<>();
        
        // 基础菜单（所有用户都可以访问）
        menus.add("用户管理");
        
        // 根据权限添加菜单
        if (canManageStudents()) {
            menus.add("学生管理");
        }
        
        if (canManageTeachers()) {
            menus.add("教师管理");
        }
        
        if (hasPermission(Permission.LIBRARY_VIEW)) {
            menus.add("图书馆");
        }
        
        if (hasPermission(Permission.LIBRARY_ADMIN)) {
            menus.add("图书维护(管理员)");
        }
        
        if (hasPermission(Permission.SHOP_VIEW)) {
            menus.add("商店");
        }
        
        if (hasPermission(Permission.SHOP_ADMIN)) {
            menus.add("商品管理");
        }
        
        if (canAccessSystemAdmin()) {
            menus.add("系统管理");
        }
        
        return menus;
    }
    
    /**
     * 根据角色获取默认权限
     */
    public static Set<String> getDefaultPermissionsForRole(String roleCode) {
        Set<String> permissions = new HashSet<>();
        
        switch (roleCode) {
            case Role.STUDENT_ROLE:
                permissions.add(Permission.USER_VIEW);
                permissions.add(Permission.STUDENT_VIEW);
                permissions.add(Permission.LIBRARY_VIEW);
                permissions.add(Permission.LIBRARY_BORROW);
                permissions.add(Permission.LIBRARY_RETURN);
                permissions.add(Permission.SHOP_VIEW);
                permissions.add(Permission.SHOP_PURCHASE);
                break;
                
            case Role.TEACHER_ROLE:
                permissions.add(Permission.USER_VIEW);
                permissions.add(Permission.STUDENT_VIEW);
                permissions.add(Permission.TEACHER_VIEW);
                permissions.add(Permission.LIBRARY_VIEW);
                permissions.add(Permission.LIBRARY_BORROW);
                permissions.add(Permission.LIBRARY_RETURN);
                permissions.add(Permission.SHOP_VIEW);
                permissions.add(Permission.SHOP_PURCHASE);
                permissions.add(Permission.STUDENT_MANAGE);
                break;
                
            case Role.ADMIN_ROLE:
                // 管理员拥有所有权限
                permissions.addAll(Arrays.asList(
                    Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, 
                    Permission.USER_DELETE, Permission.USER_MANAGE_ROLES,
                    Permission.STUDENT_VIEW, Permission.STUDENT_CREATE, Permission.STUDENT_UPDATE, 
                    Permission.STUDENT_DELETE, Permission.STUDENT_MANAGE,
                    Permission.TEACHER_VIEW, Permission.TEACHER_CREATE, Permission.TEACHER_UPDATE, 
                    Permission.TEACHER_DELETE, Permission.TEACHER_MANAGE,
                    Permission.LIBRARY_VIEW, Permission.LIBRARY_MANAGE, Permission.LIBRARY_ADMIN,
                    Permission.SHOP_VIEW, Permission.SHOP_MANAGE, Permission.SHOP_ADMIN,
                    Permission.SYSTEM_ADMIN, Permission.SYSTEM_CONFIG, Permission.SYSTEM_LOG
                ));
                break;
        }
        
        return permissions;
    }
    
    /**
     * 初始化用户角色（用于新用户注册时）
     */
    public static boolean initializeUserRole(Integer userId, String roleCode) {
        User currentUser = SessionContext.getCurrentUser();
        String assignedBy = currentUser != null ? currentUser.getUsername() : "system";
        return permissionService.assignRoleToUser(userId, roleCode, assignedBy);
    }
    
    /**
     * 检查权限并抛出异常（用于API接口级权限验证）
     */
    public static void requirePermission(String permissionCode) throws SecurityException {
        if (!hasPermission(permissionCode)) {
            throw new SecurityException("权限不足: 需要权限 " + permissionCode);
        }
    }
    
    /**
     * 检查角色并抛出异常（用于API接口级权限验证）
     */
    public static void requireRole(String roleCode) throws SecurityException {
        if (!hasRole(roleCode)) {
            throw new SecurityException("权限不足: 需要角色 " + roleCode);
        }
    }
    
    /**
     * 检查任意角色并抛出异常（用于API接口级权限验证）
     */
    public static void requireAnyRole(String... roleCodes) throws SecurityException {
        if (!hasAnyRole(roleCodes)) {
            throw new SecurityException("权限不足: 需要角色 " + Arrays.toString(roleCodes));
        }
    }
}
