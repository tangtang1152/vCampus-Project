package com.vCampus.dao;

import com.vCampus.entity.User;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;

public class UserDaoImpl extends AbstractBaseDaoImpl<User, Integer> implements IUserDao {

    @Override
    protected String getTableName() {
        return "tbl_user";
    }

    @Override
    protected String getIdColumnName() {
        return "userId";
    }

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
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getRole());
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

        String sql = "INSERT INTO tbl_user (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(pstmt, user);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
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