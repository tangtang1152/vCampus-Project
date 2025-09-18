package com.vCampus.dao;

import com.vCampus.entity.Admin;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;

public class AdminDaoImpl extends AbstractBaseDaoImpl<Admin, String> implements IAdminDao {

    @Override
    protected String getTableName() {
        return "tbl_admin";
    }

    @Override
    protected String getIdColumnName() {
        return "adminId";
    }

    @Override
    protected Admin createEntityFromResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setUserId(rs.getInt("userId"));
        try { admin.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        try { admin.setPassword(rs.getString("password")); } catch (SQLException ignored) {}
        try { admin.setRole(rs.getString("role")); } catch (SQLException ignored) {}
        admin.setAdminId(rs.getString("adminId"));
        admin.setAdminName(rs.getString("adminName"));
        return admin;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Admin admin) throws SQLException {
        pstmt.setString(1, admin.getAdminId());
        pstmt.setInt(2, admin.getUserId());
        pstmt.setString(3, admin.getAdminName());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Admin admin) throws SQLException {
        pstmt.setString(1, admin.getAdminName());
        pstmt.setString(2, admin.getAdminId());
    }

    @Override
    public boolean insert(Admin admin, Connection conn) throws SQLException {
        String truncatedAdminName = ValidationService.truncateString(
                admin.getAdminName(), DBConstants.ADMIN_NAME_MAX_LENGTH);
       
        admin.setAdminName(truncatedAdminName);

        // 根据业务规范：adminId 为手工输入的唯一学工号（短文本），需要显式插入
        String sql = "INSERT INTO tbl_admin (adminId, userId, adminName) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, admin);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入管理员记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Admin admin, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_admin SET adminName = ? WHERE adminId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, admin);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Admin findByAdminId(String adminId, Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.username, u.password, u.role FROM tbl_admin a " +
                    "JOIN tbl_user u ON a.userId = u.userId WHERE a.adminId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Admin findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.username, u.password, u.role FROM tbl_admin a " +
                    "JOIN tbl_user u ON a.userId = u.userId WHERE a.userId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }
}