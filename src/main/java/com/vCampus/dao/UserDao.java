package com.vCampus.dao;

import com.vCampus.entity.User;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;
import com.vCampus.util.DBUtil;
import java.sql.*;

/**
 * 用户数据访问对象类
 * 负责与数据库中的tbl_user表进行交互
 * 提供对用户数据的CRUD（创建、读取、更新、删除）操作
 */
public class UserDao {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 要查找的用户名
     * @return 找到的用户对象，如果未找到则返回null
     * @throws SQLException 如果数据库操作发生错误
     */
    public static User findByUsername(String username) throws SQLException {
        // SQL查询语句，使用参数化查询防止SQL注入
        String sql = "SELECT * FROM tbl_user WHERE username = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数
            pstmt.setString(1, username);
            
            // 执行查询并处理结果
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 创建并返回用户对象
                    return createUserFromResultSet(rs);
                }
            }
        }
        
        // 如果没有找到匹配的用户，返回null
        return null;
    }
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 要查找的用户ID
     * @return 找到的用户对象，如果未找到则返回null
     * @throws SQLException 如果数据库操作发生错误
     */
    public static User findByUserId(int userId) throws SQLException {
        // SQL查询语句，使用参数化查询防止SQL注入
        String sql = "SELECT * FROM tbl_user WHERE userId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数
            pstmt.setInt(1, userId);
            
            // 执行查询并处理结果
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 创建并返回用户对象
                    return createUserFromResultSet(rs);
                }
            }
        }
        
        // 如果没有找到匹配的用户，返回null
        return null;
    }
    
    /**
     * 验证用户凭据
     * 
     * @param username 用户名
     * @param password 密码
     * @return 如果凭据有效返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean validateUser(String username, String password) throws SQLException {
        // SQL查询语句，使用参数化查询防止SQL注入
        String sql = "SELECT COUNT(*) FROM tbl_user WHERE username = ? AND password = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            // 执行查询并处理结果
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 创建新用户
     * 
     * @param user 要创建的用户对象
     * @return 创建成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean createUser(User user) throws SQLException {
        // 确保数据长度不超过数据库限制
        String truncatedUsername = ValidationService.truncateString(user.getUsername(), DBConstants.USERNAME_MAX_LENGTH);
        String truncatedPassword = ValidationService.truncateString(user.getPassword(), DBConstants.PASSWORD_MAX_LENGTH);
        String truncatedRole = ValidationService.truncateString(user.getRole(), DBConstants.ROLE_MAX_LENGTH);
        
        user.setUsername(truncatedUsername);
        user.setPassword(truncatedPassword);
        user.setRole(truncatedRole);
    	
    	// SQL插入语句
        String sql = "INSERT INTO tbl_user (username, password, role) VALUES (?, ?, ?)";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 设置插入参数
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            
            // 执行插入操作
            int affectedRows = pstmt.executeUpdate();
            
            // 如果插入成功，获取生成的用户ID
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
    
    /**
     * 更新用户信息
     * 
     * @param user 要更新的用户对象
     * @return 更新成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean updateUser(User user) throws SQLException {
        // SQL更新语句
        String sql = "UPDATE tbl_user SET username = ?, password = ?, role = ? WHERE userId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置更新参数
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getUserId());
            
            // 执行更新操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @return 删除成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean deleteUser(int userId) throws SQLException {
        // SQL删除语句
        String sql = "DELETE FROM tbl_user WHERE userId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置删除参数
            pstmt.setInt(1, userId);
            
            // 执行删除操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * 从结果集创建用户对象
     * 辅助方法，用于将数据库结果集转换为User对象
     * 
     * @param rs 数据库查询结果集
     * @return 从结果集创建的用户对象
     * @throws SQLException 如果读取结果集时发生错误
     */
    private static User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }
}