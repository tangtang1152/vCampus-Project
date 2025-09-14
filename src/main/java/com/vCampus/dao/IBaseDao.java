package com.vCampus.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO接口基类
 * 定义通用的CRUD操作
 */
public interface IBaseDao<T> {
    T findById(int id, Connection conn) throws SQLException;
    List<T> findAll(Connection conn) throws SQLException;
    boolean insert(T entity, Connection conn) throws SQLException;
    boolean update(T entity, Connection conn) throws SQLException;
    boolean delete(int id, Connection conn) throws SQLException;
}