package com.vCampus.service;

import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 抽象基础服务实现
 * 提供通用的CRUD操作实现
 */
public abstract class AbstractBaseServiceImpl<T, ID> implements IBaseService<T,ID>{
	
	/**
	 * 事务由基类统一处理
	 * 子类只需要实现具体的SQL逻辑
	 */	
    protected abstract T doGetBySelfId(ID id, Connection conn) throws Exception;
    protected abstract List<T> doGetAll(Connection conn) throws Exception;
    protected abstract boolean doAdd(T entity, Connection conn) throws Exception;
    protected abstract boolean doUpdate(T entity, Connection conn) throws Exception;
    protected abstract boolean doDelete(ID id, Connection conn) throws Exception;
    protected abstract boolean doExists(ID id, Connection conn) throws Exception;
    
    @Override    
    public T getBySelfId(ID id) {
        try {
            return TransactionManager.executeInTransaction(conn -> doGetBySelfId(id,conn));
        } catch (Exception e) {
            handleException("获取信息失败", e);
            return null;
        }
    }
    
    @Override    
    public List<T> getAll() {
        try {
            return TransactionManager.executeInTransaction(conn -> doGetAll(conn));
        } catch (Exception e) {
            handleException("获取所有信息失败", e);
            return List.of();
        }
    }
    
    @Override    
    public boolean add(T entity) {
        try {
            return TransactionManager.executeInTransaction(conn -> doAdd(entity,conn));
        } catch (Exception e) {
            handleException("添加信息失败", e);
            return false;
        }
    }
    
    @Override    
    public boolean update(T entity) {
        try {
            return TransactionManager.executeInTransaction(conn -> doUpdate(entity,conn));
        } catch (Exception e) {
            handleException("更新信息失败", e);
            return false;
        }
    }
    
    @Override    
    public boolean delete(ID id) {
        try {
            return TransactionManager.executeInTransaction(conn -> doDelete(id,conn));
        } catch (Exception e) {
            handleException("完整删除信息失败", e);
            return false;
        }
    }
    
    @Override    
    public boolean exists(ID id) {
        try {
            return TransactionManager.executeInTransaction(conn -> doExists(id,conn));
        } catch (Exception e) {
            handleException("检查存在性失败", e);
            return false;
        }
    }
        
    protected void handleException(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}