package com.vCampus.test;

import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.entity.User;
import com.vCampus.service.IUserService;

/**
 * 测试数据库修复是否有效
 */
public class DatabaseFixTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 开始测试数据库修复...");
        
        try {
            // 获取服务
            IPermissionService permissionService = ServiceFactory.getPermissionService();
            IUserService userService = ServiceFactory.getUserService();
            
            // 测试1: 检查默认角色是否存在
            System.out.println("📋 测试1: 检查默认角色...");
            var studentRole = permissionService.getRoleByCode("STUDENT");
            var teacherRole = permissionService.getRoleByCode("TEACHER");
            var adminRole = permissionService.getRoleByCode("ADMIN");
            
            System.out.println("学生角色: " + (studentRole != null ? "✅ 存在" : "❌ 不存在"));
            System.out.println("教师角色: " + (teacherRole != null ? "✅ 存在" : "❌ 不存在"));
            System.out.println("管理员角色: " + (adminRole != null ? "✅ 存在" : "❌ 不存在"));
            
            // 测试2: 尝试分配角色给用户
            System.out.println("\n📋 测试2: 测试角色分配...");
            
            // 查找一个测试用户
            User testUser = userService.findByUsername("kk");
            if (testUser != null) {
                System.out.println("找到测试用户: " + testUser.getUsername() + " (ID: " + testUser.getUserId() + ")");
                
                // 尝试分配学生角色
                boolean success = permissionService.assignRoleToUser(testUser.getUserId(), "STUDENT", "SYSTEM");
                System.out.println("分配学生角色: " + (success ? "✅ 成功" : "❌ 失败"));
                
                // 检查用户角色
                var userRoles = permissionService.getUserRoleCodes(testUser.getUserId());
                System.out.println("用户角色: " + userRoles);
                
            } else {
                System.out.println("❌ 找不到测试用户 'kk'");
            }
            
            System.out.println("\n🎉 数据库修复测试完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
