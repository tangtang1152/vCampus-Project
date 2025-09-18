package com.vCampus.dao;

import com.vCampus.entity.User;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    
  //-------------------------------------------------------------------------------
 
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     * @throws SQLException
     */
    public boolean updateUserPassword(Integer userId, String newPassword,Connection conn) throws SQLException {
        PreparedStatement ps = null;
        
        try {
            String sql = "UPDATE tbl_user SET password = ? WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 是否更新成功
     * @throws SQLException
     */
    public boolean updateUserRole(Integer userId, String newRole,Connection conn) throws SQLException {
        PreparedStatement ps = null;
        
        try {
            String sql = "UPDATE tbl_user SET role = ? WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据角色获取用户列表
     * @param role 角色（student/teacher/admin）
     * @return 用户列表
     * @throws SQLException
     */
    public List<User> getUsersByRole(String role,Connection conn) throws SQLException {
       // Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
           // conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE role = ? ORDER BY userId";
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                users.add(createEntityFromResultSet(rs));
            }
            return users;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
          //  DBUtil.closeConnection(conn);
        }
    }
    

    /**
         * @param username 用户名
     * @return 是否存在
     * @throws SQLException
     */
    public boolean usernameExists(String username,Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT COUNT(*) FROM tbl_user WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
 
    /**
     * 获取用户总数
     * @return 用户总数
     * @throws SQLException
     */
    public int getTotalUserCount(Connection conn) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        
        try {
            st = conn.createStatement();
            rs = st.executeQuery("SELECT COUNT(*) FROM tbl_user");
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 根据角色获取用户数量
     * @param role 角色
     * @return 用户数量
     * @throws SQLException
     */
    public int getUserCountByRole(String role,Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT COUNT(*) FROM tbl_user WHERE role = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
     
}