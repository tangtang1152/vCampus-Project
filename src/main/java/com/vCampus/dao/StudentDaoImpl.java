package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        student.setUsername(rs.getString("username"));
        student.setPassword(rs.getString("password"));
        student.setRole(rs.getString("role"));
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
    
  //-----------------------------------------------------------------------------------
    
    @Override
    public List<Student> findStudentsByClass(String className,Connection conn) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student WHERE className = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, className);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassName(rs.getString("className"));
                
                // 安全处理日期字段
                java.sql.Date enrollDate = rs.getDate("enrollDate");
                if (enrollDate != null) {
                    student.setEnrollDate(new Date(enrollDate.getTime()));
                }
                
                student.setStatus(rs.getString("status"));
                students.add(student);
            }
        }
        return students;
    }
    
    @Override
    public List<Student> findStudentsByStatus(String status,Connection conn) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student WHERE status = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassName(rs.getString("className"));
                
                // 安全处理日期字段
                java.sql.Date enrollDate = rs.getDate("enrollDate");
                if (enrollDate != null) {
                    student.setEnrollDate(new Date(enrollDate.getTime()));
                }
                
                student.setStatus(rs.getString("status"));
                students.add(student);
            }
        }
        return students;
    }
    
    @Override
    public boolean isStudentIdExists(String studentId,Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_student WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    @Override
    public boolean updateStudentStatus(String studentId, String status,Connection conn) throws SQLException {
        String sql = "UPDATE tbl_student SET status = ? WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, studentId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean isStudentActive(String studentId,Connection conn) throws SQLException {
        String sql = "SELECT status FROM tbl_student WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                return "正常".equals(status) || "active".equalsIgnoreCase(status);
            }
            return false;
        }
    }
    
    //---------------
    
    @Override
    public int getStudentOrderCount(String studentId,Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_order WHERE studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    @Override
    public double getStudentTotalSpending(String studentId,Connection conn) throws SQLException {
        String sql = "SELECT SUM(totalAmount) FROM tbl_order WHERE studentId = ? AND status IN ('已支付', '已完成')";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }
    
    @Override
    public List<String> getStudentRecentOrderIds(String studentId, int limit,Connection conn) throws SQLException {
        List<String> orderIds = new ArrayList<>();
        String sql = "SELECT orderId FROM tbl_order WHERE studentId = ? ORDER BY orderDate DESC LIMIT ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                orderIds.add(rs.getString("orderId"));
            }
        }
        return orderIds;
    }
    
}