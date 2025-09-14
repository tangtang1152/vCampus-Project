package com.vcampus.main;

import com.vcampus.controller.LibraryController;

public class TestLibraryController {
    private static LibraryController controller = new LibraryController();
    
    public static void main(String[] args) {
        System.out.println("🎯 测试图书馆控制器层...\n");
        
        // 先清理测试数据
        controller.cleanupAllTestData();
        
        testBasicFunctions();
        testValidation();
        testQueryFunctions();
        
        System.out.println("\n✅ 控制器层测试完成！");
    }
    
    private static void testBasicFunctions() {
        System.out.println("=== 测试基本功能 ===");
        
        // 测试借书
        System.out.println("\n1. 测试借书:");
        boolean borrowResult = controller.borrowBook(1, "2023001", 30);
        System.out.println("借书结果: " + (borrowResult ? "✅ 成功" : "❌ 失败"));
        
        // 测试查询
        System.out.println("\n2. 测试查询用户记录:");
        controller.displayUserRecords("2023001");
        
        // 测试还书 - 修改为使用学生ID和图书ID
        System.out.println("\n3. 测试还书:");
        boolean returnResult = controller.returnBook("2023001", 1); // 修改这里
        System.out.println("还书结果: " + (returnResult ? "✅ 成功" : "❌ 失败"));
    }
    
    private static void testValidation() {
        System.out.println("\n=== 测试参数验证 ===");
        
        // 测试无效参数
        System.out.println("\n1. 测试无效图书ID:");
        String error1 = controller.validateBorrowParameters(0, "2023001", 30);
        System.out.println("验证结果: " + (error1 != null ? "✅ " + error1 : "❌ 应该失败"));
        
        System.out.println("\n2. 测试空学生ID:");
        String error2 = controller.validateBorrowParameters(1, "", 30);
        System.out.println("验证结果: " + (error2 != null ? "✅ " + error2 : "❌ 应该失败"));
        
        System.out.println("\n3. 测试无效借阅天数:");
        String error3 = controller.validateBorrowParameters(1, "2023001", -5);
        System.out.println("验证结果: " + (error3 != null ? "✅ " + error3 : "❌ 应该失败"));
        
        // 测试归还参数验证
        System.out.println("\n4. 测试归还参数验证:");
        String error4 = controller.validateReturnParameters("", 1);
        System.out.println("验证结果: " + (error4 != null ? "✅ " + error4 : "❌ 应该失败"));
    }
    
    private static void testQueryFunctions() {
        System.out.println("\n=== 测试查询功能 ===");
        
        // 先借几本书用于测试
        controller.borrowBook(2, "2023001", 15);
        controller.borrowBook(3, "2023002", 20);
        
        // 测试状态检查
        System.out.println("\n1. 用户是否可以借阅: " + 
            (controller.canUserBorrow("2023001") ? "✅ 可以" : "❌ 不可以"));
        
        System.out.println("2. 用户借阅数量: " + controller.getUserBorrowCount("2023001"));
        
        // 测试统计信息
        System.out.println("\n3. 图书馆统计:");
        System.out.println(controller.getLibraryStats());
        
        // 测试用户统计
        System.out.println("\n4. 用户借阅统计:");
        System.out.println(controller.getUserBorrowStats("2023001"));
        
        // 测试超期图书显示
        System.out.println("\n5. 超期图书:");
        controller.displayAllOverdueBooks();
        
        // 测试清理功能
        System.out.println("\n6. 测试数据清理:");
        boolean cleanupResult = controller.cleanupAllTestData();
        System.out.println("清理结果: " + (cleanupResult ? "✅ 成功" : "❌ 失败"));
    }
}