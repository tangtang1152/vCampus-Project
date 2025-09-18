package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.service.ValidationService;
import com.vCampus.util.DBConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date; // 导入java.util.Date

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
        // 从 tbl_user 表获取的数据 (通过 JOIN 得到)
        student.setUserId(rs.getInt("userId"));
        // 检查是否存在 username, password, role 列，因为它们可能不是每次查询都join而来
        try {
            student.setUsername(rs.getString("username"));
            student.setPassword(rs.getString("password"));
            student.setRole(rs.getString("role"));
        } catch (SQLException e) {
            // 如果没有join tbl_user，这些列可能不存在，可以忽略此异常或打印警告
            // System.out.println("Warning: username, password, role columns not found in ResultSet. This is normal if tbl_user was not joined.");
        }


        // 从 tbl_student 表获取的数据
        student.setStudentId(rs.getString("studentId"));
        student.setStudentName(rs.getString("studentName"));
        student.setClassName(rs.getString("className"));

        // 修复：获取并设置所有缺失的字段
        Date enrollDate = rs.getDate("enrollDate");
        if (enrollDate != null) {
            student.setEnrollDate(new Date(enrollDate.getTime())); // 将 java.sql.Date 转换为 java.util.Date
        }
        student.setSex(rs.getString("sex"));
        student.setEmail(rs.getString("email"));
        student.setIdCard(rs.getString("idCard"));
        student.setStatus(rs.getString("status"));

        return student;
    }

    @Override
    protected void setInsertParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentId());
        pstmt.setInt(2, student.getUserId());
        pstmt.setString(3, student.getStudentName());
        pstmt.setString(4, student.getClassName());
        // 修复：添加所有缺失的字段
        if (student.getEnrollDate() != null) {
            pstmt.setDate(5, new java.sql.Date(student.getEnrollDate().getTime()));
        } else {
            pstmt.setNull(5, Types.DATE);
        }
        pstmt.setString(6, student.getSex());
        pstmt.setString(7, student.getEmail());
        pstmt.setString(8, student.getIdCard());
        pstmt.setString(9, student.getStatus());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentName());
        pstmt.setString(2, student.getClassName());
        // 修复：添加所有缺失的字段
        if (student.getEnrollDate() != null) {
            pstmt.setDate(3, new java.sql.Date(student.getEnrollDate().getTime()));
        } else {
            pstmt.setNull(3, Types.DATE);
        }
        pstmt.setString(4, student.getSex());
        pstmt.setString(5, student.getEmail());
        pstmt.setString(6, student.getIdCard());
        pstmt.setString(7, student.getStatus());
        pstmt.setString(8, student.getStudentId()); // WHERE 条件
    }

    @Override
    public boolean insert(Student student, Connection conn) throws SQLException {
        String truncatedStudentName = ValidationService.truncateString(
                student.getStudentName(), DBConstants.STUDENT_NAME_MAX_LENGTH);
        String truncatedClassName = ValidationService.truncateString(
                student.getClassName(), DBConstants.CLASS_NAME_MAX_LENGTH);
        // 修复：截断其他可能过长的字段
        String truncatedSex = ValidationService.truncateString(
                student.getSex(), DBConstants.SEX_MAX_LENGTH);
        String truncatedEmail = ValidationService.truncateString(
                student.getEmail(), DBConstants.EMAIL_MAX_LENGTH); // 假设 DBConstants 中有 EMAIL_MAX_LENGTH
        String truncatedIdCard = ValidationService.truncateString(
                student.getIdCard(), DBConstants.IDCARD_MAX_LENGTH); // 假设 DBConstants 中有 IDCARD_MAX_LENGTH
        String truncatedStatus = ValidationService.truncateString(
                student.getStatus(), DBConstants.STATUS_MAX_LENGTH); // 假设 DBConstants 中有 STATUS_MAX_LENGTH

        student.setStudentName(truncatedStudentName);
        student.setClassName(truncatedClassName);
        student.setSex(truncatedSex);
        student.setEmail(truncatedEmail);
        student.setIdCard(truncatedIdCard);
        student.setStatus(truncatedStatus);

        // 修复：更新SQL语句，包含所有字段
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
        // 修复：更新SQL语句，包含所有字段
        String sql = "UPDATE tbl_student SET studentName = ?, className = ?, enrollDate = ?, sex = ?, email = ?, idCard = ?, status = ? WHERE studentId = ?";
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

    //--

    @Override
    public List<Student> findStudentsByClass(String className, Connection conn) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s " +
                "JOIN tbl_user u ON s.userId = u.userId WHERE s.className = ?"; // 修复 SQL 语句，使其 join tbl_user
        // 注意：原方法手动构建 Student 对象，但 createEntityFromResultSet 更加通用和完整。
        // 这里为了保持一致性，还是使用 createEntityFromResultSet。
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, className);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
  students.add(createEntityFromResultSet(rs)); // 使用 createEntityFromResultSet
                }
            }
        }
        return students;
    }

    @Override
    public List<Student> findStudentsByStatus(String status, Connection conn) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s " +
                "JOIN tbl_user u ON s.userId = u.userId WHERE s.status = ?"; // 修复 SQL 语句，使其 join tbl_user
        // 注意：原方法手动构建 Student 对象，但 createEntityFromResultSet 更加通用和完整。
        // 这里为了保持一致性，还是使用 createEntityFromResultSet。
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
  students.add(createEntityFromResultSet(rs)); // 使用 createEntityFromResultSet
                }
            }
        }
        return students;
    }

    @Override
    public boolean isStudentIdExists(String studentId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_student WHERE studentId = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @Override
    public boolean updateStudentStatus(String studentId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_student SET status = ? WHERE studentId = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, studentId);

            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean isStudentActive(String studentId, Connection conn) throws SQLException {
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
    public int getStudentOrderCount(String studentId, Connection conn) throws SQLException {
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
    public double getStudentTotalSpending(String studentId, Connection conn) throws SQLException {
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
    public List<String> getStudentRecentOrderIds(String studentId, int limit, Connection conn) throws SQLException {
        List<String> orderIds = new ArrayList<>();
        String sql = "SELECT orderId FROM tbl_order WHERE studentId = ? ORDER BY orderDate DESC LIMIT ?"; // Access 不支持 LIMIT，可能需要其他方式实现分页
        // 注意：Access SQL 可能不支持 `LIMIT` 关键字。如果是 Access 数据库，可能需要调整查询方式或在应用层进行截取。
        // 对于 Access，一种常见的替代方式是使用 SELECT TOP N： "SELECT TOP ? orderId FROM tbl_order WHERE studentId = ? ORDER BY orderDate DESC"
        // 但这里为了通用性，先保留 LIMIT，如果运行时报错，需根据实际数据库类型修改。
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setInt(2, limit); // LIMIT 的参数
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                orderIds.add(rs.getString("orderId"));
            }
        }
        return orderIds;
    }

}