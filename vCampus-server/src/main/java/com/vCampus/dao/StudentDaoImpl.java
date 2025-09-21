package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository // <-- 添加
public class StudentDaoImpl extends AbstractBaseDaoImpl<Student, String> implements IStudentDao {
    
    @Override
    protected String getTableName() { return "tbl_student"; }

    @Override
    protected String getIdColumnName() { return "studentId"; }

    @Override
    protected Student createEntityFromResultSet(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getString("studentId"));
        s.setStudentName(rs.getString("studentName"));
        s.setClassName(rs.getString("className"));
        s.setUserId(rs.getInt("userId"));
        return s;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        pstmt.setString(2, student.getStudentName());
        pstmt.setString(3, student.getClassName());
        pstmt.setInt(4, student.getUserId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentName());
        pstmt.setString(2, student.getClassName());
        pstmt.setInt(3, student.getUserId());
        pstmt.setString(4, student.getStudentId());
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(
                student.getStudentName(), DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                student.getClassName(), DBConstants.CLASS_NAME_MAX_LENGTH);
        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);
        return super.insert(student, conn);
    }

    @Override
    public Student findByStudentId(String studentId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId WHERE s.studentId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId WHERE s.userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }
}