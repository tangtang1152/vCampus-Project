package com.vCampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vCampus.entity.OrderItem;

public interface IOrderItemDao {

	boolean addOrderItem(OrderItem orderItem) throws SQLException;
	boolean deleteOrderItemId(Integer itemId) throws SQLException;
	boolean deleteOrderItemsByOrderId(String orderId) throws SQLException;
	OrderItem getOrderItemByItemId(Integer itemId) throws SQLException;
	List<OrderItem> getOrderItemsByOrderId(String orderId) throws SQLException;
	List<OrderItem> getAllOrderItems() throws SQLException;
	
	// 事务化重载
	boolean addOrderItem(OrderItem orderItem, java.sql.Connection conn) throws SQLException;
	boolean deleteOrderItemsByOrderId(String orderId, java.sql.Connection conn) throws SQLException;
	List<OrderItem> getOrderItemsByOrderId(String orderId, java.sql.Connection conn) throws SQLException;
	
}
