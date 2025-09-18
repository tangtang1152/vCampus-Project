package com.vCampus.dao;

import com.vCampus.entity.Teacher;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 教师数据访问对象接口
 */
public interface ITeacherDao extends IBaseDao<Teacher, String> {
    Teacher findByTeacherId(String teacherId, Connection conn) throws SQLException;
    Teacher findByUserId(Integer userId, Connection conn) throws SQLException;
}