package com.vCampus.common;

import com.vCampus.entity.User;
import com.vCampus.entity.UserRole;
import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色切换器
 * 支持用户在不同角色之间切换
 */
public class RoleSwitcher {
    
    private static volatile String currentActiveRole;
    private static final IPermissionService permissionService = ServiceFactory.getPermissionService();
    
    /**
     * 获取当前活跃角色
     */
    public static String getCurrentActiveRole() {
        if (currentActiveRole == null) {
            // 如果没有设置活跃角色，使用用户的第一个角色
            User currentUser = SessionContext.getCurrentUser();
            if (currentUser != null) {
                Set<String> userRoles = permissionService.getUserRoleCodes(currentUser.getUserId());
                if (!userRoles.isEmpty()) {
                    currentActiveRole = userRoles.iterator().next();
                }
            }
        }
        return currentActiveRole;
    }
    
    /**
     * 设置当前活跃角色
     */
    public static boolean setCurrentActiveRole(String roleCode) {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // 检查用户是否拥有该角色
        if (permissionService.hasRole(currentUser.getUserId(), roleCode)) {
            currentActiveRole = roleCode;
            return true;
        }
        return false;
    }
    
    /**
     * 获取用户的所有可用角色
     */
    public static List<String> getAvailableRoles() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        
        return permissionService.getUserRoleCodes(currentUser.getUserId())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的所有角色信息
     */
    public static List<UserRole> getUserRoles() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        
        return permissionService.getActiveUserRoles(currentUser.getUserId());
    }
    
    /**
     * 检查当前活跃角色是否有指定权限
     */
    public static boolean hasPermissionInCurrentRole(String permissionCode) {
        String activeRole = getCurrentActiveRole();
        if (activeRole == null) {
            return false;
        }
        
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // 检查用户是否拥有该权限（通过任何角色）
        return permissionService.hasPermission(currentUser.getUserId(), permissionCode);
    }
    
    /**
     * 获取当前活跃角色的权限
     */
    public static Set<String> getCurrentRolePermissions() {
        String activeRole = getCurrentActiveRole();
        if (activeRole == null) {
            return Set.of();
        }
        
        return permissionService.getRolePermissionsByCode(activeRole)
                .stream()
                .map(permission -> permission.getPermissionCode())
                .collect(Collectors.toSet());
    }
    
    /**
     * 获取用户所有角色的合并权限
     */
    public static Set<String> getAllUserPermissions() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return Set.of();
        }
        
        return permissionService.getUserPermissionCodes(currentUser.getUserId());
    }
    
    /**
     * 重置角色切换器（用户登出时调用）
     */
    public static void reset() {
        currentActiveRole = null;
    }
    
    /**
     * 初始化角色切换器（用户登录时调用）
     */
    public static void initialize() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            Set<String> userRoles = permissionService.getUserRoleCodes(currentUser.getUserId());
            if (!userRoles.isEmpty()) {
                // 优先选择管理员角色，然后是教师角色，最后是学生角色
                if (userRoles.contains("ADMIN")) {
                    currentActiveRole = "ADMIN";
                } else if (userRoles.contains("TEACHER")) {
                    currentActiveRole = "TEACHER";
                } else if (userRoles.contains("STUDENT")) {
                    currentActiveRole = "STUDENT";
                } else {
                    currentActiveRole = userRoles.iterator().next();
                }
            }
        }
    }
    
    /**
     * 检查是否可以切换到指定角色
     */
    public static boolean canSwitchToRole(String roleCode) {
        return getAvailableRoles().contains(roleCode);
    }
    
    /**
     * 获取角色显示名称
     */
    public static String getRoleDisplayName(String roleCode) {
        switch (roleCode) {
            case "STUDENT":
                return "学生";
            case "TEACHER":
                return "教师";
            case "ADMIN":
                return "管理员";
            default:
                return roleCode;
        }
    }
    
    /**
     * 获取当前活跃角色的显示名称
     */
    public static String getCurrentRoleDisplayName() {
        String activeRole = getCurrentActiveRole();
        return activeRole != null ? getRoleDisplayName(activeRole) : "未知";
    }
}
