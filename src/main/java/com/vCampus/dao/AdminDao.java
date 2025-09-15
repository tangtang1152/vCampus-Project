package com.vCampus.dao;

import com.vCampus.entity.Admin;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;
import java.sql.*;

/**
 * 管理员数据访问对象实现类
 */
public class AdminDao implements IAdminDao {

    @Override
    public Admin findById(String adminId, Connection conn) throws SQLException {
        return findByAdminId(adminId, conn);
    }

    @Override
    public java.util.List<Admin> findAll(Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.username, u.password, u.role " +
                    "FROM tbl_admin a " +
                    "JOIN tbl_user u ON a.userId = u.userId";
        java.util.List<Admin> admins = new java.util.ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                admins.add(createAdminFromResultSet(rs));
            }
        }
        return admins;
    }

    @Override
    public boolean insert(Admin admin, Connection conn) throws SQLException {
        String truncatedAdminName = ValidationService.truncateString(
            admin.getAdminName(), DBConstants.ADMIN_NAME_MAX_LENGTH);
        admin.setAdminName(truncatedAdminName);

        String sql = "INSERT INTO tbl_admin (adminId, userId, adminName) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admin.getAdminId());
            pstmt.setInt(2, admin.getUserId());
            pstmt.setString(3, admin.getAdminName());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean update(Admin admin, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_admin SET adminName = ? WHERE adminId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admin.getAdminName());
            pstmt.setString(2, admin.getAdminId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(String adminId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_admin WHERE adminId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Admin findByAdminId(String adminId, Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.username, u.password, u.role " +
                    "FROM tbl_admin a " +
                    "JOIN tbl_user u ON a.userId = u.userId " +
                    "WHERE a.adminId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createAdminFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Admin findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT a.*, u.username, u.password, u.role " +
                    "FROM tbl_admin a " +
                    "JOIN tbl_user u ON a.userId = u.userId " +
                    "WHERE a.userId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createAdminFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Admin createAdminFromResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        
        // 设置从User表继承的属性
        admin.setUserId(rs.getInt("userId"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setRole(rs.getString("role"));
        
        // 设置Admin特有的属性
        admin.setAdminId(rs.getString("adminId"));
        admin.setAdminName(rs.getString("adminName"));
        
        return admin;
    }
}