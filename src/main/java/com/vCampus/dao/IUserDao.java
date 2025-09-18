package com.vCampus.dao;

import com.vCampus.entity.User;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

/**
 * 用户数据访问对象接口
 */
public interface IUserDao extends IBaseDao<User,Integer> {
    User findByUsername(String username, Connection conn) throws SQLException;
    //登陆权限认证 （用户名密码）
    boolean validateUser(String username, String password, Connection conn) throws SQLException;
    
    // 新增的方法
    boolean updateUserPassword(Integer userId, String newPassword,Connection conn) throws SQLException;
    boolean updateUserRole(Integer userId, String newRole,Connection conn) throws SQLException;
    List<User> getUsersByRole(String role,Connection conn) throws SQLException;
    boolean usernameExists(String username,Connection conn) throws SQLException;
    int getTotalUserCount(Connection conn) throws SQLException;
    int getUserCountByRole(String role,Connection conn) throws SQLException;
   // List<User> searchUsers(String keyword) throws SQLException;
}