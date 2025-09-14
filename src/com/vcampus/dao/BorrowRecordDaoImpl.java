package com.vcampus.dao;

import com.vcampus.entity.BorrowRecord;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDaoImpl implements IBorrowRecordDao {

	@Override
	public BorrowRecord getLatestBorrowingRecord(String studentId, int bookId) throws SQLException {
	    String sql = "SELECT * FROM borrowrecord WHERE student_id = ? AND book_id = ? AND status = 'borrowing' ORDER BY borrow_date DESC";
	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, studentId);
	        pstmt.setInt(2, bookId);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return resultSetToBorrowRecord(rs); // 使用你已经写好的转换方法
	        }
	    }
	    return null;
	}
	
    @Override
    public boolean insertBorrowRecord(BorrowRecord record) {
        String sql = "INSERT INTO borrowrecord (student_id, book_id, borrow_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getStudentId());
            pstmt.setInt(2, record.getBookId());
            pstmt.setTimestamp(3, new Timestamp(record.getBorrowDate().getTime()));
            pstmt.setTimestamp(4, new Timestamp(record.getDueDate().getTime()));
            
            if (record.getReturnDate() != null) {
                pstmt.setTimestamp(5, new Timestamp(record.getReturnDate().getTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }
            
            pstmt.setString(6, record.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateBorrowRecord(BorrowRecord record) {
        String sql = "UPDATE borrowrecord SET student_id = ?, book_id = ?, borrow_date = ?, due_date = ?, return_date = ?, status = ? WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getStudentId());
            pstmt.setInt(2, record.getBookId());
            pstmt.setTimestamp(3, new Timestamp(record.getBorrowDate().getTime()));
            pstmt.setTimestamp(4, new Timestamp(record.getDueDate().getTime()));
            
            if (record.getReturnDate() != null) {
                pstmt.setTimestamp(5, new Timestamp(record.getReturnDate().getTime()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }
            
            pstmt.setString(6, record.getStatus());
            pstmt.setInt(7, record.getRecordId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBorrowRecord(int recordId) {
        String sql = "DELETE FROM borrowrecord WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean softDeleteBorrowRecord(int recordId) {
        String sql = "UPDATE borrowrecord SET status = 'deleted' WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BorrowRecord getBorrowRecordById(int recordId) {
        String sql = "SELECT * FROM borrowrecord WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToBorrowRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByStudentId(String studentId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrowrecord WHERE student_id = ? ORDER BY borrow_date DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(resultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByBookId(int bookId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrowrecord WHERE book_id = ? ORDER BY borrow_date DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(resultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrowrecord ORDER BY borrow_date DESC";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                records.add(resultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrowrecord WHERE status = 'overdue' OR (return_date IS NULL AND due_date < ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(resultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getBorrowingRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM borrowrecord WHERE status = 'borrowing' OR (return_date IS NULL AND due_date >= ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(resultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public boolean returnBook(int recordId) {
        String sql = "UPDATE borrowrecord SET return_date = ?, status = 'returned' WHERE record_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, recordId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 将ResultSet转换为BorrowRecord对象
    private BorrowRecord resultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setStudentId(rs.getString("student_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBorrowDate(rs.getTimestamp("borrow_date"));
        record.setDueDate(rs.getTimestamp("due_date"));
        record.setReturnDate(rs.getTimestamp("return_date"));
        record.setStatus(rs.getString("status"));
        return record;
    } 
    }

