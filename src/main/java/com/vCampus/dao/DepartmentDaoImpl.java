package com.vCampus.dao;

import com.vCampus.entity.Department;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;

/**
 * 部门数据访问对象实现类
 */
public class DepartmentDaoImpl extends AbstractBaseDaoImpl<Department, String> implements IDepartmentDao {

    @Override
    protected String getTableName() {
        return "department";
    }

    @Override
    protected String getIdColumnName() {
        return "departmentId";
    }

    @Override
    protected Department createEntityFromResultSet(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setDepartmentId(rs.getString("departmentId"));
        department.setDepartmentName(rs.getString("departmentName"));
        return department;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Department department) throws SQLException {
        pstmt.setString(1, department.getDepartmentId());
        pstmt.setString(2, department.getDepartmentName());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Department department) throws SQLException {
        pstmt.setString(1, department.getDepartmentName());
        pstmt.setString(2, department.getDepartmentId());
    }

    @Override
    public boolean insert(Department department, Connection conn) throws SQLException {
        String truncatedDeptName = ValidationService.truncateString(department.getDepartmentName(), 
                DBConstants.DEPARTMENT_NAME_MAX_LENGTH);
        
        department.setDepartmentName(truncatedDeptName);

        String sql = "INSERT INTO department (departmentId, departmentName) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, department);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入部门记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Department department, Connection conn) throws SQLException {
        String sql = "UPDATE department SET departmentName = ? WHERE departmentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, department);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Department findByDepartmentName(String departmentName, Connection conn) throws SQLException {
        String sql = "SELECT * FROM department WHERE departmentName = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, departmentName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // 由于继承了AbstractBaseDaoImpl，findById方法已经实现，这里不需要再写
    // 但为了保持接口一致性，我们可以添加一个别名方法
    public Department findByDepartmentId(String departmentId, Connection conn) throws SQLException {
        return findById(departmentId, conn);
    }
}