package com.vCampus.dao;

import com.vCampus.entity.Student;
import com.vCampus.util.DBUtil;
import java.sql.*;

/**
 * 学生数据访问对象类
 * 负责与数据库中的tbl_student表进行交互
 * 提供对学生数据的CRUD（创建、读取、更新、删除）操作
 */
public class StudentDao {
    
    /**
     * 根据学号查找学生
     * 
     * @param studentId 要查找的学生学号
     * @return 找到的学生对象，如果未找到则返回null
     * @throws SQLException 如果数据库操作发生错误
     */
    public static Student findByStudentId(int studentId) throws SQLException {
        // SQL查询语句，使用连接查询获取用户和学生信息
        String sql = "SELECT s.*, u.username, u.password, u.role " +
                     "FROM tbl_student s " +
                     "JOIN tbl_user u ON s.userId = u.userId " +
                     "WHERE s.studentId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数
            pstmt.setInt(1, studentId);
            
            // 执行查询并处理结果
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 创建并返回学生对象
                    return createStudentFromResultSet(rs);
                }
            }
        }
        
        // 如果没有找到匹配的学生，返回null
        return null;
    }
    
    /**
     * 根据用户ID查找学生
     * 
     * @param userId 要查找的用户ID
     * @return 找到的学生对象，如果未找到则返回null
     * @throws SQLException 如果数据库操作发生错误
     */
    public static Student findByUserId(int userId) throws SQLException {
        // SQL查询语句，使用连接查询获取用户和学生信息
        String sql = "SELECT s.*, u.username, u.password, u.role " +
                     "FROM tbl_student s " +
                     "JOIN tbl_user u ON s.userId = u.userId " +
                     "WHERE s.userId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数
            pstmt.setInt(1, userId);
            
            // 执行查询并处理结果
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 创建并返回学生对象
                    return createStudentFromResultSet(rs);
                }
            }
        }
        
        // 如果没有找到匹配的学生，返回null
        return null;
    }
    
    /**
     * 创建学生记录
     * 
     * @param student 要创建的学生对象
     * @return 创建成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean createStudent(Student student) throws SQLException {
        String sql = "INSERT INTO tbl_student (studentId, userId, studentName, className) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, student.getStudentId());
            pstmt.setInt(2, student.getUserId());
            pstmt.setString(3, student.getStudentName());
            pstmt.setString(4, student.getClassName());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("插入学生信息成功: " + student.getStudentId());
                return true;
            } else {
                System.out.println("插入学生信息失败，受影响的行数为0");
                return false;
            }
        }
    }
    
    /**
     * 更新学生信息
     * 
     * @param student 要更新的学生对象
     * @return 更新成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean updateStudent(Student student) throws SQLException {
        // SQL更新语句
        String sql = "UPDATE tbl_student SET studentName = ?, className = ? WHERE studentId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置更新参数
            pstmt.setString(1, student.getStudentName());
            pstmt.setString(2, student.getClassName());
            pstmt.setInt(3, student.getStudentId());
            
            // 执行更新操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * 删除学生记录
     * 
     * @param studentId 要删除的学生学号
     * @return 删除成功返回true，否则返回false
     * @throws SQLException 如果数据库操作发生错误
     */
    public static boolean deleteStudent(int studentId) throws SQLException {
        // SQL删除语句
        String sql = "DELETE FROM tbl_student WHERE studentId = ?";
        
        // 使用try-with-resources确保资源正确关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置删除参数
            pstmt.setInt(1, studentId);
            
            // 执行删除操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * 从结果集创建学生对象
     * 辅助方法，用于将数据库结果集转换为Student对象
     * 
     * @param rs 数据库查询结果集
     * @return 从结果集创建的学生对象
     * @throws SQLException 如果读取结果集时发生错误
     */
    private static Student createStudentFromResultSet(ResultSet rs) throws SQLException {
        // 创建学生对象
        Student student = new Student();
        
        // 设置从User表继承的属性
        student.setUserId(rs.getInt("userId"));
        student.setUsername(rs.getString("username"));
        student.setPassword(rs.getString("password"));
        student.setRole(rs.getString("role"));
        
        // 设置Student特有的属性
        student.setStudentId(rs.getInt("studentId"));
        student.setStudentName(rs.getString("studentName"));
        student.setClassName(rs.getString("className"));
        
        return student;
    }
}