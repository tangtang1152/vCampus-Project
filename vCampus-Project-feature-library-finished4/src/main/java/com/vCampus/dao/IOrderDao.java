package com.vCampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vCampus.entity.Order;

public interface IOrderDao {

	boolean createOrder(Order order) throws SQLException;
	boolean deleteOrder(String orderId) throws SQLException;
	boolean updateOrderStatus(String orderId, String status) throws SQLException;
	Order getOrderById(String orderId) throws SQLException;
	List<Order> getOrdersByStudentId(String studentId) throws SQLException;

	List<Order> getAllOrders() throws SQLException;
	List<Order> getOrdersByStatus(String status) throws SQLException;
}
