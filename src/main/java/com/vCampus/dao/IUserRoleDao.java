package com.vCampus.dao;

import com.vCampus.entity.UserRole;

import java.sql.Connection;
import java.util.List;

/**
 * 用户角色关联数据访问接口
 */
public interface IUserRoleDao extends IBaseDao<UserRole, Integer> {
    
    /**
     * 根据用户ID查找用户角色
     */
    List<UserRole> findByUserId(Integer userId, Connection conn);
    
    /**
     * 根据用户ID和角色ID删除用户角色
     */
    boolean deleteByUserAndRole(Integer userId, Integer roleId, Connection conn);
    
    /**
     * 根据用户ID和角色代码删除用户角色
     */
    boolean deleteByUserAndRoleCode(Integer userId, String roleCode, Connection conn);
    
    /**
     * 检查用户是否拥有指定角色
     */
    boolean hasRole(Integer userId, String roleCode, Connection conn);
    
    /**
     * 根据角色代码查找用户角色
     */
    List<UserRole> findByRoleCode(String roleCode, Connection conn);
}
