package com.vCampus.service;

import com.vCampus.entity.Order;
import com.vCampus.entity.OrderItem;
import java.util.List;

/**
 * 订单服务接口
 * 定义订单相关的业务逻辑操作
 */
public interface IOrderService {
    
    /**
     * 创建订单
     * @param order 订单对象
     * @return 是否创建成功
     */
    boolean createOrder(Order order);
    
    /**
     * 删除订单
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    boolean deleteOrder(String orderId);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateOrderStatus(String orderId, String status);
    
    /**
     * 根据ID获取订单
     * @param orderId 订单ID
     * @return 订单对象
     */
    Order getOrderById(String orderId);
    
    /**
     * 根据学生ID获取订单
     * @param studentId 学生ID
     * @return 订单列表
     */
    List<Order> getOrdersByStudentId(String studentId);
    
    /**
     * 获取所有订单
     * @return 订单列表
     */
    List<Order> getAllOrders();
    
    /**
     * 根据状态获取订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByStatus(String status);
    
    /**
     * 添加订单项
     * @param orderItem 订单项对象
     * @return 是否添加成功
     */
    boolean addOrderItem(OrderItem orderItem);
    
    /**
     * 根据订单ID获取订单项
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> getOrderItemsByOrderId(String orderId);
    
    /**
     * 删除订单项
     * @param itemId 订单项ID
     * @return 是否删除成功
     */
    boolean deleteOrderItemId(Integer itemId);
    
    /**
     * 检查订单是否存在
     * @param orderId 订单ID
     * @return 是否存在
     */
    boolean isOrderExists(String orderId);
    
    /**
     * 获取订单的商品总数量
     * @param orderId 订单ID
     * @return 商品总数量
     */
    int getTotalQuantityByOrder(String orderId);
    
    /**
     * 获取订单的总金额（从订单项计算）
     * @param orderId 订单ID
     * @return 总金额
     */
    double getTotalAmountByOrder(String orderId);
    
    /**
     * 获取学生的订单数量
     * @param studentId 学生ID
     * @return 订单数量
     */
    int getOrderCountByStudent(String studentId);
    
    /**
     * 根据商品ID获取订单项
     * @param productId 商品ID
     * @return 订单项列表
     */
    List<OrderItem> getOrderItemsByProductId(String productId);
}