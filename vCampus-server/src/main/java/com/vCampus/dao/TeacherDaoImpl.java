package com.vCampus.dao;

import com.vCampus.entity.Teacher;
import com.vCampus.util.ValidationService;
import com.vCampus.util.DBConstants;
import org.springframework.stereotype.Repository; // <-- 添加这个

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository // <-- 添加
public class TeacherDaoImpl extends AbstractBaseDaoImpl<Teacher, String> implements ITeacherDao {

    @Override
    protected String getTableName() { return "tbl_teacher"; }

    @Override
    protected String getIdColumnName() { return "teacherId"; }

    @Override
    protected Teacher createEntityFromResultSet(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setTeacherId(rs.getString("teacherId"));
        t.setTeacherName(rs.getString("teacherName"));
        t.setSex(rs.getString("sex"));
        t.setTechnical(rs.getString("technical"));
        t.setDepartmentId(rs.getString("departmentId"));
        t.setUserId(rs.getInt("userId"));
        return t;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Teacher teacher) throws SQLException {
        pstmt.setString(1, teacher.getTeacherId());
        pstmt.setString(2, teacher.getTeacherName());
        pstmt.setString(3, teacher.getSex());
        pstmt.setString(4, teacher.getTechnical());
        pstmt.setString(5, teacher.getDepartmentId());
        pstmt.setInt(6, teacher.getUserId());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Teacher teacher) throws SQLException {
        pstmt.setString(1, teacher.getTeacherName());
        pstmt.setString(2, teacher.getSex());
        pstmt.setString(3, teacher.getTechnical());
        pstmt.setString(4, teacher.getDepartmentId());
        pstmt.setInt(5, teacher.getUserId());
        pstmt.setString(6, teacher.getTeacherId());
    }

    @Override
    public boolean insert(Teacher teacher, Connection conn) throws SQLException {
        String truncatedTeacherName = ValidationService.truncateString(
                teacher.getTeacherName(), DBConstants.TEACHER_NAME_MAX_LENGTH);
        String truncatedSex = ValidationService.truncateString(
                teacher.getSex(), DBConstants.SEX_MAX_LENGTH);
        String truncatedTechnical = ValidationService.truncateString(
                teacher.getTechnical(), DBConstants.TECHNICAL_MAX_LENGTH);
        String truncatedDeptId = ValidationService.truncateString(
                teacher.getDepartmentId(), DBConstants.DEPARTMENT_ID_MAX_LENGTH);
        teacher.setTeacherName(truncatedTeacherName);
        teacher.setSex(truncatedSex);
        teacher.setTechnical(truncatedTechnical);
        teacher.setDepartmentId(truncatedDeptId);
        return super.insert(teacher, conn);
    }

    @Override
    public boolean update(Teacher teacher, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_teacher SET teacherName = ?, sex = ?, technical = ?, departmentId = ? WHERE teacherId = ?";
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
}