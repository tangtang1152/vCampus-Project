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
}


