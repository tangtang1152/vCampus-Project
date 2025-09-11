package com.vCampus.test;

import com.vCampus.service.UserService;

public class UserServiceTest {
    public static void main(String[] args) {
        System.out.println("========== 用户服务测试 ==========");
        
        // 测试登录功能
        String testUsername = "testuser";
        String testPassword = "testpass";
        
        boolean loginResult = UserService.login(testUsername, testPassword);
        System.out.println("登录测试结果: " + (loginResult ? "✅ 成功" : "❌ 失败"));
        
        // 测试注册功能（需要先创建Student对象）
        // Student student = new Student(1001, "测试用户", "密码", "男");
        // boolean registerResult = UserService.register(student);
        // System.out.println("注册测试结果: " + (registerResult ? "✅ 成功" : "❌ 失败"));
        
        System.out.println("========== 测试结束 ==========");
    }
}