package com.vCampus.dao;

import com.vCampus.entity.Student;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 学生数据访问对象接口
 */
public interface IStudentDao extends IBaseDao<Student,String> {
    Student findByStudentId(String studentId, Connection conn) throws SQLException;
    Student findByUserId(Integer userId, Connection conn) throws SQLException;
}