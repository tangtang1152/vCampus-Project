package com.vCampus.service;

import com.vCampus.entity.User;
import com.vCampus.entity.UserRole;

import java.util.List;
import java.util.Map;

/**
 * 用户管理服务接口
 * 提供统一的用户管理功能
 */
public interface IUserManagementService {
    
    // 用户查询和筛选
    List<User> getAllUsers();
    List<User> getUsersByRole(String roleCode);
    List<User> getUsersByDepartment(String departmentId);
    List<User> getUsersByClass(String className);
    List<User> searchUsers(String keyword);
    List<User> getUsersWithFilters(Map<String, Object> filters);
    
    // 用户信息管理
    User getUserById(Integer userId);
    User getUserByUsername(String username);
    boolean updateUser(User user);
    boolean deleteUser(Integer userId);
    boolean activateUser(Integer userId);
    boolean deactivateUser(Integer userId);
    
    // 角色管理
    boolean assignRoleToUser(Integer userId, String roleCode, String assignedBy);
    boolean removeRoleFromUser(Integer userId, String roleCode);
    boolean updateUserRoles(Integer userId, List<String> roleCodes, String assignedBy);
    List<UserRole> getUserRoles(Integer userId);
    List<String> getUserRoleCodes(Integer userId);
    
    // 批量操作
    boolean batchAssignRoles(List<Integer> userIds, String roleCode, String assignedBy);
    boolean batchRemoveRoles(List<Integer> userIds, String roleCode);
    boolean batchDeleteUsers(List<Integer> userIds);
    boolean batchActivateUsers(List<Integer> userIds);
    boolean batchDeactivateUsers(List<Integer> userIds);
    
    // 导入导出
    boolean exportUsersToCsv(String filePath);
    boolean importUsersFromCsv(String filePath);
    boolean exportUsersToExcel(String filePath);
    boolean importUsersFromExcel(String filePath);
    
    // 统计信息
    Map<String, Integer> getUserStatistics();
    Map<String, Integer> getRoleStatistics();
    Map<String, Integer> getDepartmentStatistics();
    
    // 权限检查
    boolean canManageUser(Integer currentUserId, Integer targetUserId);
    boolean canAssignRole(Integer currentUserId, String roleCode);
    boolean canDeleteUser(Integer currentUserId, Integer targetUserId);
}
