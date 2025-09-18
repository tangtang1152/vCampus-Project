package com.vCampus.test;

import com.vCampus.common.SessionContext;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.User;
import com.vCampus.service.IPermissionService;
import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.util.PermissionUtil;

/**
 * RBAC系统测试类
 * 用于验证权限控制系统是否正常工作
 */
public class RBACSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== RBAC系统测试开始 ===");
        
        try {
            // 测试权限服务
            IPermissionService permissionService = ServiceFactory.getPermissionService();
            IUserService userService = ServiceFactory.getUserService();
            
            // 测试角色和权限
            System.out.println("1. 测试角色和权限...");
            testRolesAndPermissions(permissionService);
            
            // 测试用户权限
            System.out.println("2. 测试用户权限...");
            testUserPermissions(userService, permissionService);
            
            // 测试权限工具类
            System.out.println("3. 测试权限工具类...");
            testPermissionUtil();
            
            System.out.println("=== RBAC系统测试完成 ===");
            
        } catch (Exception e) {
            System.err.println("RBAC系统测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testRolesAndPermissions(IPermissionService permissionService) {
        try {
            // 获取所有角色
            var roles = permissionService.getAllRoles();
            System.out.println("  角色数量: " + roles.size());
            for (var role : roles) {
                System.out.println("    - " + role.getRoleName() + " (" + role.getRoleCode() + ")");
            }
            
            // 获取所有权限
            var permissions = permissionService.getAllPermissions();
            System.out.println("  权限数量: " + permissions.size());
            for (var permission : permissions) {
                System.out.println("    - " + permission.getPermissionName() + " (" + permission.getPermissionCode() + ")");
            }
            
        } catch (Exception e) {
            System.err.println("  测试角色和权限失败: " + e.getMessage());
        }
    }
    
    private static void testUserPermissions(IUserService userService, IPermissionService permissionService) {
        try {
            // 创建测试用户
            Student testStudent = new Student();
            testStudent.setUsername("test_student");
            testStudent.setPassword("123456");
            testStudent.setRole("STUDENT");
            testStudent.setStudentId("S2024001");
            testStudent.setStudentName("测试学生");
            testStudent.setClassName("计算机1班");
            
            // 注册测试用户
            var result = userService.register(testStudent);
            System.out.println("  注册测试学生: " + result.getMessage());
            
            if (result == IUserService.RegisterResult.SUCCESS) {
                // 分配学生角色
                permissionService.assignRoleToUser(testStudent.getUserId(), "STUDENT", "system");
                
                // 测试权限
                boolean canViewLibrary = permissionService.hasPermission(testStudent.getUserId(), "library:view");
                boolean canManageUsers = permissionService.hasPermission(testStudent.getUserId(), "user:manage_roles");
                
                System.out.println("    学生可以查看图书馆: " + canViewLibrary);
                System.out.println("    学生可以管理用户: " + canManageUsers);
                
                // 清理测试数据
                userService.delete(testStudent.getUserId());
            }
            
        } catch (Exception e) {
            System.err.println("  测试用户权限失败: " + e.getMessage());
        }
    }
    
    private static void testPermissionUtil() {
        try {
            // 创建测试管理员用户
            Admin testAdmin = new Admin();
            testAdmin.setUsername("test_admin");
            testAdmin.setPassword("123456");
            testAdmin.setRole("ADMIN");
            testAdmin.setAdminId("A2024001");
            testAdmin.setAdminName("测试管理员");
            
            // 设置当前用户
            SessionContext.setCurrentUser(testAdmin);
            
            // 测试权限工具类
            boolean isAdmin = PermissionUtil.isAdmin();
            boolean canManageUsers = PermissionUtil.canManageUsers();
            boolean canAccessSystemAdmin = PermissionUtil.canAccessSystemAdmin();
            
            System.out.println("  当前用户是管理员: " + isAdmin);
            System.out.println("  可以管理用户: " + canManageUsers);
            System.out.println("  可以访问系统管理: " + canAccessSystemAdmin);
            
            // 清理
            SessionContext.clear();
            
        } catch (Exception e) {
            System.err.println("  测试权限工具类失败: " + e.getMessage());
        }
    }
}
