package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;
import com.vCampus.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问对象实现类
 */
public class StudentDao implements IStudentDao {

    @Override
    public Student findById(String studentId, Connection conn) throws SQLException {
        return findByStudentId(studentId, conn);
    }

    @Override
    public List<Student> findAll(Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId";
        List<Student> students = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                students.add(createStudentFromResultSet(rs));
            }
        }
        return students;
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(student.getStudentName(), 
                DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(student.getClassName(), 
                DBConstants.CLASS_NAME_MAX_LENGTH);

        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);

        String sql = "INSERT INTO tbl_student (studentId, userId, studentName, className) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getStudentId());
            pstmt.setInt(2, student.getUserId());
            pstmt.setString(3, student.getStudentName());
            pstmt.setString(4, student.getClassName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean update(Student student, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_student SET studentName = ?, className = ? WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getStudentName());
            pstmt.setString(2, student.getClassName());
            pstmt.setString(3, student.getStudentId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(String studentId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_student WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Student findByStudentId(String studentId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId WHERE s.studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createStudentFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId WHERE s.userId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createStudentFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Student createStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setUserId(rs.getInt("userId"));
        student.setUsername(rs.getString("username"));
        student.setPassword(rs.getString("password"));
        student.setRole(rs.getString("role"));
        student.setStudentId(rs.getString("studentId"));
        student.setStudentName(rs.getString("studentName"));
        student.setClassName(rs.getString("className"));
        return student;
    }
}