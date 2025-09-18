package com.vCampus.dao;

import com.vCampus.entity.Subject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 课程数据访问对象接口
 */
public interface ISubjectDao extends IBaseDao<Subject, String> {
    List<Subject> findByTeacherId(String teacherId, Connection conn) throws SQLException;
    List<Subject> findBySubjectName(String subjectName, Connection conn) throws SQLException;
}