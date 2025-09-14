package com.vcampus.dao;

import com.vcampus.entity.BorrowRecord;

import java.sql.SQLException;
import java.util.List;

public interface IBorrowRecordDao {
    
	// 获取最新的借阅记录
    BorrowRecord getLatestBorrowingRecord(String studentId, int bookId) throws SQLException;
	    
	// 插入借阅记录
    boolean insertBorrowRecord(BorrowRecord record);
    
    // 更新借阅记录
    boolean updateBorrowRecord(BorrowRecord record);
    
    /**
     * 硬删除借阅记录（直接删除）
     */
    boolean deleteBorrowRecord(int recordId);
    
    /**
     * 软删除借阅记录（标记为 deleted）
     */
    boolean softDeleteBorrowRecord(int recordId);
    
    // 根据借阅ID查找记录
    BorrowRecord getBorrowRecordById(int recordId);
    
    // 根据学生ID查找所有借阅记录
    List<BorrowRecord> getBorrowRecordsByStudentId(String studentId);
    
    // 根据图书ID查找所有借阅记录
    List<BorrowRecord> getBorrowRecordsByBookId(int bookId);
    
    // 获取所有借阅记录
    List<BorrowRecord> getAllBorrowRecords();
    
    // 查找逾期未还的记录
    List<BorrowRecord> getOverdueRecords();
    
    // 查找当前正在借阅的记录
    List<BorrowRecord> getBorrowingRecords();
    
    // 归还图书
    boolean returnBook(int recordId);
    
    
}