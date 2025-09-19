package com.vCampus.dao;

import com.vCampus.entity.BorrowRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDao implements IBorrowRecordDao {
    @Override
    public BorrowRecord findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE recordId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<BorrowRecord> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean insert(BorrowRecord e, Connection conn) throws SQLException {
        return createBorrow(e, conn);
    }

    @Override
    public boolean update(BorrowRecord e, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_borrow_record SET bookId=?, userId=?, borrowDate=?, dueDate=?, returnDate=?, renewTimes=?, fine=?, borrowStatus=? WHERE recordId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, e.getBookId());
            ps.setObject(2, e.getUserId());
            ps.setDate(3, e.getBorrowDate());
            ps.setDate(4, e.getDueDate());
            ps.setDate(5, e.getReturnDate());
            ps.setObject(6, e.getRenewTimes());
            ps.setObject(7, e.getFine());
            ps.setString(8, e.getStatus());
            ps.setObject(9, e.getRecordId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_borrow_record WHERE recordId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean createBorrow(BorrowRecord r, Connection conn) throws SQLException {
        String sql = "INSERT INTO tbl_borrow_record (bookId, userId, borrowDate, dueDate, returnDate, renewTimes, fine, borrowStatus) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, r.getBookId());
            ps.setObject(2, r.getUserId());
            ps.setDate(3, r.getBorrowDate());
            ps.setDate(4, r.getDueDate());
            ps.setDate(5, r.getReturnDate());
            ps.setObject(6, r.getRenewTimes());
            ps.setObject(7, r.getFine());
            ps.setString(8, r.getStatus());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) r.setRecordId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean markReturn(Integer recordId, java.sql.Date returnDate, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_borrow_record SET returnDate=?, borrowStatus='已还' WHERE recordId=? AND borrowStatus='借出'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, returnDate);
            ps.setInt(2, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<BorrowRecord> findActiveByUser(String userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId=? AND borrowStatus='借出'";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<BorrowRecord> findOverdueByUser(String userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId=? AND borrowStatus='逾期'";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<BorrowRecord> listByUser(String userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId=? ORDER BY borrowDate DESC";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<BorrowRecord> listByUserAndStatus(String userId, String status, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE userId=? AND borrowStatus=? ORDER BY borrowDate DESC";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public int countTotalByBook(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    @Override
    public int countMonthlyByBook(Integer bookId, java.sql.Date monthStart, java.sql.Date nextMonthStart, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=? AND borrowDate>=? AND borrowDate<?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setDate(2, monthStart);
            ps.setDate(3, nextMonthStart);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    @Override
    public int countCurrentBorrowedByBook(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=? AND borrowStatus='借出'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    @Override
    public List<BorrowRecord> listByBook(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_borrow_record WHERE bookId=? ORDER BY borrowDate DESC";
        List<BorrowRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean existsActiveByUserAndBook(Integer userId, Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM tbl_borrow_record WHERE userId=? AND bookId=? AND borrowStatus='借出' FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public int countActiveBorrowsByUser(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_borrow_record WHERE userId=? AND borrowStatus='借出'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean existsOverdueByUser(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM tbl_borrow_record WHERE userId=? AND borrowStatus='逾期' FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord r = new BorrowRecord();
        r.setRecordId((Integer) rs.getObject("recordId"));
        r.setBookId((Integer) rs.getObject("bookId"));
        r.setUserId((Integer) rs.getObject("userId"));
        r.setBorrowDate(rs.getDate("borrowDate"));
        r.setDueDate(rs.getDate("dueDate"));
        r.setReturnDate(rs.getDate("returnDate"));
        r.setRenewTimes((Integer) rs.getObject("renewTimes"));
        r.setFine(rs.getObject("fine") == null ? null : ((Number) rs.getObject("fine")).doubleValue());
        r.setStatus(rs.getString("borrowStatus"));
        return r;
    }
}


