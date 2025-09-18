package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;

public class StudentDaoImpl extends AbstractBaseDaoImpl<Student, String> implements IStudentDao {

    @Override
    protected String getTableName() {
        return "tbl_student";
    }

    @Override
    protected String getIdColumnName() {
        return "studentId";
    }

    @Override
    protected Student createEntityFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setUserId(rs.getInt("userId"));
        try { student.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        try { student.setPassword(rs.getString("password")); } catch (SQLException ignored) {}
        try { student.setRole(rs.getString("role")); } catch (SQLException ignored) {}
        student.setStudentId(rs.getString("studentId"));
        student.setStudentName(rs.getString("studentName"));
        student.setClassName(rs.getString("className"));
        return student;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        pstmt.setInt(2, student.getUserId());
        pstmt.setString(3, student.getStudentName());
        pstmt.setString(4, student.getClassName());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentName());
        pstmt.setString(2, student.getClassName());
        pstmt.setString(3, student.getStudentId());
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(
                student.getStudentName(), DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                student.getClassName(), DBConstants.CLASS_NAME_MAX_LENGTH);
        
        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);

        String sql = "INSERT INTO tbl_student (studentId, userId, studentName, className) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, student);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入学生记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Student student, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_student SET studentName = ?, className = ? WHERE studentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, student);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Student findByStudentId(String studentId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s " +
                    "JOIN tbl_user u ON s.userId = u.userId WHERE s.studentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s " +
                    "JOIN tbl_user u ON s.userId = u.userId WHERE s.userId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }
}