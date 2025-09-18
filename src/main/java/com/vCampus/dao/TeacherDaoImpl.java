package com.vCampus.dao;

import com.vCampus.entity.Teacher;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDaoImpl extends AbstractBaseDaoImpl<Teacher, String> implements ITeacherDao {

    @Override
    protected String getTableName() {
        return "tbl_teacher";
    }

    @Override
    protected String getIdColumnName() {
        return "teacherId";
    }

    @Override
    protected Teacher createEntityFromResultSet(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setUserId(rs.getInt("userId"));
        teacher.setUsername(rs.getString("username"));
        teacher.setPassword(rs.getString("password"));
        teacher.setRole(rs.getString("role"));
        teacher.setTeacherId(rs.getString("teacherId"));
        teacher.setTeacherName(rs.getString("teacherName"));
        teacher.setDepartmentId(rs.getString("DepartmentId"));
        return teacher;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Teacher teacher) throws SQLException {
        pstmt.setString(1, teacher.getTeacherId());
        pstmt.setInt(2, teacher.getUserId());
        pstmt.setString(3, teacher.getTeacherName());
        pstmt.setString(4, teacher.getDepartmentId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Teacher teacher) throws SQLException {
        pstmt.setString(1, teacher.getTeacherName());
        pstmt.setString(2, teacher.getDepartmentId());
        pstmt.setString(3, teacher.getTeacherId());
    }

    @Override
    public boolean insert(Teacher teacher, Connection conn) throws SQLException {
        String truncatedTeacherName = ValidationService.truncateString(
                teacher.getTeacherName(), DBConstants.TEACHER_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                teacher.getDepartmentId(), DBConstants.CLASS_NAME_MAX_LENGTH);
        
        teacher.setTeacherName(truncatedTeacherName);
        teacher.setDepartmentId(truncatedClassName);

        String sql = "INSERT INTO tbl_teacher (teacherId, userId, teacherName, DepartmentName) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setInsertParameters(pstmt, teacher);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("插入教师记录失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Teacher teacher, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_teacher SET teacherName = ?, DepartmentName = ? WHERE teacherId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, teacher);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Teacher findByTeacherId(String teacherId, Connection conn) throws SQLException {
        String sql = "SELECT t.*, u.username, u.password, u.role FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId WHERE t.teacherId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createEntityFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Teacher findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT t.*, u.username, u.password, u.role FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId WHERE t.userId = ?";
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
    
    @Override
    public List<Teacher> findByDepartment(String departmentId, Connection conn) {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT t.*, u.username, u.password, u.role FROM tbl_teacher t " +
                    "JOIN tbl_user u ON t.userId = u.userId WHERE t.DepartmentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    teachers.add(createEntityFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("根据部门查找教师失败: " + e.getMessage());
            e.printStackTrace();
        }
        return teachers;
    }
}