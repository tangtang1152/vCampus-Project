package com.vCampus.dao;

import com.vCampus.entity.Permission;
import com.vCampus.entity.Role;
import com.vCampus.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色数据访问实现类
 */
public class RoleDaoImpl extends AbstractBaseDaoImpl<Role, Integer> implements IRoleDao {
    
    @Override
    protected String getTableName() {
        return "roles";
    }
    
    @Override
    protected String getIdColumnName() {
        return "role_id";
    }
    
    @Override
    protected Role createEntityFromResultSet(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("role_id"));
        role.setRoleName(rs.getString("role_name"));
        role.setRoleCode(rs.getString("role_code"));
        role.setDescription(rs.getString("description"));
        return role;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Role entity) throws SQLException {
        ps.setString(1, entity.getRoleName());
        ps.setString(2, entity.getRoleCode());
        ps.setString(3, entity.getDescription());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Role entity) throws SQLException {
        ps.setString(1, entity.getRoleName());
        ps.setString(2, entity.getRoleCode());
        ps.setString(3, entity.getDescription());
        ps.setInt(4, entity.getRoleId());
    }
    
    @Override
    public boolean insert(Role entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO roles (role_name, role_code, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setInsertParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean update(Role entity, Connection conn) throws SQLException {
        String sql = "UPDATE roles SET role_name = ?, role_code = ?, description = ? WHERE role_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public Role findByCode(String roleCode, Connection conn) {
        String sql = "SELECT * FROM roles WHERE role_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("根据代码查找角色失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean assignPermission(Integer roleId, Integer permissionId, Connection conn) {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("分配权限到角色失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean removePermission(Integer roleId, Integer permissionId, Connection conn) {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("从角色移除权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Set<Permission> getRolePermissions(Integer roleId, Connection conn) {
        Set<Permission> permissions = new HashSet<>();
        String sql = "SELECT p.* FROM permissions p " +
                    "INNER JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                    "WHERE rp.role_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setPermissionId(rs.getInt("permission_id"));
                    permission.setPermissionName(rs.getString("permission_name"));
                    permission.setPermissionCode(rs.getString("permission_code"));
                    permission.setResource(rs.getString("resource"));
                    permission.setAction(rs.getString("action"));
                    permission.setDescription(rs.getString("description"));
                    permissions.add(permission);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取角色权限失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return permissions;
    }
}
