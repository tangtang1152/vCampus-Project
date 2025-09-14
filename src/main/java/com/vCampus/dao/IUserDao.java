package com.vCampus.dao;

import com.vCampus.entity.User;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 用户数据访问对象接口
 */
public interface IUserDao extends IBaseDao<User> {
    User findByUsername(String username, Connection conn) throws SQLException;
    boolean validateUser(String username, String password, Connection conn) throws SQLException;
}