package com.vcampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vcampus.entity.OrderItem;

public interface IOrderItemDao {

	boolean addOrderItem(OrderItem orderItem) throws SQLException;
	boolean deleteOrderItem(Integer id) throws SQLException;
	boolean deleteOrderItemsByOrderId(String orderId) throws SQLException;
	OrderItem getOrderItemById(Integer id) throws SQLException;
	List<OrderItem> getOrderItemsByOrderId(String orderId) throws SQLException;
	List<OrderItem> getAllOrderItems() throws SQLException;
	
}
