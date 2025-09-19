package com.vCampus.dao;

import com.vCampus.entity.SchoolClass;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级数据访问对象实现类
 */
public class SchoolClassDaoImpl extends AbstractBaseDaoImpl<SchoolClass, String> implements ISchoolClassDao {

    @Override
    protected String getTableName() {
        return "tbl_class";
    }

    @Override
    protected String getIdColumnName() {
        return "classId";
    }

    @Override
    protected SchoolClass createEntityFromResultSet(ResultSet rs) throws SQLException {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassId(rs.getString("classId"));
        schoolClass.setClassName(rs.getString("className"));
        schoolClass.setDepartmentId(rs.getString("departmentId"));
        // 处理数据库中的NULL值
        if (rs.wasNull()) {
            schoolClass.setDepartmentId("");
        }
        return schoolClass;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, SchoolClass schoolClass) throws SQLException {
        pstmt.setString(1, schoolClass.getClassId());
        pstmt.setString(2, schoolClass.getClassName());
        
        // departmentId可以为null或空字符串
        if (schoolClass.getDepartmentId() != null && !schoolClass.getDepartmentId().isEmpty()) {
            pstmt.setString(3, schoolClass.getDepartmentId());
        } else {
            pstmt.setNull(3, Types.VARCHAR);
        }
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, SchoolClass schoolClass) throws SQLException {
        pstmt.setString(1, schoolClass.getClassName());
        
        // departmentId可以为null或空字符串
        if (schoolClass.getDepartmentId() != null && !schoolClass.getDepartmentId().isEmpty()) {
            pstmt.setString(2, schoolClass.getDepartmentId());
        } else {
            pstmt.setNull(2, Types.VARCHAR);
        }
        
        pstmt.setString(3, schoolClass.getClassId());
    }

    @Override
    public boolean insert(SchoolClass schoolClass, Connection conn) throws SQLException {
        String truncatedClassName = ValidationService.truncateString(schoolClass.getClassName(), 
                DBConstants.CLASS_NAME_MAX_LENGTH);
        
        schoolClass.setClassName(truncatedClassName);

        String sql = "INSERT INTO tbl_class (classId, className, departmentId) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, schoolClass);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入班级记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(SchoolClass schoolClass, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_class SET className = ?, departmentId = ? WHERE classId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, schoolClass);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<SchoolClass> findByDepartmentId(String departmentId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_class WHERE departmentId = ?";
        List<SchoolClass> classes = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(createEntityFromResultSet(rs));
                }
            }
        }
        return classes;
    }

    @Override
    public List<SchoolClass> findByClassName(String className, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_class WHERE className LIKE ?";
        List<SchoolClass> classes = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + className + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(createEntityFromResultSet(rs));
                }
            }
        }
        return classes;
    }

    // 由于继承了AbstractBaseDaoImpl，findById方法已经实现，这里添加一个别名方法保持兼容性
    public SchoolClass findByClassId(String classId, Connection conn) throws SQLException {
        return findById(classId, conn);
    }
}