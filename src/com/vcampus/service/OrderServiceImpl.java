package com.vcampus.service;

import com.vcampus.dao.IOrderDao;
import com.vcampus.dao.IOrderItemDao;
import com.vcampus.dao.OrderDaoImpl;
import com.vcampus.dao.OrderItemDaoImpl;
import com.vcampus.entity.Order;
import com.vcampus.entity.OrderItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 * 处理订单相关的业务逻辑
 */
public class OrderServiceImpl implements IOrderService {
    
    private IOrderDao orderDao;
    private IOrderItemDao orderItemDao;
    
    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
        this.orderItemDao = new OrderItemDaoImpl();
    }
    
    /**
     * 构造方法，允许注入不同的DAO实现
     * @param orderDao 订单DAO实现
     * @param orderItemDao 订单项DAO实现
     */
    public OrderServiceImpl(IOrderDao orderDao, IOrderItemDao orderItemDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
    }
    
    @Override
    public boolean createOrder(Order order) {
        try {
            if (order == null) {
                System.err.println("创建订单失败：订单对象不能为空");
                return false;
            }
            
            if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
                System.err.println("创建订单失败：订单ID不能为空");
                return false;
            }
            
            if (order.getStudentId() == null || order.getStudentId().trim().isEmpty()) {
                System.err.println("创建订单失败：学生ID不能为空");
                return false;
            }
            
            if (order.getTotalAmount() < 0) {
                System.err.println("创建订单失败：订单金额不能为负数");
                return false;
            }
            
            if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
                System.err.println("创建订单失败：订单状态不能为空");
                return false;
            }
            
            // 检查订单是否已存在
            if (orderDao.getOrderById(order.getOrderId()) != null) {
                System.err.println("创建订单失败：订单ID '" + order.getOrderId() + "' 已存在");
                return false;
            }
            
            return orderDao.createOrder(order);
            
        } catch (SQLException e) {
            System.err.println("创建订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteOrder(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("删除订单失败：订单ID不能为空");
                return false;
            }
            
            // 检查订单是否存在
            Order existingOrder = orderDao.getOrderById(orderId);
            if (existingOrder == null) {
                System.err.println("删除订单失败：订单ID '" + orderId + "' 不存在");
                return false;
            }
            
            // 先删除订单项
            orderItemDao.deleteOrderItemsByOrderId(orderId);
            
            // 再删除订单
            return orderDao.deleteOrder(orderId);
            
        } catch (SQLException e) {
            System.err.println("删除订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateOrderStatus(String orderId, String status) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("更新订单状态失败：订单ID不能为空");
                return false;
            }
            
            if (status == null || status.trim().isEmpty()) {
                System.err.println("更新订单状态失败：状态不能为空");
                return false;
            }
            
            // 验证状态合法性
            if (!isValidStatus(status)) {
                System.err.println("更新订单状态失败：状态 '" + status + "' 不合法");
                return false;
            }
            
            // 检查订单是否存在
            Order existingOrder = orderDao.getOrderById(orderId);
            if (existingOrder == null) {
                System.err.println("更新订单状态失败：订单ID '" + orderId + "' 不存在");
                return false;
            }
            
            return orderDao.updateOrderStatus(orderId, status);
            
        } catch (SQLException e) {
            System.err.println("更新订单状态过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    @Override
    public Order getOrderById(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("获取订单失败：订单ID不能为空");
                return null;
            }
            
            return orderDao.getOrderById(orderId);
            
        } catch (SQLException e) {
            System.err.println("获取订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Order> getOrdersByStudentId(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                System.err.println("获取订单失败：学生ID不能为空");
                return null;
            }
            
            return orderDao.getOrdersByStudentId(studentId);
            
        } catch (SQLException e) {
            System.err.println("获取学生订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Order> getAllOrders() {
        try {
            return orderDao.getAllOrders();
            
        } catch (SQLException e) {
            System.err.println("获取所有订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Order> getOrdersByStatus(String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                System.err.println("获取订单失败：状态不能为空");
                return null;
            }
            
            if (!isValidStatus(status)) {
                System.err.println("获取订单失败：状态 '" + status + "' 不合法");
                return null;
            }
            
            return orderDao.getOrdersByStatus(status);
            
        } catch (SQLException e) {
            System.err.println("根据状态获取订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean addOrderItem(OrderItem orderItem) {
        try {
            if (orderItem == null) {
                System.err.println("添加订单项失败：订单项对象不能为空");
                return false;
            }
            
            if (orderItem.getOrderId() == null || orderItem.getOrderId().trim().isEmpty()) {
                System.err.println("添加订单项失败：订单ID不能为空");
                return false;
            }
            
            if (orderItem.getProductId() == null || orderItem.getProductId().trim().isEmpty()) {
                System.err.println("添加订单项失败：商品ID不能为空");
                return false;
            }
            
            if (orderItem.getQuantity() <= 0) {
                System.err.println("添加订单项失败：数量必须大于0");
                return false;
            }
            
            if (orderItem.getSubtotal() < 0) {
                System.err.println("添加订单项失败：小计金额不能为负数");
                return false;
            }
            
            // 检查订单是否存在
            Order existingOrder = orderDao.getOrderById(orderItem.getOrderId());
            if (existingOrder == null) {
                System.err.println("添加订单项失败：订单ID '" + orderItem.getOrderId() + "' 不存在");
                return false;
            }
            
            return orderItemDao.addOrderItem(orderItem);
            
        } catch (SQLException e) {
            System.err.println("添加订单项过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("获取订单项失败：订单ID不能为空");
                return null;
            }
            
            // 检查订单是否存在
            Order existingOrder = orderDao.getOrderById(orderId);
            if (existingOrder == null) {
                System.err.println("获取订单项失败：订单ID '" + orderId + "' 不存在");
                return null;
            }
            
            return orderItemDao.getOrderItemsByOrderId(orderId);
            
        } catch (SQLException e) {
            System.err.println("获取订单项过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean deleteOrderItemId(Integer itemId) {
        try {
            if (itemId == null || itemId <= 0) {
                System.err.println("删除订单项失败：ID不合法");
                return false;
            }
            
            // 检查订单项是否存在
            OrderItem existingItem = orderItemDao.getOrderItemByItemId(itemId);
            if (existingItem == null) {
                System.err.println("删除订单项失败：订单项ID " + itemId + " 不存在");
                return false;
            }
            
            return orderItemDao.deleteOrderItemId(itemId);
            
        } catch (SQLException e) {
            System.err.println("删除订单项过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean isOrderExists(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return false;
            }
            
            return orderDao.getOrderById(orderId) != null;
            
        } catch (SQLException e) {
            System.err.println("检查订单是否存在过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getTotalQuantityByOrder(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("获取商品总数失败：订单ID不能为空");
                return 0;
            }
            
            // 检查订单是否存在
            if (!isOrderExists(orderId)) {
                System.err.println("获取商品总数失败：订单ID '" + orderId + "' 不存在");
                return 0;
            }
            
            List<OrderItem> items = getOrderItemsByOrderId(orderId);
            if (items == null) {
                return 0;
            }
            
            int totalQuantity = 0;
            for (OrderItem item : items) {
                totalQuantity += item.getQuantity();
            }
            return totalQuantity;
            
        } catch (Exception e) {
            System.err.println("获取商品总数过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public double getTotalAmountByOrder(String orderId) {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                System.err.println("获取总金额失败：订单ID不能为空");
                return 0.0;
            }
            
            // 检查订单是否存在
            if (!isOrderExists(orderId)) {
                System.err.println("获取总金额失败：订单ID '" + orderId + "' 不存在");
                return 0.0;
            }
            
            List<OrderItem> items = getOrderItemsByOrderId(orderId);
            if (items == null) {
                return 0.0;
            }
            
            double totalAmount = 0.0;
            for (OrderItem item : items) {
                totalAmount += item.getSubtotal();
            }
            return totalAmount;
            
        } catch (Exception e) {
            System.err.println("获取总金额过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    @Override
    public int getOrderCountByStudent(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                System.err.println("获取订单数量失败：学生ID不能为空");
                return 0;
            }
            
            List<Order> orders = getOrdersByStudentId(studentId);
            return orders != null ? orders.size() : 0;
            
        } catch (Exception e) {
            System.err.println("获取订单数量过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public List<OrderItem> getOrderItemsByProductId(String productId) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("获取订单项失败：商品ID不能为空");
                return null;
            }
            
            // 获取所有订单项，然后过滤
            List<OrderItem> allItems = orderItemDao.getAllOrderItems();
            if (allItems == null) {
                return null;
            }
            
            List<OrderItem> filteredItems = new ArrayList<>();
            for (OrderItem item : allItems) {
                if (productId.equals(item.getProductId())) {
                    filteredItems.add(item);
                }
            }
            return filteredItems;
            
        } catch (SQLException e) {
            System.err.println("获取订单项过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 验证订单状态是否合法
     * @param status 订单状态
     * @return 是否合法
     */
    private boolean isValidStatus(String status) {
        return "待支付".equals(status) || "已支付".equals(status) || 
               "已发货".equals(status) || "已完成".equals(status) || 
               "已取消".equals(status);
    }
    
    /**
     * 获取订单详情（包含订单项）
     * @param orderId 订单ID
     * @return 订单对象（包含订单项）
     */
    public Order getOrderDetails(String orderId) {
        Order order = getOrderById(orderId);
        if (order != null) {
            List<OrderItem> items = getOrderItemsByOrderId(orderId);
            // 这里可以设置订单项到订单对象中
            // order.setItems(items);
        }
        return order;
    }
    
    /**
     * 批量添加订单项
     * @param orderItems 订单项列表
     * @return 是否全部添加成功
     */
    public boolean batchAddOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            System.err.println("批量添加订单项失败：订单项列表不能为空");
            return false;
        }
        
        boolean allSuccess = true;
        for (OrderItem item : orderItems) {
            if (!addOrderItem(item)) {
                allSuccess = false;
                System.err.println("批量添加订单项失败：商品ID " + item.getProductId());
            }
        }
        
        return allSuccess;
    }
}