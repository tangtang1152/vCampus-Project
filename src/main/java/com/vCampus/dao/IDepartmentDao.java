package com.vCampus.dao;

import com.vCampus.entity.Department;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 部门数据访问对象接口
 */
public interface IDepartmentDao extends IBaseDao<Department, String> {
    Department findByDepartmentName(String departmentName, Connection conn) throws SQLException;
}