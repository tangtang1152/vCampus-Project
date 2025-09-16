package com.vCampus.service;

import java.util.List;

/**
 * 基础服务接口
 * 定义通用的CRUD操作
 */
public interface IBaseService<T, ID> {
    
	//自身Id studentId teacherId adminId
    T getBySelfId(ID id);
    
    List<T> getAll();
    
    boolean add(T entity);
    
    //也更新tbl_user
    boolean update(T entity);
    
    //也删tbl_user
    boolean delete(ID id);
    
    //此Id记录是否存在
    boolean exists(ID id);
}