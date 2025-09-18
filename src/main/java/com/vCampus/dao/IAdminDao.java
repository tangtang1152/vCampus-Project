package com.vCampus.dao;

import com.vCampus.entity.Admin;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 管理员数据访问对象接口
 */
public interface IAdminDao extends IBaseDao<Admin, String> {
    Admin findByAdminId(String adminId, Connection conn) throws SQLException;
    Admin findByUserId(Integer userId, Connection conn) throws SQLException;
}