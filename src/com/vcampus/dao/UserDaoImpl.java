package com.vcampus.dao;

import com.vcampus.entity.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象实现类
 */
public class UserDaoImpl implements IUserDao {

    @Override
    public boolean addUser(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO tbl_user (username, password, role) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public boolean deleteUser(Integer userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM tbl_user WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE tbl_user SET username = ?, password = ?, role = ? WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getUserId());
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public User getUserById(Integer userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM tbl_user ORDER BY userId");
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public User validateUser(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE username = ? AND password = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 将ResultSet映射到User对象的辅助方法
     * @param rs ResultSet对象
     * @return User对象
     * @throws SQLException
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     * @throws SQLException
     */
    public boolean usernameExists(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 检查用户ID是否存在
     * @param userId 用户ID
     * @return 是否存在
     * @throws SQLException
     */
    public boolean userIdExists(Integer userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tbl_user WHERE userId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 根据角色获取用户列表
     * @param role 角色（student/teacher/admin）
     * @return 用户列表
     * @throws SQLException
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE role = ? ORDER BY userId";
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     * @throws SQLException
     */
    public boolean updateUserPassword(Integer userId, String newPassword) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 是否更新成功
     * @throws SQLException
     */
    public boolean updateUserRole(Integer userId, String newRole) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 获取用户总数
     * @return 用户总数
     * @throws SQLException
     */
    public int getTotalUserCount() throws SQLException {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 根据角色获取用户数量
     * @param role 角色
     * @return 用户数量
     * @throws SQLException
     */
    public int getUserCountByRole(String role) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
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
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 搜索用户（根据用户名模糊搜索）
     * @param keyword 关键词
     * @return 用户列表
     * @throws SQLException
     */
    public List<User> searchUsers(String keyword) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_user WHERE username LIKE ? ORDER BY userId";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            rs = ps.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }
  
}