package com.vcampus.dao;

import com.vcampus.entity.Order;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问对象实现类
 */
public class OrderDaoImpl implements IOrderDao {

    @Override
    public boolean createOrder(Order order) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO tbl_order (orderId, studentId, orderDate, totalAmount, status) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getStudentId());
            
            // 处理日期类型
            if (order.getOrderDate() != null) {
                ps.setTimestamp(3, new Timestamp(order.getOrderDate().getTime()));
            } else {
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }
            
            ps.setDouble(4, order.getTotalAmount());
            ps.setString(5, order.getStatus());
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            // 确保资源被关闭
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public boolean deleteOrder(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM tbl_order WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE tbl_order SET status = ? WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, orderId);
            
            int result = ps.executeUpdate();
            return result > 0;
            
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public Order getOrderById(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }
            return null;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public List<Order> getOrdersByStudentId(String studentId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order WHERE studentId = ? ORDER BY orderDate DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, studentId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM tbl_order ORDER BY orderDate DESC");
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (st != null) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    @Override
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order WHERE status = ? ORDER BY orderDate DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 将ResultSet映射到Order对象的辅助方法
     * @param rs ResultSet对象
     * @return Order对象
     * @throws SQLException
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getString("orderId"));
        order.setStudentId(rs.getString("studentId"));
        
        // 处理日期字段
        Timestamp timestamp = rs.getTimestamp("orderDate");
        if (timestamp != null) {
            order.setOrderDate(new Date(timestamp.getTime()));
        }
        
        order.setTotalAmount(rs.getDouble("totalAmount"));
        order.setStatus(rs.getString("status"));
        return order;
    }

    /**
     * 检查订单是否存在
     * @param orderId 订单ID
     * @return 是否存在
     * @throws SQLException
     */
    public boolean orderExists(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tbl_order WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 获取学生的订单数量
     * @param studentId 学生ID
     * @return 订单数量
     * @throws SQLException
     */
    public int getOrderCountByStudent(String studentId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tbl_order WHERE studentId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, studentId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }
}