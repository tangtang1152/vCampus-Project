package com.vCampus.dao;

import com.vCampus.entity.User;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository // <-- 添加
public class UserDaoImpl extends AbstractBaseDaoImpl<User, Integer> implements IUserDao {
    @Override
    protected String getTableName() { return "tbl_user"; }

    @Override
    protected String getIdColumnName() { return "userId"; }

    @Override
    protected User createEntityFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, User user) throws SQLException {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getRole());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, User user) throws SQLException {
        String truncatedUsername = ValidationService.truncateString(user.getUsername(), 
                DBConstants.USERNAME_MAX_LENGTH);
        String truncatedPassword = ValidationService.truncateString(user.getPassword(), 
                DBConstants.PASSWORD_MAX_LENGTH);
        String truncatedRole = ValidationService.truncateString(user.getRole(), 
                DBConstants.ROLE_MAX_LENGTH);
        pstmt.setString(1, truncatedUsername);
        pstmt.setString(2, truncatedPassword);
        pstmt.setString(3, truncatedRole);
        pstmt.setInt(4, user.getUserId());
    }

    @Override
    public boolean insert(User user, Connection conn) throws SQLException {
        String truncatedUsername = ValidationService.truncateString(user.getUsername(), 
                DBConstants.USERNAME_MAX_LENGTH);
        String truncatedPassword = ValidationService.truncateString(user.getPassword(), 
                DBConstants.PASSWORD_MAX_LENGTH);
        String truncatedRole = ValidationService.truncateString(user.getRole(), 
                DBConstants.ROLE_MAX_LENGTH);
        user.setUsername(truncatedUsername);
        user.setPassword(truncatedPassword);
        user.setRole(truncatedRole);
        return super.insert(user, conn);
    }

    @Override
    public boolean update(User user, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_user SET username = ?, password = ?, role = ? WHERE userId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, user);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public User findByUsername(String username, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_user WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean validateUser(String username, String password, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_user WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}