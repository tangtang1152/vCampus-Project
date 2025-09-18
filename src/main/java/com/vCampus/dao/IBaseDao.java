package com.vCampus.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO接口基类
 * 支持泛型ID类型
 * 定义通用的CRUD操作
 *
 * @param <T> 实体类型
 * @param <ID> ID类型
 */

public interface IBaseDao<T,ID> {
    T findById(ID id, Connection conn) throws SQLException;
    //从数据库中查询所有角色（学生/教师/管理员）信息及其关联的用户账户信息
    //返回包含所有学生/教师/管理员信息的列表
    List<T> findAll(Connection conn) throws SQLException;
    boolean insert(T entity, Connection conn) throws SQLException;
    boolean update(T entity, Connection conn) throws SQLException;
    boolean delete(ID id, Connection conn) throws SQLException;
}