package com.vCampus.dao;

import com.vCampus.entity.Permission;

import java.sql.Connection;

/**
 * 权限数据访问接口
 */
public interface IPermissionDao extends IBaseDao<Permission, Integer> {
    
    /**
     * 根据权限代码查找权限
     */
    Permission findByCode(String permissionCode, Connection conn);
}
