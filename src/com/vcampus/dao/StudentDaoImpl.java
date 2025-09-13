package com.vcampus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.vcampus.entity.Student;
import util.DBUtil;
import java.util.Date;

public class StudentDaoImpl implements IStudentDao {
    
    @Override
    public boolean insertStudent(Student student) throws SQLException {
        String sql = "INSERT INTO tbl_student (studentId, userId, studentName, classId, enrollDate, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getStudentId());
            pstmt.setInt(2, student.getUserId());
            pstmt.setString(3, student.getStudentName());
            pstmt.setString(4, student.getClassId());
            
            // 安全处理日期字段
            if (student.getEnrollDate() != null) {
                pstmt.setDate(5, new java.sql.Date(student.getEnrollDate().getTime()));
            } else {
                pstmt.setNull(5, java.sql.Types.DATE);
            }
            
            pstmt.setString(6, student.getStatus());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public Student findStudentById(String studentId) throws SQLException {
        String sql = "SELECT * FROM tbl_student WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassId(rs.getString("classId"));
                
                // 安全处理日期字段
                java.sql.Date enrollDate = rs.getDate("enrollDate");
                if (enrollDate != null) {
                    student.setEnrollDate(new Date(enrollDate.getTime()));
                }
                
                student.setStatus(rs.getString("status"));
                return student;
            }
            return null;
        }
    }
    
    @Override
    public List<Student> findAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassId(rs.getString("classId"));
                
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
    public boolean isStudentIdExists(String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_student WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    @Override
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE tbl_student SET userId = ?, studentName = ?, classId = ?, " +
                    "enrollDate = ?, status = ? WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, student.getUserId());
            pstmt.setString(2, student.getStudentName());
            pstmt.setString(3, student.getClassId());
            
            // 安全处理日期字段
            if (student.getEnrollDate() != null) {
                pstmt.setDate(4, new java.sql.Date(student.getEnrollDate().getTime()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }
            
            pstmt.setString(5, student.getStatus());
            pstmt.setString(6, student.getStudentId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM tbl_student WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public List<Student> findStudentsByClass(String classId) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student WHERE classId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, classId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassId(rs.getString("classId"));
                
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
    public List<Student> findStudentsByStatus(String status) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student WHERE status = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassId(rs.getString("classId"));
                
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
    
    
 // 在 StudentDaoImpl 类中添加以下商店模块相关的方法：

    @Override
    public Student findStudentByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM tbl_student WHERE userId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getString("studentId"));
                student.setUserId(rs.getInt("userId"));
                student.setStudentName(rs.getString("studentName"));
                student.setClassId(rs.getString("classId"));
                
                // 安全处理日期字段
                java.sql.Date enrollDate = rs.getDate("enrollDate");
                if (enrollDate != null) {
                    student.setEnrollDate(new Date(enrollDate.getTime()));
                }
                
                student.setStatus(rs.getString("status"));
                return student;
            }
            return null;
        }
    }

    @Override
    public int getStudentOrderCount(String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_order WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public double getStudentTotalSpending(String studentId) throws SQLException {
        String sql = "SELECT SUM(totalAmount) FROM tbl_order WHERE studentId = ? AND status IN ('已支付', '已完成')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    @Override
    public List<String> getStudentRecentOrderIds(String studentId, int limit) throws SQLException {
        List<String> orderIds = new ArrayList<>();
        String sql = "SELECT orderId FROM tbl_order WHERE studentId = ? ORDER BY orderDate DESC LIMIT ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    public boolean updateStudentStatus(String studentId, String status) throws SQLException {
        String sql = "UPDATE tbl_student SET status = ? WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, studentId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean isStudentActive(String studentId) throws SQLException {
        String sql = "SELECT status FROM tbl_student WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                return "正常".equals(status) || "active".equalsIgnoreCase(status);
            }
            return false;
        }
    }

    /**
     * 获取学生购物统计信息（订单数量、总消费金额）
     * @param studentId 学生ID
     * @return 包含统计信息的数组 [订单数量, 总消费金额]
     * @throws SQLException
     */
    public Object[] getStudentShoppingStats(String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) as orderCount, SUM(totalAmount) as totalSpending " +
                     "FROM tbl_order WHERE studentId = ? AND status IN ('已支付', '已完成')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int orderCount = rs.getInt("orderCount");
                double totalSpending = rs.getDouble("totalSpending");
                return new Object[]{orderCount, totalSpending};
            }
            return new Object[]{0, 0.0};
        }
    }

    /**
     * 获取学生最喜欢的商品分类
     * @param studentId 学生ID
     * @return 最喜欢的商品分类
     * @throws SQLException
     */
    public String getStudentFavoriteCategory(String studentId) throws SQLException {
        String sql = "SELECT p.category, COUNT(*) as purchaseCount " +
                     "FROM tbl_order_item oi " +
                     "JOIN tbl_product p ON oi.productId = p.productId " +
                     "JOIN tbl_order o ON oi.orderId = o.orderId " +
                     "WHERE o.studentId = ? AND o.status IN ('已支付', '已完成') " +
                     "GROUP BY p.category " +
                     "ORDER BY purchaseCount DESC " +
                     "LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("category");
            }
            return null;
        }
    }

    /**
     * 检查学生是否有未支付的订单
     * @param studentId 学生ID
     * @return 是否有未支付的订单
     * @throws SQLException
     */
    public boolean hasUnpaidOrders(String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_order WHERE studentId = ? AND status = '待支付'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * 获取学生的平均订单金额
     * @param studentId 学生ID
     * @return 平均订单金额
     * @throws SQLException
     */
    public double getStudentAverageOrderValue(String studentId) throws SQLException {
        String sql = "SELECT AVG(totalAmount) as avgOrderValue " +
                     "FROM tbl_order WHERE studentId = ? AND status IN ('已支付', '已完成')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avgOrderValue");
            }
            return 0.0;
        }
    }
}