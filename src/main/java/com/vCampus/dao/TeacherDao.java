package com.vCampus.dao;

import com.vCampus.entity.Teacher;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;
import com.vCampus.util.DBUtil;
import java.sql.*;

/**
 * 教师数据访问对象实现类
 */
public class TeacherDao implements ITeacherDao {

    @Override
    public Teacher findById(String teacherId, Connection conn) throws SQLException {
        return findByTeacherId(teacherId, conn);
    }

    @Override
    public java.util.List<Teacher> findAll(Connection conn) throws SQLException {
        String sql = "SELECT t.*, u.username, u.password, u.role " +
                    "FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId";
        java.util.List<Teacher> teachers = new java.util.ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                teachers.add(createTeacherFromResultSet(rs));
            }
        }
        return teachers;
    }

    @Override
    public boolean insert(Teacher teacher, Connection conn) throws SQLException {
        String truncatedTeacherName = ValidationService.truncateString(
            teacher.getTeacherName(), DBConstants.TEACHER_NAME_MAX_LENGTH);
        String truncatedTechnical = ValidationService.truncateString(
            teacher.getTechnical(), DBConstants.TECHNICAL_MAX_LENGTH);
        String truncatedDepartmentId = ValidationService.truncateString(
            teacher.getDepartmentId(), DBConstants.DEPARTMENT_ID_MAX_LENGTH);
        
        teacher.setTeacherName(truncatedTeacherName);
        teacher.setTechnical(truncatedTechnical);
        teacher.setDepartmentId(truncatedDepartmentId);

        String sql = "INSERT INTO tbl_teacher (teacherId, userId, teacherName, sex, technical, departmentId) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getTeacherId());
            pstmt.setInt(2, teacher.getUserId());
            pstmt.setString(3, teacher.getTeacherName());
            pstmt.setString(4, teacher.getSex());
            pstmt.setString(5, teacher.getTechnical());
            pstmt.setString(6, teacher.getDepartmentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean update(Teacher teacher, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_teacher SET teacherName = ?, sex = ?, technical = ?, departmentId = ? " +
                     "WHERE teacherId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getTeacherName());
            pstmt.setString(2, teacher.getSex());
            pstmt.setString(3, teacher.getTechnical());
            pstmt.setString(4, teacher.getDepartmentId());
            pstmt.setString(5, teacher.getTeacherId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(String teacherId, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_teacher WHERE teacherId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Teacher findByTeacherId(String teacherId, Connection conn) throws SQLException {
        String sql = "SELECT t.*, u.username, u.password, u.role " +
                    "FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId " +
                    "WHERE t.teacherId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createTeacherFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Teacher findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT t.*, u.username, u.password, u.role " +
                    "FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId " +
                    "WHERE t.userId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createTeacherFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Teacher createTeacherFromResultSet(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        
        // 设置从User表继承的属性
        teacher.setUserId(rs.getInt("userId"));
        teacher.setUsername(rs.getString("username"));
        teacher.setPassword(rs.getString("password"));
        teacher.setRole(rs.getString("role"));
        
        // 设置Teacher特有的属性
        teacher.setTeacherId(rs.getString("teacherId"));
        teacher.setTeacherName(rs.getString("teacherName"));
        teacher.setSex(rs.getString("sex"));
        teacher.setTechnical(rs.getString("technical"));
        teacher.setDepartmentId(rs.getString("departmentId"));
        
        return teacher;
    }
}