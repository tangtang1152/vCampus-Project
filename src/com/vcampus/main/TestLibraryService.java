package com.vcampus.main;

import com.vcampus.service.LibraryService;
import com.vcampus.entity.BorrowRecord;
import java.util.List;
import com.vcampus.dao.BorrowRecordDaoImpl;

public class TestLibraryService {
    private static LibraryService libraryService = new LibraryService();
    private static BorrowRecordDaoImpl borrowRecordDao = new BorrowRecordDaoImpl();
    // 清理模式： "hard" = 硬删除, "soft" = 软删除
    private static final String CLEAN_MODE = "soft";  

    public static void main(String[] args) {
        System.out.println("🎯 开始测试图书馆服务层...\n");

        // 测试前清理测试数据
        cleanupTestData();

        // 测试1: 借阅功能
        testBorrowFunction();

        // 测试2: 查询功能
        testQueryFunctions();

        // 测试3: 归还功能
        testReturnFunction();

        // 测试4: 续借功能
        testRenewFunction();

        // 测试5: 边界情况测试
        testEdgeCases();

        // 测试完成后再次清理
        cleanupTestData();

        System.out.println("\n✅ 所有测试完成！");
    }

    /**
     * 根据模式清理测试数据
     */
    private static void cleanupTestData() {
        System.out.println("\n🧹 开始清理测试数据... (模式=" + CLEAN_MODE + ")");

        String[] testStudentIds = {"2023001", "2023002", "2023003"};

        for (String studentId : testStudentIds) {
            if ("hard".equalsIgnoreCase(CLEAN_MODE)) {
                libraryService.cleanupStudentRecordsHard(studentId);
            } else {
                libraryService.cleanupStudentRecordsSoft(studentId);
            }
        }

        // 重置图书库存（根据需要调整）
        libraryService.resetBookInventory(1, 5);
        libraryService.resetBookInventory(2, 3);
        libraryService.resetBookInventory(3, 2);
        libraryService.resetBookInventory(4, 1);
        libraryService.resetBookInventory(5, 4);
        libraryService.resetBookInventory(6, 2);
        libraryService.resetBookInventory(7, 3);

        System.out.println("✅ 测试数据清理完成\n");
    }

    /**
     * 测试借阅功能
     */
    private static void testBorrowFunction() {
        System.out.println("=== 测试1: 图书借阅功能 ===");

        // 测试正常借阅
        System.out.println("\n1. 测试正常借阅:");
        boolean success1 = libraryService.borrowBook(1, "2023001", 30);
        System.out.println("借阅结果: " + (success1 ? "✅ 成功" : "❌ 失败"));

        // 测试重复借阅同一本书
        System.out.println("\n2. 测试重复借阅同一本书:");
        boolean success2 = libraryService.borrowBook(1, "2023001", 30);
        System.out.println("重复借阅结果: " + (success2 ? "❌ 异常-应该失败" : "✅ 正常-应该失败"));

        // 测试借阅另一本书
        System.out.println("\n3. 测试借阅另一本书:");
        boolean success3 = libraryService.borrowBook(2, "2023001", 15);
        System.out.println("借阅另一本书结果: " + (success3 ? "✅ 成功" : "❌ 失败"));

        // 测试另一个用户借阅
        System.out.println("\n4. 测试另一个用户借阅:");
        boolean success4 = libraryService.borrowBook(3, "2023002", 20);
        System.out.println("另一用户借阅结果: " + (success4 ? "✅ 成功" : "❌ 失败"));
    }

    /**
     * 测试查询功能
     */
    private static void testQueryFunctions() {
        System.out.println("\n=== 测试2: 查询功能 ===");

        System.out.println("\n1. 查询用户2023001的借阅记录:");
        libraryService.showUserBorrowRecords("2023001");

        System.out.println("\n2. 查询用户2023002的借阅记录:");
        libraryService.showUserBorrowRecords("2023002");

        System.out.println("\n3. 查询图书1的借阅历史:");
        libraryService.showBookBorrowHistory(1);

        System.out.println("4. 用户2023001当前借阅数量: " +
                libraryService.getUserBorrowingCount("2023001"));

        System.out.println("5. 用户2023001是否有超期图书: " +
                libraryService.hasUserOverdueBooks("2023001"));
    }

    /**
     * 测试归还功能
     */
    private static void testReturnFunction() {
        System.out.println("\n=== 测试3: 图书归还功能 ===");

        System.out.println("归还前的借阅记录:");
        libraryService.showUserBorrowRecords("2023001");

        System.out.println("\n1. 测试归还图书:");
        boolean returnSuccess = libraryService.returnBook("2023001", 1); // ✅ 传入学生ID + recordId
        System.out.println("归还结果: " + (returnSuccess ? "✅ 成功" : "❌ 失败"));

        System.out.println("\n归还后的借阅记录:");
        libraryService.showUserBorrowRecords("2023001");

        System.out.println("\n2. 测试重复归还:");
        boolean duplicateReturn = libraryService.returnBook("2023001", 1); // ✅ 同样修改
        System.out.println("重复归还结果: " + (duplicateReturn ? "✅ 成功（已处理）" : "❌ 失败"));
    }

    /**
     * 测试续借功能
     */
    private static void testRenewFunction() {
        System.out.println("\n=== 测试4: 图书续借功能 ===");

        System.out.println("先借阅一本书用于测试续借:");
        boolean borrowResult = libraryService.borrowBook(4, "2023003", 7);
        if (!borrowResult) {
            System.out.println("❌ 借阅失败，无法测试续借");
            return;
        }

        // 获取刚创建的借阅记录
        System.out.println("获取借阅记录...");
        List<BorrowRecord> records = libraryService.getBorrowRecordsByStudentId("2023003");
        
        if (records.isEmpty()) {
            System.out.println("❌ 未找到借阅记录");
            return;
        }
        
        // 找到最新的未归还记录
        BorrowRecord targetRecord = null;
        for (BorrowRecord record : records) {
            if ("borrowing".equals(record.getStatus())) {
                targetRecord = record;
                break;
            }
        }
        
        if (targetRecord == null) {
            System.out.println("❌ 未找到可续借的记录（可能已归还）");
            return;
        }
        
        int recordId = targetRecord.getRecordId();
        System.out.println("📝 获取到借阅记录ID: " + recordId);
        System.out.println("   图书ID: " + targetRecord.getBookId());
        System.out.println("   当前应还日期: " + targetRecord.getDueDate());

        System.out.println("\n1. 测试正常续借:");
        boolean renewSuccess = libraryService.renewBook(recordId, 14);
        System.out.println("续借结果: " + (renewSuccess ? "✅ 成功" : "❌ 失败"));
        
        // 显示续借后的信息
        if (renewSuccess) {
            BorrowRecord updatedRecord = borrowRecordDao.getBorrowRecordById(recordId);
            if (updatedRecord != null) {
                System.out.println("🔄 续借成功！新的应还日期: " + updatedRecord.getDueDate());
            }
        }

        // 清理：归还图书
        System.out.println("清理测试数据...");
        libraryService.returnBook("2023003", 4);
    }


    /**
     * 测试边界情况
     */
    private static void testEdgeCases() {
        System.out.println("\n=== 测试5: 边界情况测试 ===");

        System.out.println("\n1. 测试借阅不存在的图书:");
        boolean invalidBook = libraryService.borrowBook(999, "2023001", 30);
        System.out.println("结果: " + (invalidBook ? "❌ 异常" : "✅ 正常-应该失败"));

        System.out.println("\n2. 测试借阅天数为0:");
        boolean zeroDays = libraryService.borrowBook(5, "2023001", 0);
        System.out.println("结果: " + (zeroDays ? "❌ 异常" : "✅ 正常-应该失败"));

        System.out.println("\n3. 测试借阅天数为负数:");
        boolean negativeDays = libraryService.borrowBook(5, "2023001", -5);
        System.out.println("结果: " + (negativeDays ? "❌ 异常" : "✅ 正常-应该失败"));

        System.out.println("\n4. 测试归还不存在的记录:");
        boolean invalidReturn = libraryService.returnBook("2023001", 999); // ✅ 修改调用
        System.out.println("结果: " + (invalidReturn ? "❌ 异常" : "✅ 正常-应该失败"));

        System.out.println("\n5. 显示所有超期图书:");
        libraryService.showAllOverdueBooks();
    }
}
