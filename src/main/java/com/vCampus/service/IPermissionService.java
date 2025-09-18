package com.vCampus.service;

import com.vCampus.entity.Permission;
import com.vCampus.entity.Role;
import com.vCampus.entity.User;
import com.vCampus.entity.UserRole;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 * 提供基于角色的访问控制功能
 */
public interface IPermissionService {
    
    // 角色管理
    List<Role> getAllRoles();
    Role getRoleById(Integer roleId);
    Role getRoleByCode(String roleCode);
    boolean createRole(Role role);
    boolean updateRole(Role role);
    boolean deleteRole(Integer roleId);
    
    // 权限管理
    List<Permission> getAllPermissions();
    Permission getPermissionById(Integer permissionId);
    Permission getPermissionByCode(String permissionCode);
    boolean createPermission(Permission permission);
    boolean updatePermission(Permission permission);
    boolean deletePermission(Integer permissionId);
    
    // 角色权限管理
    boolean assignPermissionToRole(Integer roleId, Integer permissionId);
    boolean removePermissionFromRole(Integer roleId, Integer permissionId);
    Set<Permission> getRolePermissions(Integer roleId);
    Set<Permission> getRolePermissionsByCode(String roleCode);
    
    // 用户角色管理
    boolean assignRoleToUser(Integer userId, Integer roleId, String assignedBy);
    boolean assignRoleToUser(Integer userId, String roleCode, String assignedBy);
    boolean removeRoleFromUser(Integer userId, Integer roleId);
    boolean removeRoleFromUser(Integer userId, String roleCode);
    List<UserRole> getUserRoles(Integer userId);
    List<UserRole> getActiveUserRoles(Integer userId);
    Set<String> getUserRoleCodes(Integer userId);
    
    // 权限检查
    boolean hasPermission(Integer userId, String permissionCode);
    boolean hasRole(Integer userId, String roleCode);
    boolean hasAnyRole(Integer userId, String... roleCodes);
    boolean hasAllRoles(Integer userId, String... roleCodes);
    
    // 权限计算
    Set<Permission> getUserPermissions(Integer userId);
    Set<String> getUserPermissionCodes(Integer userId);
    
    // 角色权限初始化
    void initializeDefaultRolesAndPermissions();
    
    // 权限验证
    boolean validateAccess(String resource, String action, Integer userId);
}
