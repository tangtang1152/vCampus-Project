package com.vCampus.dao;

import com.vCampus.entity.Admin;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository // <-- 添加这个注解
public class AdminDaoImpl extends AbstractBaseDaoImpl<Admin, String> implements IAdminDao {

    @Override
    protected String getTableName() { return "tbl_admin"; }

    @Override
    protected String getIdColumnName() { return "adminId"; }

    @Override
    protected Admin createEntityFromResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getString("adminId"));
        admin.setAdminName(rs.getString("adminName"));
        admin.setUserId(rs.getInt("userId"));
        return admin;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Admin admin) throws SQLException {
        pstmt.setString(1, admin.getAdminId());
        pstmt.setString(2, admin.getAdminName());
        pstmt.setInt(3, admin.getUserId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Admin admin) throws SQLException {
        pstmt.setString(1, admin.getAdminName());
        pstmt.setInt(2, admin.getUserId());
        pstmt.setString(3, admin.getAdminId());
    }

    @Override
    public boolean insert(Admin admin, Connection conn) throws SQLException {
        String truncatedAdminName = ValidationService.truncateString(
                admin.getAdminName(), DBConstants.ADMIN_NAME_MAX_LENGTH);
        admin.setAdminName(truncatedAdminName);
        return super.insert(admin, conn);
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