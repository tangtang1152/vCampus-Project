package com.vCampus.dao;

import com.vCampus.entity.SchoolClass;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 班级数据访问对象接口
 */
public interface ISchoolClassDao extends IBaseDao<SchoolClass, String> {
    List<SchoolClass> findByDepartmentId(String departmentId, Connection conn) throws SQLException;
    List<SchoolClass> findByClassName(String className, Connection conn) throws SQLException;
}