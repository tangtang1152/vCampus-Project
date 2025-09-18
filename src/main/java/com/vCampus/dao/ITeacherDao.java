package com.vCampus.dao;

import com.vCampus.entity.Teacher;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 教师数据访问对象接口
 */
public interface ITeacherDao extends IBaseDao<Teacher, String> {
    Teacher findByTeacherId(String teacherId, Connection conn) throws SQLException;
    Teacher findByUserId(Integer userId, Connection conn) throws SQLException;
    
    // 用户管理模块
    List<Teacher> findByDepartment(String departmentId, Connection conn);
}