package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Order;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Product;
import com.vCampus.net.HttpClientUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商店服务实现类（客户端CS）：HTTP调用服务端
 */
public class ShopServiceImpl implements IShopService {

    private final IProductService productService = new ProductServiceImpl();
    private final IOrderService orderService = new OrderServiceImpl();

    @Override
    public List<Product> getAllProducts() { return productService.getAllProducts(); }

    @Override
    public List<Product> getProductsByCategory(String category) { return productService.getProductsByCategory(category); }

    @Override
    public Product getProductById(String productId) { return productService.getProductById(productId); }

    @Override
    public String purchase(String studentId, List<OrderItem> items) {
        if (studentId == null || studentId.trim().isEmpty()) return null;
        if (items == null || items.isEmpty()) return null;
        if (!validateCartItems(items)) return null;
        for (OrderItem it : items) if (!productService.checkStock(it.getProductId(), it.getQuantity())) return null;
        double total = calculateCartTotal(items);
        String orderId = "ORD" + System.currentTimeMillis();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStudentId(studentId);
        order.setOrderDate(new Date());
        order.setTotalAmount(total);
        order.setStatus("待支付");
        boolean ok = orderService.createOrder(order);
        if (!ok) return null;
        try {
            for (OrderItem it : items) {
                it.setOrderId(orderId);
                Product p = productService.getProductById(it.getProductId());
                if (p != null) it.setSubtotal(p.getPrice() * it.getQuantity());
                if (!orderService.addOrderItem(it)) return null;
                // 库存更新交给服务端订单逻辑处理；此处不再直接扣减
            }
            return orderId;
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean payOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) return false;
        try {
            JsonNode data = HttpClientUtil.sendPost("/orders/" + orderId + "/pay", java.util.Map.of());
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean cancelOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) return false;
        try {
            JsonNode data = HttpClientUtil.sendPost("/orders/" + orderId + "/cancel", java.util.Map.of());
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public List<Order> getOrderHistory(String studentId) { return orderService.getOrdersByStudentId(studentId); }

    @Override
    public Order getOrderDetails(String orderId) { return orderService.getOrderById(orderId); }

    @Override
    public boolean checkStock(String productId, int requiredQuantity) { return productService.checkStock(productId, requiredQuantity); }

    @Override
    public List<Product> searchProducts(String productName) { return productService.searchProductsByName(productName); }

    @Override
    public List<String> getAllCategories() { return productService.getAllCategories(); }

    @Override
    public double calculateCartTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return 0.0;
        double total = 0.0;
        for (OrderItem it : items) {
            Product p = productService.getProductById(it.getProductId());
            if (p != null) total += p.getPrice() * it.getQuantity();
        }
        return total;
    }

    @Override
    public boolean validateCartItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return false;
        for (OrderItem it : items) {
            if (it.getProductId() == null || it.getProductId().trim().isEmpty()) return false;
            if (it.getQuantity() <= 0) return false;
            Product p = productService.getProductById(it.getProductId());
            if (p == null) return false;
        }
        return true;
    }

    @Override
    public List<Product> getRecommendedProducts(int limit) {
        List<Product> all = productService.getAllProducts();
        if (all == null) return null;
        List<Product> out = new ArrayList<>();
        for (Product p : all) { if (p.getStock() > 10) { out.add(p); if (out.size() >= limit) break; } }
        return out;
    }

    @Override
    public List<Product> getHotProducts(int limit) {
        List<Product> all = productService.getAllProducts();
        if (all == null) return null;
        all.sort((a,b) -> Double.compare(a.getPrice(), b.getPrice()));
        return all.subList(0, Math.min(limit, all.size()));
    }
}
