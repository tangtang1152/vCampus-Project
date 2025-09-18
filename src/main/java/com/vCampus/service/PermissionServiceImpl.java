package com.vCampus.service;

import com.vCampus.dao.*;
import com.vCampus.entity.*;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 提供基于角色的访问控制功能
 */
public class PermissionServiceImpl implements IPermissionService {
    
    private static final IRoleDao roleDao = new RoleDaoImpl();
    private static final IPermissionDao permissionDao = new PermissionDaoImpl();
    private static final IUserRoleDao userRoleDao = new UserRoleDaoImpl();
    
    @Override
    public List<Role> getAllRoles() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.findAll(conn)
            );
        } catch (Exception e) {
            System.err.println("获取所有角色失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public Role getRoleById(Integer roleId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.findById(roleId, conn)
            );
        } catch (Exception e) {
            System.err.println("根据ID获取角色失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Role getRoleByCode(String roleCode) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.findByCode(roleCode, conn)
            );
        } catch (Exception e) {
            System.err.println("根据代码获取角色失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean createRole(Role role) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.insert(role, conn)
            );
        } catch (Exception e) {
            System.err.println("创建角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateRole(Role role) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.update(role, conn)
            );
        } catch (Exception e) {
            System.err.println("更新角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteRole(Integer roleId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.delete(roleId, conn)
            );
        } catch (Exception e) {
            System.err.println("删除角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Permission> getAllPermissions() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.findAll(conn)
            );
        } catch (Exception e) {
            System.err.println("获取所有权限失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public Permission getPermissionById(Integer permissionId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.findById(permissionId, conn)
            );
        } catch (Exception e) {
            System.err.println("根据ID获取权限失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Permission getPermissionByCode(String permissionCode) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.findByCode(permissionCode, conn)
            );
        } catch (Exception e) {
            System.err.println("根据代码获取权限失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean createPermission(Permission permission) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.insert(permission, conn)
            );
        } catch (Exception e) {
            System.err.println("创建权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updatePermission(Permission permission) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.update(permission, conn)
            );
        } catch (Exception e) {
            System.err.println("更新权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deletePermission(Integer permissionId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                permissionDao.delete(permissionId, conn)
            );
        } catch (Exception e) {
            System.err.println("删除权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean assignPermissionToRole(Integer roleId, Integer permissionId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.assignPermission(roleId, permissionId, conn)
            );
        } catch (Exception e) {
            System.err.println("分配权限到角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean removePermissionFromRole(Integer roleId, Integer permissionId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.removePermission(roleId, permissionId, conn)
            );
        } catch (Exception e) {
            System.err.println("从角色移除权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Set<Permission> getRolePermissions(Integer roleId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                roleDao.getRolePermissions(roleId, conn)
            );
        } catch (Exception e) {
            System.err.println("获取角色权限失败: " + e.getMessage());
            e.printStackTrace();
            return new HashSet<>();
        }
    }
    
    @Override
    public Set<Permission> getRolePermissionsByCode(String roleCode) {
        Role role = getRoleByCode(roleCode);
        if (role != null) {
            return getRolePermissions(role.getRoleId());
        }
        return new HashSet<>();
    }
    
    @Override
    public boolean assignRoleToUser(Integer userId, Integer roleId, String assignedBy) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 获取角色信息以获取roleCode
                Role role = roleDao.findById(roleId, conn);
                if (role == null) {
                    System.err.println("角色不存在: " + roleId);
                    return false;
                }
                
                UserRole userRole = new UserRole(userId, roleId, role.getRoleCode(), null, assignedBy);
                return userRoleDao.insert(userRole, conn);
            });
        } catch (Exception e) {
            System.err.println("分配角色到用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean assignRoleToUser(Integer userId, String roleCode, String assignedBy) {
        Role role = getRoleByCode(roleCode);
        if (role != null) {
            return assignRoleToUser(userId, role.getRoleId(), assignedBy);
        }
        return false;
    }
    
    @Override
    public boolean removeRoleFromUser(Integer userId, Integer roleId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userRoleDao.deleteByUserAndRole(userId, roleId, conn)
            );
        } catch (Exception e) {
            System.err.println("从用户移除角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean removeRoleFromUser(Integer userId, String roleCode) {
        Role role = getRoleByCode(roleCode);
        if (role != null) {
            return removeRoleFromUser(userId, role.getRoleId());
        }
        return false;
    }
    
    @Override
    public List<UserRole> getUserRoles(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userRoleDao.findByUserId(userId, conn)
            );
        } catch (Exception e) {
            System.err.println("获取用户角色失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<UserRole> getActiveUserRoles(Integer userId) {
        return getUserRoles(userId).stream()
                .filter(UserRole::isValid)
                .collect(Collectors.toList());
    }
    
    @Override
    public Set<String> getUserRoleCodes(Integer userId) {
        return getActiveUserRoles(userId).stream()
                .map(UserRole::getRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasPermission(Integer userId, String permissionCode) {
        Set<String> userPermissions = getUserPermissionCodes(userId);
        return userPermissions.contains(permissionCode);
    }
    
    @Override
    public boolean hasRole(Integer userId, String roleCode) {
        Set<String> userRoles = getUserRoleCodes(userId);
        return userRoles.contains(roleCode);
    }
    
    @Override
    public boolean hasAnyRole(Integer userId, String... roleCodes) {
        Set<String> userRoles = getUserRoleCodes(userId);
        return Arrays.stream(roleCodes).anyMatch(userRoles::contains);
    }
    
    @Override
    public boolean hasAllRoles(Integer userId, String... roleCodes) {
        Set<String> userRoles = getUserRoleCodes(userId);
        return Arrays.stream(roleCodes).allMatch(userRoles::contains);
    }
    
    @Override
    public Set<Permission> getUserPermissions(Integer userId) {
        Set<Permission> permissions = new HashSet<>();
        List<UserRole> userRoles = getActiveUserRoles(userId);
        
        for (UserRole userRole : userRoles) {
            Role role = getRoleById(userRole.getRoleId());
            if (role != null) {
                Set<Permission> rolePermissions = getRolePermissions(role.getRoleId());
                permissions.addAll(rolePermissions);
            }
        }
        
        return permissions;
    }
    
    @Override
    public Set<String> getUserPermissionCodes(Integer userId) {
        return getUserPermissions(userId).stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());
    }
    
    @Override
    public void initializeDefaultRolesAndPermissions() {
        try {
            TransactionManager.executeInTransaction(conn -> {
                // 创建默认权限
                createDefaultPermissions(conn);
                
                // 创建默认角色
                createDefaultRoles(conn);
                
                return true;
            });
        } catch (Exception e) {
            System.err.println("初始化默认角色和权限失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean validateAccess(String resource, String action, Integer userId) {
        String permissionCode = resource + ":" + action;
        return hasPermission(userId, permissionCode);
    }
    
    /**
     * 创建默认权限
     */
    private void createDefaultPermissions(Connection conn) throws Exception {
        System.out.println("📋 开始创建默认权限...");
        
        // 批量创建权限，减少数据库查询次数
        Permission[] permissions = {
            // 用户管理权限
            new Permission(null, "查看用户", Permission.USER_VIEW, "user", "view", "查看用户信息"),
            new Permission(null, "创建用户", Permission.USER_CREATE, "user", "create", "创建新用户"),
            new Permission(null, "更新用户", Permission.USER_UPDATE, "user", "update", "更新用户信息"),
            new Permission(null, "删除用户", Permission.USER_DELETE, "user", "delete", "删除用户"),
            new Permission(null, "管理用户角色", Permission.USER_MANAGE_ROLES, "user", "manage_roles", "管理用户角色分配"),
            
            // 学生管理权限
            new Permission(null, "查看学生", Permission.STUDENT_VIEW, "student", "view", "查看学生信息"),
            new Permission(null, "创建学生", Permission.STUDENT_CREATE, "student", "create", "创建学生账户"),
            new Permission(null, "更新学生", Permission.STUDENT_UPDATE, "student", "update", "更新学生信息"),
            new Permission(null, "删除学生", Permission.STUDENT_DELETE, "student", "delete", "删除学生账户"),
            new Permission(null, "管理学生", Permission.STUDENT_MANAGE, "student", "manage", "管理学生"),
            
            // 教师管理权限
            new Permission(null, "查看教师", Permission.TEACHER_VIEW, "teacher", "view", "查看教师信息"),
            new Permission(null, "创建教师", Permission.TEACHER_CREATE, "teacher", "create", "创建教师账户"),
            new Permission(null, "更新教师", Permission.TEACHER_UPDATE, "teacher", "update", "更新教师信息"),
            new Permission(null, "删除教师", Permission.TEACHER_DELETE, "teacher", "delete", "删除教师账户"),
            new Permission(null, "管理教师", Permission.TEACHER_MANAGE, "teacher", "manage", "管理教师"),
            
            // 图书馆权限
            new Permission(null, "查看图书馆", Permission.LIBRARY_VIEW, "library", "view", "查看图书馆信息"),
            new Permission(null, "借阅图书", Permission.LIBRARY_BORROW, "library", "borrow", "借阅图书"),
            new Permission(null, "归还图书", Permission.LIBRARY_RETURN, "library", "return", "归还图书"),
            new Permission(null, "管理图书馆", Permission.LIBRARY_MANAGE, "library", "manage", "管理图书馆"),
            new Permission(null, "图书馆管理", Permission.LIBRARY_ADMIN, "library", "admin", "图书馆管理员权限"),
            
            // 商店权限
            new Permission(null, "查看商店", Permission.SHOP_VIEW, "shop", "view", "查看商店信息"),
            new Permission(null, "购买商品", Permission.SHOP_PURCHASE, "shop", "purchase", "购买商品"),
            new Permission(null, "管理商店", Permission.SHOP_MANAGE, "shop", "manage", "管理商店"),
            new Permission(null, "商店管理", Permission.SHOP_ADMIN, "shop", "admin", "商店管理员权限"),
            
            // 系统管理权限
            new Permission(null, "系统管理", Permission.SYSTEM_ADMIN, "system", "admin", "系统管理员权限"),
            new Permission(null, "系统配置", Permission.SYSTEM_CONFIG, "system", "config", "系统配置权限"),
            new Permission(null, "系统日志", Permission.SYSTEM_LOG, "system", "log", "查看系统日志")
        };
        
        // 批量创建权限
        for (Permission permission : permissions) {
            createPermissionIfNotExists(permission, conn);
        }
        
        System.out.println("✅ 默认权限创建完成");
    }
    
    /**
     * 创建默认角色
     */
    private void createDefaultRoles(Connection conn) throws Exception {
        System.out.println("👥 开始创建默认角色...");
        
        // 学生角色
        Role studentRole = new Role(null, "学生", Role.STUDENT_ROLE, "学生用户，具有基础学习功能权限");
        createRoleIfNotExists(studentRole, conn);
        assignPermissionsToRole(studentRole.getRoleCode(), new String[]{
            Permission.USER_VIEW, Permission.STUDENT_VIEW, Permission.LIBRARY_VIEW, 
            Permission.LIBRARY_BORROW, Permission.LIBRARY_RETURN, Permission.SHOP_VIEW, 
            Permission.SHOP_PURCHASE
        }, conn);
        
        // 教师角色
        Role teacherRole = new Role(null, "教师", Role.TEACHER_ROLE, "教师用户，具有教学管理权限");
        createRoleIfNotExists(teacherRole, conn);
        assignPermissionsToRole(teacherRole.getRoleCode(), new String[]{
            Permission.USER_VIEW, Permission.STUDENT_VIEW, Permission.TEACHER_VIEW,
            Permission.LIBRARY_VIEW, Permission.LIBRARY_BORROW, Permission.LIBRARY_RETURN,
            Permission.SHOP_VIEW, Permission.SHOP_PURCHASE, Permission.STUDENT_MANAGE
        }, conn);
        
        // 管理员角色
        Role adminRole = new Role(null, "管理员", Role.ADMIN_ROLE, "系统管理员，具有所有权限");
        createRoleIfNotExists(adminRole, conn);
        assignPermissionsToRole(adminRole.getRoleCode(), new String[]{
            Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, 
            Permission.USER_DELETE, Permission.USER_MANAGE_ROLES,
            Permission.STUDENT_VIEW, Permission.STUDENT_CREATE, Permission.STUDENT_UPDATE, 
            Permission.STUDENT_DELETE, Permission.STUDENT_MANAGE,
            Permission.TEACHER_VIEW, Permission.TEACHER_CREATE, Permission.TEACHER_UPDATE, 
            Permission.TEACHER_DELETE, Permission.TEACHER_MANAGE,
            Permission.LIBRARY_VIEW, Permission.LIBRARY_MANAGE, Permission.LIBRARY_ADMIN,
            Permission.SHOP_VIEW, Permission.SHOP_MANAGE, Permission.SHOP_ADMIN,
            Permission.SYSTEM_ADMIN, Permission.SYSTEM_CONFIG, Permission.SYSTEM_LOG
        }, conn);
        
        System.out.println("✅ 默认角色创建完成");
    }
    
    /**
     * 如果权限不存在则创建
     */
    private void createPermissionIfNotExists(Permission permission, Connection conn) throws Exception {
        if (permissionDao.findByCode(permission.getPermissionCode(), conn) == null) {
            permissionDao.insert(permission, conn);
        }
    }
    
    /**
     * 如果角色不存在则创建
     */
    private void createRoleIfNotExists(Role role, Connection conn) throws Exception {
        if (roleDao.findByCode(role.getRoleCode(), conn) == null) {
            roleDao.insert(role, conn);
        }
    }
    
    /**
     * 为角色分配权限
     */
    private void assignPermissionsToRole(String roleCode, String[] permissionCodes, Connection conn) throws Exception {
        Role role = roleDao.findByCode(roleCode, conn);
        if (role != null) {
            for (String permissionCode : permissionCodes) {
                Permission permission = permissionDao.findByCode(permissionCode, conn);
                if (permission != null) {
                    roleDao.assignPermission(role.getRoleId(), permission.getPermissionId(), conn);
                }
            }
        }
    }
}
