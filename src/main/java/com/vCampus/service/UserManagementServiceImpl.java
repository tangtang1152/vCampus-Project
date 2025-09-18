package com.vCampus.service;

import com.vCampus.dao.*;
import com.vCampus.entity.*;
import com.vCampus.util.PermissionUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 * 提供统一的用户管理功能
 */
public class UserManagementServiceImpl implements IUserManagementService {
    
    private static final IUserDao userDao = new UserDaoImpl();
    private static final IStudentDao studentDao = new StudentDaoImpl();
    private static final ITeacherDao teacherDao = new TeacherDaoImpl();
    private static final IAdminDao adminDao = new AdminDaoImpl();
    private static final IUserRoleDao userRoleDao = new UserRoleDaoImpl();
    private static final IPermissionService permissionService = ServiceFactory.getPermissionService();
    
    @Override
    public List<User> getAllUsers() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findAll(conn)
            );
        } catch (Exception e) {
            System.err.println("获取所有用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<User> getUsersByRole(String roleCode) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<UserRole> userRoles = userRoleDao.findByRoleCode(roleCode, conn);
                List<User> users = new ArrayList<>();
                
                for (UserRole userRole : userRoles) {
                    if (userRole.isValid()) {
                        User user = userDao.findById(userRole.getUserId(), conn);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                }
                
                return users;
            });
        } catch (Exception e) {
            System.err.println("根据角色获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<User> getUsersByDepartment(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<Teacher> teachers = teacherDao.findByDepartment(departmentId, conn);
                return teachers.stream()
                        .map(teacher -> (User) teacher)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            System.err.println("根据部门获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<User> getUsersByClass(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<Student> students = studentDao.findByClass(className, conn);
                return students.stream()
                        .map(student -> (User) student)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            System.err.println("根据班级获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<User> searchUsers(String keyword) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<User> allUsers = userDao.findAll(conn);
                return allUsers.stream()
                        .filter(user -> 
                            user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                            (user instanceof Student && ((Student) user).getStudentName().toLowerCase().contains(keyword.toLowerCase())) ||
                            (user instanceof Teacher && ((Teacher) user).getTeacherName().toLowerCase().contains(keyword.toLowerCase())) ||
                            (user instanceof Admin && ((Admin) user).getAdminName().toLowerCase().contains(keyword.toLowerCase()))
                        )
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            System.err.println("搜索用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<User> getUsersWithFilters(Map<String, Object> filters) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<User> users = userDao.findAll(conn);
                
                return users.stream()
                        .filter(user -> {
                            // 角色筛选
                            if (filters.containsKey("role")) {
                                String roleCode = (String) filters.get("role");
                                if (!permissionService.hasRole(user.getUserId(), roleCode)) {
                                    return false;
                                }
                            }
                            
                            // 部门筛选
                            if (filters.containsKey("department") && user instanceof Teacher) {
                                String departmentId = (String) filters.get("department");
                                if (!departmentId.equals(((Teacher) user).getDepartmentId())) {
                                    return false;
                                }
                            }
                            
                            // 班级筛选
                            if (filters.containsKey("class") && user instanceof Student) {
                                String className = (String) filters.get("class");
                                if (!className.equals(((Student) user).getClassName())) {
                                    return false;
                                }
                            }
                            
                            // 关键词搜索
                            if (filters.containsKey("keyword")) {
                                String keyword = ((String) filters.get("keyword")).toLowerCase();
                                boolean matches = user.getUsername().toLowerCase().contains(keyword);
                                
                                if (!matches && user instanceof Student) {
                                    matches = ((Student) user).getStudentName().toLowerCase().contains(keyword);
                                } else if (!matches && user instanceof Teacher) {
                                    matches = ((Teacher) user).getTeacherName().toLowerCase().contains(keyword);
                                } else if (!matches && user instanceof Admin) {
                                    matches = ((Admin) user).getAdminName().toLowerCase().contains(keyword);
                                }
                                
                                if (!matches) {
                                    return false;
                                }
                            }
                            
                            return true;
                        })
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            System.err.println("筛选用户失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public User getUserById(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findById(userId, conn)
            );
        } catch (Exception e) {
            System.err.println("根据ID获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public User getUserByUsername(String username) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findByUsername(username, conn)
            );
        } catch (Exception e) {
            System.err.println("根据用户名获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateUser(User user) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.update(user, conn)
            );
        } catch (Exception e) {
            System.err.println("更新用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 先删除用户角色关联
                List<UserRole> userRoles = userRoleDao.findByUserId(userId, conn);
                for (UserRole userRole : userRoles) {
                    userRoleDao.delete(userRole.getUserRoleId(), conn);
                }
                
                // 删除用户信息
                User user = userDao.findById(userId, conn);
                if (user instanceof Student) {
                    studentDao.delete(((Student) user).getStudentId(), conn);
                } else if (user instanceof Teacher) {
                    teacherDao.delete(((Teacher) user).getTeacherId(), conn);
                } else if (user instanceof Admin) {
                    adminDao.delete(((Admin) user).getAdminId(), conn);
                }
                
                // 最后删除用户账户
                return userDao.delete(userId, conn);
            });
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean activateUser(Integer userId) {
        // 这里可以实现用户激活逻辑，比如设置状态字段
        return true;
    }
    
    @Override
    public boolean deactivateUser(Integer userId) {
        // 这里可以实现用户停用逻辑，比如设置状态字段
        return true;
    }
    
    @Override
    public boolean assignRoleToUser(Integer userId, String roleCode, String assignedBy) {
        return permissionService.assignRoleToUser(userId, roleCode, assignedBy);
    }
    
    @Override
    public boolean removeRoleFromUser(Integer userId, String roleCode) {
        return permissionService.removeRoleFromUser(userId, roleCode);
    }
    
    @Override
    public boolean updateUserRoles(Integer userId, List<String> roleCodes, String assignedBy) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 先删除现有角色
                List<UserRole> existingRoles = userRoleDao.findByUserId(userId, conn);
                for (UserRole userRole : existingRoles) {
                    userRoleDao.delete(userRole.getUserRoleId(), conn);
                }
                
                // 添加新角色
                for (String roleCode : roleCodes) {
                    permissionService.assignRoleToUser(userId, roleCode, assignedBy);
                }
                
                return true;
            });
        } catch (Exception e) {
            System.err.println("更新用户角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<UserRole> getUserRoles(Integer userId) {
        return permissionService.getUserRoles(userId);
    }
    
    @Override
    public List<String> getUserRoleCodes(Integer userId) {
        return new ArrayList<>(permissionService.getUserRoleCodes(userId));
    }
    
    @Override
    public boolean batchAssignRoles(List<Integer> userIds, String roleCode, String assignedBy) {
        boolean allSuccess = true;
        for (Integer userId : userIds) {
            if (!assignRoleToUser(userId, roleCode, assignedBy)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean batchRemoveRoles(List<Integer> userIds, String roleCode) {
        boolean allSuccess = true;
        for (Integer userId : userIds) {
            if (!removeRoleFromUser(userId, roleCode)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean batchDeleteUsers(List<Integer> userIds) {
        boolean allSuccess = true;
        for (Integer userId : userIds) {
            if (!deleteUser(userId)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean batchActivateUsers(List<Integer> userIds) {
        boolean allSuccess = true;
        for (Integer userId : userIds) {
            if (!activateUser(userId)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean batchDeactivateUsers(List<Integer> userIds) {
        boolean allSuccess = true;
        for (Integer userId : userIds) {
            if (!deactivateUser(userId)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    @Override
    public boolean exportUsersToCsv(String filePath) {
        // TODO: 实现CSV导出功能
        return false;
    }
    
    @Override
    public boolean importUsersFromCsv(String filePath) {
        // TODO: 实现CSV导入功能
        return false;
    }
    
    @Override
    public boolean exportUsersToExcel(String filePath) {
        // TODO: 实现Excel导出功能
        return false;
    }
    
    @Override
    public boolean importUsersFromExcel(String filePath) {
        // TODO: 实现Excel导入功能
        return false;
    }
    
    @Override
    public Map<String, Integer> getUserStatistics() {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Map<String, Integer> stats = new HashMap<>();
                List<User> allUsers = userDao.findAll(conn);
                
                int studentCount = 0;
                int teacherCount = 0;
                int adminCount = 0;
                
                for (User user : allUsers) {
                    if (user instanceof Student) {
                        studentCount++;
                    } else if (user instanceof Teacher) {
                        teacherCount++;
                    } else if (user instanceof Admin) {
                        adminCount++;
                    }
                }
                
                stats.put("total", allUsers.size());
                stats.put("students", studentCount);
                stats.put("teachers", teacherCount);
                stats.put("admins", adminCount);
                
                return stats;
            });
        } catch (Exception e) {
            System.err.println("获取用户统计信息失败: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Integer> getRoleStatistics() {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Map<String, Integer> stats = new HashMap<>();
                List<UserRole> allUserRoles = userRoleDao.findAll(conn);
                
                for (UserRole userRole : allUserRoles) {
                    if (userRole.isValid()) {
                        String roleCode = userRole.getRoleCode();
                        stats.put(roleCode, stats.getOrDefault(roleCode, 0) + 1);
                    }
                }
                
                return stats;
            });
        } catch (Exception e) {
            System.err.println("获取角色统计信息失败: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Integer> getDepartmentStatistics() {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Map<String, Integer> stats = new HashMap<>();
                List<Teacher> teachers = teacherDao.findAll(conn);
                
                for (Teacher teacher : teachers) {
                    String departmentId = teacher.getDepartmentId();
                    if (departmentId != null && !departmentId.isEmpty()) {
                        stats.put(departmentId, stats.getOrDefault(departmentId, 0) + 1);
                    }
                }
                
                return stats;
            });
        } catch (Exception e) {
            System.err.println("获取部门统计信息失败: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    @Override
    public boolean canManageUser(Integer currentUserId, Integer targetUserId) {
        // 管理员可以管理所有用户
        if (PermissionUtil.hasRole("ADMIN")) {
            return true;
        }
        
        // 教师可以管理学生
        if (PermissionUtil.hasRole("TEACHER")) {
            User targetUser = getUserById(targetUserId);
            return targetUser instanceof Student;
        }
        
        // 用户只能管理自己
        return currentUserId.equals(targetUserId);
    }
    
    @Override
    public boolean canAssignRole(Integer currentUserId, String roleCode) {
        // 只有管理员可以分配角色
        return PermissionUtil.hasRole("ADMIN");
    }
    
    @Override
    public boolean canDeleteUser(Integer currentUserId, Integer targetUserId) {
        // 只有管理员可以删除用户
        return PermissionUtil.hasRole("ADMIN");
    }
}
