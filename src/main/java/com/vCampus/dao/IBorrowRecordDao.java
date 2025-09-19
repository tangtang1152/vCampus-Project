package com.vCampus.dao;

import com.vCampus.entity.BorrowRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IBorrowRecordDao extends IBaseDao<BorrowRecord, Integer> {
    boolean createBorrow(BorrowRecord record, Connection conn) throws SQLException;
    boolean markReturn(Integer recordId, java.sql.Date returnDate, Connection conn) throws SQLException;
    List<BorrowRecord> findActiveByUser(String userId, Connection conn) throws SQLException;
    List<BorrowRecord> findOverdueByUser(String userId, Connection conn) throws SQLException;
    int countActiveBorrowsByUser(Integer userId, Connection conn) throws SQLException;
    boolean existsOverdueByUser(Integer userId, Connection conn) throws SQLException;
    
    // 新增：是否已借出同一本书（防止重复借阅）
    boolean existsActiveByUserAndBook(Integer userId, Integer bookId, Connection conn) throws SQLException;
    
    // 新增：按用户列出全部借阅
    List<BorrowRecord> listByUser(String userId, Connection conn) throws SQLException;
    
    // 新增：按用户+状态列出借阅
    List<BorrowRecord> listByUserAndStatus(String userId, String status, Connection conn) throws SQLException;

    // 统计：某书总借阅次数
    int countTotalByBook(Integer bookId, Connection conn) throws SQLException;
    // 统计：某书本月借阅次数（区间 [start, nextStart)）
    int countMonthlyByBook(Integer bookId, java.sql.Date monthStart, java.sql.Date nextMonthStart, Connection conn) throws SQLException;
    // 统计：某书当前借出数量
    int countCurrentBorrowedByBook(Integer bookId, Connection conn) throws SQLException;
}


