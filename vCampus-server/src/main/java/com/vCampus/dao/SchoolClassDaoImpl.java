package com.vCampus.dao;

import com.vCampus.entity.SchoolClass;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级数据访问对象实现类
 */
@Repository // <-- 添加这个注解
public class SchoolClassDaoImpl extends AbstractBaseDaoImpl<SchoolClass, String> implements ISchoolClassDao {

    @Override
    protected String getTableName() { return "tbl_class"; }

    @Override
    protected String getIdColumnName() { return "classId"; }

    @Override
    protected SchoolClass createEntityFromResultSet(ResultSet rs) throws SQLException {
        SchoolClass sc = new SchoolClass();
        sc.setClassId(rs.getString("classId"));
        sc.setClassName(rs.getString("className"));
        sc.setDepartmentId(rs.getString("departmentId"));
        return sc;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, SchoolClass schoolClass) throws SQLException {
        pstmt.setString(1, schoolClass.getClassName());
        pstmt.setString(2, schoolClass.getDepartmentId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, SchoolClass schoolClass) throws SQLException {
        pstmt.setString(1, schoolClass.getDepartmentId());
        pstmt.setString(2, schoolClass.getClassName());
    }

    @Override
    public boolean insert(SchoolClass schoolClass, Connection conn) throws SQLException {
        String truncatedClassName = ValidationService.truncateString(schoolClass.getClassName(), 
                DBConstants.CLASS_NAME_MAX_LENGTH);
        schoolClass.setClassName(truncatedClassName);
        return super.insert(schoolClass, conn);
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