package com.vCampus.dao;

import com.vCampus.entity.Permission;
import com.vCampus.entity.Role;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * 角色数据访问接口
 */
public interface IRoleDao extends IBaseDao<Role, Integer> {
    
    /**
     * 根据角色代码查找角色
     */
    Role findByCode(String roleCode, Connection conn);
    
    /**
     * 为角色分配权限
     */
    boolean assignPermission(Integer roleId, Integer permissionId, Connection conn);
    
    /**
     * 从角色移除权限
     */
    boolean removePermission(Integer roleId, Integer permissionId, Connection conn);
    
    /**
     * 获取角色的所有权限
     */
    Set<Permission> getRolePermissions(Integer roleId, Connection conn);
}
