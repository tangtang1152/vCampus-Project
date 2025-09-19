package com.vCampus.dao;

import com.vCampus.entity.OrderItem;
import com.vCampus.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单明细数据访问对象实现类
 */
public class OrderItemDaoImpl implements IOrderItemDao {

    @Override
    public boolean addOrderItem(OrderItem orderItem) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO tbl_order_item (orderId, productId, quantity, subtotal) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderItem.getOrderId());
            ps.setString(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getQuantity());
            ps.setDouble(4, orderItem.getSubtotal());
            
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
    public boolean deleteOrderItemId(Integer itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM tbl_order_item WHERE itemId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            
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
    public boolean deleteOrderItemsByOrderId(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM tbl_order_item WHERE orderId = ?";
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
    public OrderItem getOrderItemByItemId(Integer itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order_item WHERE itemId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToOrderItem(rs);
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
    public List<OrderItem> getOrderItemsByOrderId(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order_item WHERE orderId = ? ORDER BY itemId";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }
            return orderItems;
            
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
    public List<OrderItem> getAllOrderItems() throws SQLException {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM tbl_order_item ORDER BY orderId, itemId");
            
            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }
            return orderItems;
            
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

    /**
     * 将ResultSet映射到OrderItem对象的辅助方法
     * @param rs ResultSet对象
     * @return OrderItem对象
     * @throws SQLException
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(rs.getInt("itemId"));
        orderItem.setOrderId(rs.getString("orderId"));
        orderItem.setProductId(rs.getString("productId"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setSubtotal(rs.getDouble("subtotal"));
        return orderItem;
    }

    /**
     * 检查订单项是否存在
     * @param id 订单项ID
     * @return 是否存在
     * @throws SQLException
     */
    public boolean orderItemExists(Integer itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM tbl_order_item WHERE itemId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
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
     * 获取订单的商品总数量
     * @param orderId 订单ID
     * @return 商品总数量
     * @throws SQLException
     */
    public int getTotalQuantityByOrder(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT SUM(quantity) FROM tbl_order_item WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
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

    /**
     * 获取订单的总金额（从订单项计算）
     * @param orderId 订单ID
     * @return 总金额
     * @throws SQLException
     */
    public double getTotalAmountByOrder(String orderId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT SUM(subtotal) FROM tbl_order_item WHERE orderId = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
            
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
     * 更新订单项数量和小计
     * @param id 订单项ID
     * @param quantity 新数量
     * @param subtotal 新小计
     * @return 是否更新成功
     * @throws SQLException
     */
    public boolean updateOrderItem(Integer itemId, int quantity, double subtotal) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE tbl_order_item SET quantity = ?, subtotal = ? WHERE itemId = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setDouble(2, subtotal);
            ps.setInt(3, itemId);
            
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

    /**
     * 根据商品ID获取订单项
     * @param productId 商品ID
     * @return 订单项列表
     * @throws SQLException
     */
    public List<OrderItem> getOrderItemsByProductId(String productId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<OrderItem> orderItems = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM tbl_order_item WHERE productId = ? ORDER BY itemId";
            ps = conn.prepareStatement(sql);
            ps.setString(1, productId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }
            return orderItems;
            
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
