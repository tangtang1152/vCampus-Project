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
    public java.util.List<Student> findAll(Connection conn) throws SQLException {
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s LEFT JOIN tbl_user u ON s.userId = u.userId ORDER BY s.studentId";
        java.util.List<Student> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(createEntityFromResultSet(rs));
            }
        }
        return list;
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
        // 扩展字段（列可能不存在，逐项尝试）
        try { java.sql.Date d = rs.getDate("enrollDate"); student.setEnrollDate(d==null?null:new java.util.Date(d.getTime())); } catch (SQLException ignored) {}
        try { student.setSex(rs.getString("sex")); } catch (SQLException ignored) {}
        try { student.setEmail(rs.getString("email")); } catch (SQLException ignored) {}
        try { student.setIdCard(rs.getString("idCard")); } catch (SQLException ignored) {}
        try { student.setStatus(rs.getString("status")); } catch (SQLException ignored) {}
        return student;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        pstmt.setInt(2, student.getUserId());
        pstmt.setString(3, student.getStudentName());
        pstmt.setString(4, student.getClassName());
        // 扩展字段（允许为空）
        if (hasColumn(pstmt.getConnection(), "tbl_student", "enrollDate")) {
            if (student.getEnrollDate() != null) pstmt.setDate(5, new java.sql.Date(student.getEnrollDate().getTime())); else pstmt.setNull(5, java.sql.Types.DATE);
            pstmt.setString(6, student.getSex());
            pstmt.setString(7, student.getEmail());
            pstmt.setString(8, student.getIdCard());
            pstmt.setString(9, student.getStatus());
        }
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Student student) throws SQLException {
        // 根据表结构动态设置参数
        boolean hasExt = hasColumn(pstmt.getConnection(), "tbl_student", "enrollDate");
        if (hasExt) {
            pstmt.setString(1, student.getStudentName());
            pstmt.setString(2, student.getClassName());
            if (student.getEnrollDate() != null) pstmt.setDate(3, new java.sql.Date(student.getEnrollDate().getTime())); else pstmt.setNull(3, java.sql.Types.DATE);
            pstmt.setString(4, student.getSex());
            pstmt.setString(5, student.getEmail());
            pstmt.setString(6, student.getIdCard());
            pstmt.setString(7, student.getStatus());
            pstmt.setString(8, student.getStudentId());
        } else {
            pstmt.setString(1, student.getStudentName());
            pstmt.setString(2, student.getClassName());
            pstmt.setString(3, student.getStudentId());
        }
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(
                student.getStudentName(), DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                student.getClassName(), DBConstants.CLASS_NAME_MAX_LENGTH);
        
        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);

        // 兼容两种表结构：精简结构与扩展结构
        String sql;
        boolean hasExt = hasColumn(conn, "tbl_student", "enrollDate");
        if (hasExt) {
            sql = "INSERT INTO tbl_student (studentId, userId, studentName, className, enrollDate, sex, email, idCard, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO tbl_student (studentId, userId, studentName, className) VALUES (?, ?, ?, ?)";
        }
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
        String sql;
        boolean hasExt = hasColumn(conn, "tbl_student", "enrollDate");
        if (hasExt) {
            sql = "UPDATE tbl_student SET studentName = ?, className = ?, enrollDate = ?, sex = ?, email = ?, idCard = ?, status = ? WHERE studentId = ?";
        } else {
            sql = "UPDATE tbl_student SET studentName = ?, className = ? WHERE studentId = ?";
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setUpdateParameters(pstmt, student);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private boolean hasColumn(Connection conn, String table, String column) {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(), column.toUpperCase())) {
            return rs.next();
        } catch (SQLException e) {
            return false;
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