package com.vcampus.controller;

import com.vcampus.entity.BorrowRecord;
import com.vcampus.service.LibraryService;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书馆控制器 - 处理图书馆模块的业务逻辑和界面交互
 */
public class LibraryController {
    private LibraryService libraryService;
    
    public LibraryController() {
        this.libraryService = new LibraryService();
    }
    
    // ==================== 借阅管理 ====================
    
    /**
     * 借阅图书
     */
    public boolean borrowBook(int bookId, String studentId, int borrowDays) {
        return libraryService.borrowBook(bookId, studentId, borrowDays);
    }
    
    /**
     * 归还图书 - 修改为使用学生ID和图书ID
     */
    public boolean returnBook(String studentId, int bookId) {
        return libraryService.returnBook(studentId, bookId);
    }
    
    /**
     * 续借图书
     */
    public boolean renewBook(int recordId, int additionalDays) {
        return libraryService.renewBook(recordId, additionalDays);
    }
    
    // ==================== 查询功能 ====================
    
    /**
     * 获取用户借阅记录
     */
    public List<BorrowRecord> getUserBorrowRecords(String studentId) {
        // 这里可以添加权限验证等逻辑
        return libraryService.getBorrowRecordsByStudentId(studentId);
    }
    
    /**
     * 获取图书借阅历史
     */
    public List<BorrowRecord> getBookBorrowHistory(int bookId) {
        return libraryService.getBorrowRecordsByBookId(bookId);
    }
    
    /**
     * 获取所有借阅记录
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return libraryService.getAllBorrowRecords();
    }
    
    /**
     * 获取超期图书列表
     */
    public List<BorrowRecord> getOverdueBooks() {
        return libraryService.getAllOverdueBooks();
    }
    
    // ==================== 状态检查 ====================
    
    /**
     * 检查用户是否可以借阅
     */
    public boolean canUserBorrow(String studentId) {
        return !libraryService.hasUserOverdueBooks(studentId);
    }
    
    /**
     * 获取用户当前借阅数量
     */
    public int getUserBorrowCount(String studentId) {
        return libraryService.getUserBorrowingCount(studentId);
    }
    
    /**
     * 检查用户是否有超期图书
     */
    public boolean hasUserOverdueBooks(String studentId) {
        return libraryService.hasUserOverdueBooks(studentId);
    }
    
    /**
     * 检查用户是否已借阅同一本书
     */
    public boolean hasUserBorrowedSameBook(String studentId, int bookId) {
        return libraryService.hasUserBorrowedSameBook(studentId, bookId);
    }
    
    // ==================== 显示功能 ====================
    
    /**
     * 显示用户借阅记录
     */
    public void displayUserRecords(String studentId) {
        libraryService.showUserBorrowRecords(studentId);
    }
    
    /**
     * 显示图书借阅历史
     */
    public void displayBookHistory(int bookId) {
        libraryService.showBookBorrowHistory(bookId);
    }
    
    /**
     * 显示所有超期图书
     */
    public void displayAllOverdueBooks() {
        libraryService.showAllOverdueBooks();
    }
    
    /**
     * 显示学生超期图书
     */
    public void displayStudentOverdueBooks(String studentId) {
        libraryService.showStudentOverdueBooks(studentId);
    }
    
    // ==================== 数据管理 ====================
    
    /**
     * 清理用户借阅记录（硬删除）
     */
    public boolean cleanupUserRecordsHard(String studentId) {
        return libraryService.cleanupStudentRecordsHard(studentId);
    }
    
    /**
     * 清理用户借阅记录（软删除）
     */
    public boolean cleanupUserRecordsSoft(String studentId) {
        return libraryService.cleanupStudentRecordsSoft(studentId);
    }
    
    /**
     * 清理所有测试数据
     */
    public boolean cleanupAllTestData() {
        // 清理测试用户数据
        String[] testStudentIds = {"2023001", "2023002", "2023003"};
        boolean success = true;
        
        for (String studentId : testStudentIds) {
            if (!libraryService.cleanupStudentRecordsHard(studentId)) {
                success = false;
            }
        }
        
        return success;
    }
    
    /**
     * 重置图书库存
     */
    public boolean resetBookInventory(int bookId, int availableCount) {
        return libraryService.resetBookInventory(bookId, availableCount);
    }
    
    // ==================== 业务逻辑验证 ====================
    
    /**
     * 验证借阅参数
     */
    public String validateBorrowParameters(int bookId, String studentId, int days) {
        if (bookId <= 0) {
            return "图书ID无效";
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            return "学生ID不能为空";
        }
        if (days <= 0) {
            return "借阅天数必须大于0";
        }
        
        // 检查是否已借阅同一本书
        if (hasUserBorrowedSameBook(studentId, bookId)) {
            return "已借阅该图书且未归还";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证归还参数
     */
    public String validateReturnParameters(String studentId, int bookId) {
        if (bookId <= 0) {
            return "图书ID无效";
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            return "学生ID不能为空";
        }
        return null; // 验证通过
    }
    
    /**
     * 验证续借参数
     */
    public String validateRenewParameters(int recordId, int additionalDays) {
        if (recordId <= 0) {
            return "记录ID无效";
        }
        if (additionalDays <= 0) {
            return "续借天数必须大于0";
        }
        return null; // 验证通过
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取图书馆统计信息
     */
    public String getLibraryStats() {
        List<BorrowRecord> allRecords = libraryService.getAllBorrowRecords();
        List<BorrowRecord> overdueRecords = libraryService.getAllOverdueBooks();
        
        int totalBorrows = allRecords.size();
        int currentBorrows = 0;
        int overdueCount = overdueRecords.size();
        
        for (BorrowRecord record : allRecords) {
            if (!record.isReturned()) {
                currentBorrows++;
            }
        }
        
        return String.format("📊 图书馆统计: 总借阅%d次, 当前借出%d本, 超期%d本", 
                           totalBorrows, currentBorrows, overdueCount);
    }
    
    /**
     * 获取用户借阅统计
     */
    public String getUserBorrowStats(String studentId) {
        int total = getUserBorrowRecords(studentId).size();
        int current = getUserBorrowCount(studentId);
        boolean hasOverdue = hasUserOverdueBooks(studentId);
        
        return String.format("👤 用户%s借阅统计: 总借阅%d次, 当前借阅%d本, 超期: %s",
                           studentId, total, current, hasOverdue ? "有" : "无");
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取用户最近借阅的图书ID
     */
    public Integer getLatestBorrowedBookId(String studentId) {
        List<BorrowRecord> records = getUserBorrowRecords(studentId);
        if (records.isEmpty()) {
            return null;
        }
        // 返回最近借阅的图书ID
        return records.get(0).getBookId();
    }
    
    /**
     * 获取用户的借阅记录ID列表
     */
    public List<Integer> getUserRecordIds(String studentId) {
        List<BorrowRecord> records = getUserBorrowRecords(studentId);
        List<Integer> recordIds = new ArrayList<>();
        
        for (BorrowRecord record : records) {
            recordIds.add(record.getRecordId());
        }
        
        return recordIds;
    }
}