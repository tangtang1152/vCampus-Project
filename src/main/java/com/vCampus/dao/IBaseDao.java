
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
    List<T> findAll(Connection conn) throws SQLException;
    boolean insert(T entity, Connection conn) throws SQLException;
    boolean update(T entity, Connection conn) throws SQLException;
    boolean delete(ID id, Connection conn) throws SQLException;
}
