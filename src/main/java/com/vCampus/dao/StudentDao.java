package com.vCampus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.vCampus.entity.Student;
import com.vCampus.util.DBConstants;
import com.vCampus.util.DBUtil;
import java.util.Date;
import com.vCampus.service.ValidationService;

public class StudentDao implements IStudentDao {
    
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
        }catch (SQLException e) {
            System.err.println("插入学生记录失败: " + e.getMessage());
            throw e; // 关键：重新抛出异常，让服务层处理
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
    public Student findById(String studentId, Connection conn) throws SQLException {
        return findByStudentId(studentId, conn);
    }
    
    @Override
    public Student findByStudentId(String studentId, Connection conn) throws SQLException {
        System.out.println("执行findByStudentId查询，学号: " + studentId);
        
        String sql = "SELECT s.*, u.username, u.password, u.role FROM tbl_student s JOIN tbl_user u ON s.userId = u.userId WHERE s.studentId = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            System.out.println("执行SQL查询: " + pstmt.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student student = createStudentFromResultSet(rs);
                    System.out.println("找到学生记录: " + student);
                    return student;
                } else {
                    System.out.println("未找到学号为 " + studentId + " 的学生记录");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("查询学生信息时发生SQL异常: " + e.getMessage());
            throw e;
        }
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
    public List<Student> findStudentsByClass(String className) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM tbl_student WHERE className = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    public boolean updateStudentStatus(String studentId, String newStatus) throws SQLException {
        String sql = "UPDATE tbl_student SET status = ? WHERE studentId = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setString(2, studentId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("更新学生状态时发生SQL异常: " + e.getMessage());
            throw e; // 重新抛出异常，让调用者处理
        }
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
                student.setClassName(rs.getString("className"));
                
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
    public boolean updateStudentStatus2(String studentId, String status) throws SQLException {
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