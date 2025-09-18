package com.vCampus.dao;

import com.vCampus.entity.Permission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 权限数据访问实现类
 */
public class PermissionDaoImpl extends AbstractBaseDaoImpl<Permission, Integer> implements IPermissionDao {
    
    @Override
    protected String getTableName() {
        return "permissions";
    }
    
    @Override
    protected String getIdColumnName() {
        return "permission_id";
    }
    
    @Override
    protected Permission createEntityFromResultSet(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setPermissionId(rs.getInt("permission_id"));
        permission.setPermissionName(rs.getString("permission_name"));
        permission.setPermissionCode(rs.getString("permission_code"));
        permission.setResource(rs.getString("resource"));
        permission.setAction(rs.getString("action"));
        permission.setDescription(rs.getString("description"));
        return permission;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Permission entity) throws SQLException {
        ps.setString(1, entity.getPermissionName());
        ps.setString(2, entity.getPermissionCode());
        ps.setString(3, entity.getResource());
        ps.setString(4, entity.getAction());
        ps.setString(5, entity.getDescription());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Permission entity) throws SQLException {
        ps.setString(1, entity.getPermissionName());
        ps.setString(2, entity.getPermissionCode());
        ps.setString(3, entity.getResource());
        ps.setString(4, entity.getAction());
        ps.setString(5, entity.getDescription());
        ps.setInt(6, entity.getPermissionId());
    }
    
    @Override
    public boolean insert(Permission entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO permissions (permission_name, permission_code, resource, action, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setInsertParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean update(Permission entity, Connection conn) throws SQLException {
        String sql = "UPDATE permissions SET permission_name = ?, permission_code = ?, resource = ?, action = ?, description = ? WHERE permission_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public Permission findByCode(String permissionCode, Connection conn) {
        String sql = "SELECT * FROM permissions WHERE permission_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, permissionCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("根据代码查找权限失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
