package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Order;
import com.vCampus.entity.OrderItem;
import com.vCampus.service.IOrderService;
import com.vCampus.service.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired private IOrderService orderService;
    @Autowired private IShopService shopService;

    // 下单（带购物车项）：返回包含 orderId 的对象
    @PostMapping
    public ApiResponse<Map<String, String>> createWithItems(@RequestBody Map<String, Object> body) {
        String studentId = String.valueOf(body.get("studentId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();
        if (items != null) {
            for (Map<String, Object> m : items) {
                OrderItem it = new OrderItem();
                it.setProductId(String.valueOf(m.get("productId")));
                it.setQuantity(Integer.parseInt(String.valueOf(m.get("quantity"))));
                orderItems.add(it);
            }
        }
        String orderId = shopService.purchase(studentId, orderItems);
        if (orderId == null) return ApiResponse.error("下单失败");
        return ApiResponse.ok(java.util.Map.of("orderId", orderId));
    }

    // 仅创建订单（不含项），布尔返回
    @PostMapping("/create")
    public ApiResponse<Boolean> create(@RequestBody Order order) {
        return ApiResponse.ok(orderService.createOrder(order));
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<Boolean> delete(@PathVariable String orderId) { return ApiResponse.ok(orderService.deleteOrder(orderId)); }

    @PostMapping("/{orderId}/status")
    public ApiResponse<Boolean> updateStatus(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(orderService.updateOrderStatus(orderId, body.get("status")));
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Boolean> pay(@PathVariable String orderId) { return ApiResponse.ok(shopService.payOrder(orderId)); }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Boolean> cancel(@PathVariable String orderId) { return ApiResponse.ok(shopService.cancelOrder(orderId)); }

    @GetMapping("/{orderId}")
    public ApiResponse<Order> get(@PathVariable String orderId) { return ApiResponse.ok(orderService.getOrderById(orderId)); }

    @GetMapping
    public ApiResponse<List<Order>> listAll() { return ApiResponse.ok(orderService.getAllOrders()); }

    @GetMapping("/by-student/{studentId}")
    public ApiResponse<List<Order>> byStudent(@PathVariable String studentId) { return ApiResponse.ok(orderService.getOrdersByStudentId(studentId)); }

    @GetMapping("/by-status/{status}")
    public ApiResponse<List<Order>> byStatus(@PathVariable String status) { return ApiResponse.ok(orderService.getOrdersByStatus(status)); }

    @PostMapping("/{orderId}/items")
    public ApiResponse<Boolean> addItem(@PathVariable String orderId, @RequestBody OrderItem item) {
        item.setOrderId(orderId);
        return ApiResponse.ok(orderService.addOrderItem(item));
    }

    @GetMapping("/{orderId}/items")
    public ApiResponse<List<OrderItem>> items(@PathVariable String orderId) { return ApiResponse.ok(orderService.getOrderItemsByOrderId(orderId)); }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Boolean> deleteItem(@PathVariable Integer itemId) { return ApiResponse.ok(orderService.deleteOrderItemId(itemId)); }

    @GetMapping("/exists/{orderId}")
    public ApiResponse<Boolean> exists(@PathVariable String orderId) { return ApiResponse.ok(orderService.isOrderExists(orderId)); }

    @GetMapping("/{orderId}/total-qty")
    public ApiResponse<Integer> totalQty(@PathVariable String orderId) { return ApiResponse.ok(orderService.getTotalQuantityByOrder(orderId)); }

    @GetMapping("/{orderId}/total-amount")
    public ApiResponse<Double> totalAmount(@PathVariable String orderId) { return ApiResponse.ok(orderService.getTotalAmountByOrder(orderId)); }

    @GetMapping("/count/by-student/{studentId}")
    public ApiResponse<Integer> countByStudent(@PathVariable String studentId) { return ApiResponse.ok(orderService.getOrderCountByStudent(studentId)); }

    @GetMapping("/items/by-product/{productId}")
    public ApiResponse<List<OrderItem>> itemsByProduct(@PathVariable String productId) { return ApiResponse.ok(orderService.getOrderItemsByProductId(productId)); }
}



