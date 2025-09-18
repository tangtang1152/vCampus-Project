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
        // 基础字段（来自 tbl_student）
        if (hasColumn(rs, "userId")) student.setUserId((Integer) rs.getObject("userId"));
        if (hasColumn(rs, "studentId")) student.setStudentId(rs.getString("studentId"));
        if (hasColumn(rs, "studentName")) student.setStudentName(rs.getString("studentName"));
        if (hasColumn(rs, "className")) student.setClassName(rs.getString("className"));

        // 可选字段（在 findAll 不一定存在）
        if (hasColumn(rs, "enrollDate")) {
            java.sql.Date d = rs.getDate("enrollDate");
            if (d != null) student.setEnrollDate(new java.util.Date(d.getTime()));
        }
        if (hasColumn(rs, "sex")) student.setSex(rs.getString("sex"));
        if (hasColumn(rs, "email")) student.setEmail(rs.getString("email"));
        if (hasColumn(rs, "idCard")) student.setIdCard(rs.getString("idCard"));
        if (hasColumn(rs, "status")) student.setStatus(rs.getString("status"));

        // 来自 tbl_user 的联接字段（仅在 join 时可用）
        if (hasColumn(rs, "username")) student.setUsername(rs.getString("username"));
        if (hasColumn(rs, "password")) student.setPassword(rs.getString("password"));
        if (hasColumn(rs, "role")) student.setRole(rs.getString("role"));
        return student;
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        if (student.getUserId() == null) pstmt.setNull(2, java.sql.Types.INTEGER); else pstmt.setInt(2, student.getUserId());
        pstmt.setString(3, student.getStudentName());
        pstmt.setString(4, student.getClassName());
        // 可选字段
        if (student.getEnrollDate() != null) pstmt.setDate(5, new java.sql.Date(student.getEnrollDate().getTime())); else pstmt.setNull(5, java.sql.Types.DATE);
        pstmt.setString(6, student.getSex());
        pstmt.setString(7, student.getEmail());
        pstmt.setString(8, student.getIdCard());
        pstmt.setString(9, student.getStatus());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentName());
        pstmt.setString(2, student.getClassName());
        pstmt.setString(3, student.getSex());
        pstmt.setString(4, student.getEmail());
        pstmt.setString(5, student.getIdCard());
        if (student.getEnrollDate() != null) pstmt.setDate(6, new java.sql.Date(student.getEnrollDate().getTime())); else pstmt.setNull(6, java.sql.Types.DATE);
        pstmt.setString(7, student.getStatus());
        pstmt.setString(8, student.getStudentId());
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(
                student.getStudentName(), DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                student.getClassName(), DBConstants.CLASS_NAME_MAX_LENGTH);
        
        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);

        String sql = "INSERT INTO tbl_student (studentId, userId, studentName, className, enrollDate, sex, email, idCard, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        String sql = "UPDATE tbl_student SET studentName = ?, className = ?, sex = ?, email = ?, idCard = ?, enrollDate = ?, status = ? WHERE studentId = ?";
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
    
    @Override
    public List<Student> findByClass(String className, Connection conn) {
        try {
            return findStudentsByClass(className, conn);
        } catch (SQLException e) {
            System.err.println("根据班级查找学生失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
}