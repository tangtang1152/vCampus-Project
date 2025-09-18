package com.vCampus.dao;

import com.vCampus.entity.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色关联数据访问实现类
 */
public class UserRoleDaoImpl extends AbstractBaseDaoImpl<UserRole, Integer> implements IUserRoleDao {
    
    @Override
    protected String getTableName() {
        return "user_roles";
    }
    
    @Override
    protected String getIdColumnName() {
        return "user_role_id";
    }
    
    @Override
    protected UserRole createEntityFromResultSet(ResultSet rs) throws SQLException {
        UserRole userRole = new UserRole();
        userRole.setUserRoleId(rs.getInt("user_role_id"));
        userRole.setUserId(rs.getInt("user_id"));
        userRole.setRoleId(rs.getInt("role_id"));
        userRole.setRoleCode(rs.getString("role_code"));
        userRole.setAssignedDate(rs.getTimestamp("assigned_date"));
        userRole.setExpireDate(rs.getTimestamp("expire_date"));
        userRole.setIsActive(rs.getBoolean("is_active"));
        userRole.setAssignedBy(rs.getString("assigned_by"));
        return userRole;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, UserRole entity) throws SQLException {
        ps.setInt(1, entity.getUserId());
        ps.setInt(2, entity.getRoleId());
        ps.setString(3, entity.getRoleCode());
        ps.setTimestamp(4, new java.sql.Timestamp(entity.getAssignedDate().getTime()));
        if (entity.getExpireDate() != null) {
            ps.setTimestamp(5, new java.sql.Timestamp(entity.getExpireDate().getTime()));
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
        ps.setBoolean(6, entity.getIsActive());
        ps.setString(7, entity.getAssignedBy());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, UserRole entity) throws SQLException {
        ps.setInt(1, entity.getUserId());
        ps.setInt(2, entity.getRoleId());
        ps.setString(3, entity.getRoleCode());
        ps.setTimestamp(4, new java.sql.Timestamp(entity.getAssignedDate().getTime()));
        if (entity.getExpireDate() != null) {
            ps.setTimestamp(5, new java.sql.Timestamp(entity.getExpireDate().getTime()));
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
        ps.setBoolean(6, entity.getIsActive());
        ps.setString(7, entity.getAssignedBy());
        ps.setInt(8, entity.getUserRoleId());
    }
    
    @Override
    public boolean insert(UserRole entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO user_roles (user_id, role_id, role_code, assigned_date, expire_date, is_active, assigned_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setInsertParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean update(UserRole entity, Connection conn) throws SQLException {
        String sql = "UPDATE user_roles SET user_id = ?, role_id = ?, role_code = ?, assigned_date = ?, expire_date = ?, is_active = ?, assigned_by = ? WHERE user_role_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public List<UserRole> findByUserId(Integer userId, Connection conn) {
        List<UserRole> userRoles = new ArrayList<>();
        String sql = "SELECT * FROM user_roles WHERE user_id = ? ORDER BY assigned_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userRoles.add(createEntityFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("根据用户ID查找用户角色失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return userRoles;
    }
    
    @Override
    public boolean deleteByUserAndRole(Integer userId, Integer roleId, Connection conn) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, roleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除用户角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteByUserAndRoleCode(Integer userId, String roleCode, Connection conn) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("根据角色代码删除用户角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean hasRole(Integer userId, String roleCode, Connection conn) {
        String sql = "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_code = ? AND is_active = true";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("检查用户角色失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public List<UserRole> findByRoleCode(String roleCode, Connection conn) {
        List<UserRole> userRoles = new ArrayList<>();
        String sql = "SELECT * FROM user_roles WHERE role_code = ? ORDER BY assigned_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userRoles.add(createEntityFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("根据角色代码查找用户角色失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return userRoles;
    }
}
