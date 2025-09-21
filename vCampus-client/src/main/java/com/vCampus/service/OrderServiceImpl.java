package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Order;
import com.vCampus.entity.OrderItem;
import com.vCampus.net.HttpClientUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类（客户端CS）：HTTP调用服务端
 */
public class OrderServiceImpl implements IOrderService {

    @Override
    public boolean createOrder(Order order) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/orders", order);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteOrder(String orderId) {
        try {
            JsonNode data = HttpClientUtil.sendDelete("/orders/" + orderId);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status) {
        try {
            var body = new java.util.HashMap<String, String>();
            body.put("status", status);
            JsonNode data = HttpClientUtil.sendPost("/orders/" + orderId + "/status", body);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public Order getOrderById(String orderId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/" + orderId);
            return HttpClientUtil.parseObject(data, Order.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Order> getOrdersByStudentId(String studentId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/by-student/" + studentId);
            return HttpClientUtil.parseList(data, Order.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Order> getAllOrders() {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders");
            return HttpClientUtil.parseList(data, Order.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/by-status/" + status);
            return HttpClientUtil.parseList(data, Order.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean addOrderItem(OrderItem orderItem) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/orders/" + orderItem.getOrderId() + "/items", orderItem);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/" + orderId + "/items");
            return HttpClientUtil.parseList(data, OrderItem.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean deleteOrderItemId(Integer itemId) {
        try {
            JsonNode data = HttpClientUtil.sendDelete("/orders/items/" + itemId);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean isOrderExists(String orderId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/exists/" + orderId);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public int getTotalQuantityByOrder(String orderId) {
        try {
            List<OrderItem> items = getOrderItemsByOrderId(orderId);
            if (items == null) return 0;
            int sum = 0; for (OrderItem it : items) sum += it.getQuantity();
            return sum;
        } catch (Exception e) { return 0; }
    }

    @Override
    public double getTotalAmountByOrder(String orderId) {
        try {
            List<OrderItem> items = getOrderItemsByOrderId(orderId);
            if (items == null) return 0.0;
            double sum = 0.0; for (OrderItem it : items) sum += it.getSubtotal();
            return sum;
        } catch (Exception e) { return 0.0; }
    }

    @Override
    public int getOrderCountByStudent(String studentId) {
        try {
            List<Order> list = getOrdersByStudentId(studentId);
            return list == null ? 0 : list.size();
        } catch (Exception e) { return 0; }
    }

    @Override
    public List<OrderItem> getOrderItemsByProductId(String productId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/orders/items/by-product/" + productId);
            return HttpClientUtil.parseList(data, OrderItem.class);
        } catch (Exception e) { return new ArrayList<>(); }
    }
}
