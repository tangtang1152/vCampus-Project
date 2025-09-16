package com.vCampus.service;

import java.util.List;

/**
 * 基础服务接口
 * 定义通用的CRUD操作
 */
public interface IBaseService<T, ID> {
    
    T getById(ID id);
    
    List<T> getAll();
    
    boolean add(T entity);
    
    boolean update(T entity);
    
    boolean delete(ID id);
    
    boolean exists(ID id);
}