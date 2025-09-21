package com.vCampus.dao;

import com.vCampus.entity.Choose;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 选课数据访问对象接口
 */
public interface IChooseDao extends IBaseDao<Choose, String> {
    List<Choose> findByStudentId(String studentId, Connection conn) throws SQLException;
    List<Choose> findBySubjectId(String subjectId, Connection conn) throws SQLException;
    Choose findByStudentAndSubject(String studentId, String subjectId, Connection conn) throws SQLException;
}