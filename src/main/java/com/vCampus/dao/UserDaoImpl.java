package com.vCampus.dao;

import com.vCampus.entity.User;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象实现类
 */
public class UserDaoImpl implements IUserDao {

    @Override
    public User findById(Integer userId, Connection conn) throws SQLException {
        return findByUserId(userId, conn);
    }

    @Override
    public List<User> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_user";
        List<User> users = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        }
        return users;
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
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

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
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getUserId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(Integer userId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_user WHERE userId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
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
                    return createUserFromResultSet(rs);
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

    public User findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_user WHERE userId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }
}