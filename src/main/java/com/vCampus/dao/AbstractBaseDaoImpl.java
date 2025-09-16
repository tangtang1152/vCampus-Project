package com.vCampus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象的BaseDao实现，提供通用的CRUD操作
 * @param <T> 实体类型
 * @param <ID> ID类型
 */
public abstract class AbstractBaseDaoImpl<T, ID> implements IBaseDao<T, ID> {
    
    protected abstract String getTableName();
    protected abstract String getIdColumnName();
    protected abstract T createEntityFromResultSet(ResultSet rs) throws SQLException;
    protected abstract void setInsertParameters(PreparedStatement pstmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement pstmt, T entity) throws SQLException;

    @Override
    public T findById(ID id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<T> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        List<T> entities = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                entities.add(createEntityFromResultSet(rs));
            }
        }
        return entities;
    }

    @Override
    public boolean insert(T entity, Connection conn) throws SQLException {
        // 子类需要实现具体的插入逻辑
        return false;
    }

    @Override
    public boolean update(T entity, Connection conn) throws SQLException {
        // 子类需要实现具体的更新逻辑
        return false;
    }

    @Override
    public boolean delete(ID id, Connection conn) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}