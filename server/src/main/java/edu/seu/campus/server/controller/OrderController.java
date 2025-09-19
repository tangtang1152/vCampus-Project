package edu.seu.campus.server.controller;

import edu.seu.campus.server.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    static class OrderItemReq {
        public String productId;
        public Integer quantity;
    }

    static class CreateOrderReq {
        public String studentId;
        public List<OrderItemReq> items;
    }

    static class CreateOrderResp {
        public String orderId;
        public double totalAmount;
        public String status;
    }

    private Connection getConn() throws Exception {
        String path = System.getProperty("vcampus.db.path");
        if (path == null || path.isBlank()) {
            String env = System.getenv("VCAMPUS_DB_PATH");
            if (env != null && !env.isBlank()) path = env;
        }
        if (path == null || path.isBlank()) {
            path = System.getProperty("user.dir") + java.io.File.separator +
                    "src" + java.io.File.separator + "main" + java.io.File.separator +
                    "resources" + java.io.File.separator + "database" + java.io.File.separator + "vCampus.accdb";
        }
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        return DriverManager.getConnection("jdbc:ucanaccess://" + path);
    }

    @PostMapping
    public ApiResponse<CreateOrderResp> create(@RequestBody CreateOrderReq req) {
        if (req == null || req.studentId == null || req.studentId.isBlank()) {
            return ApiResponse.error(30001, "studentId 不能为空");
        }
        if (req.items == null || req.items.isEmpty()) {
            return ApiResponse.error(30002, "购物车为空");
        }

        String orderId = "ORD" + System.currentTimeMillis();
        double total = 0.0;

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);

            // 检查外键学生存在
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM tbl_student WHERE studentId=?")) {
                ps.setString(1, req.studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return ApiResponse.error(30003, "学生不存在: " + req.studentId);
                    }
                }
            }

            // 逐项校验库存并累计金额
            for (OrderItemReq it : req.items) {
                if (it == null || it.productId == null || it.productId.isBlank() || it.quantity == null || it.quantity <= 0) {
                    conn.rollback();
                    return ApiResponse.error(30004, "非法订单项");
                }
                try (PreparedStatement p1 = conn.prepareStatement("SELECT price, stock FROM tbl_product WHERE productId=?")) {
                    p1.setString(1, it.productId);
                    try (ResultSet rs = p1.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return ApiResponse.error(30005, "商品不存在: " + it.productId); }
                        double price = rs.getDouble("price");
                        int stock = rs.getInt("stock");
                        if (stock < it.quantity) { conn.rollback(); return ApiResponse.error(30006, "库存不足: " + it.productId); }
                        total += price * it.quantity;
                    }
                }
            }

            // 创建订单
            try (PreparedStatement po = conn.prepareStatement(
                    "INSERT INTO tbl_order (orderId, studentId, orderDate, totalAmount, status) VALUES (?, ?, ?, ?, ?)")) {
                po.setString(1, orderId);
                po.setString(2, req.studentId);
                po.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                po.setDouble(4, total);
                po.setString(5, "待支付");
                if (po.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(30007, "创建订单失败"); }
            }

            // 插入订单项 & 扣减库存
            for (OrderItemReq it : req.items) {
                double price;
                try (PreparedStatement p1 = conn.prepareStatement("SELECT price FROM tbl_product WHERE productId=?")) {
                    p1.setString(1, it.productId);
                    try (ResultSet rs = p1.executeQuery()) { rs.next(); price = rs.getDouble(1); }
                }
                try (PreparedStatement pi = conn.prepareStatement(
                        "INSERT INTO tbl_order_item (orderId, productId, quantity, subtotal) VALUES (?, ?, ?, ?)")) {
                    pi.setString(1, orderId);
                    pi.setString(2, it.productId);
                    pi.setInt(3, it.quantity);
                    pi.setDouble(4, price * it.quantity);
                    if (pi.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(30008, "写入订单项失败"); }
                }
                try (PreparedStatement pu = conn.prepareStatement(
                        "UPDATE tbl_product SET stock = stock - ? WHERE productId = ? AND stock >= ?")) {
                    pu.setInt(1, it.quantity);
                    pu.setString(2, it.productId);
                    pu.setInt(3, it.quantity);
                    if (pu.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(30009, "扣减库存失败"); }
                }
            }

            conn.commit();
            CreateOrderResp resp = new CreateOrderResp();
            resp.orderId = orderId;
            resp.totalAmount = total;
            resp.status = "待支付";
            return ApiResponse.ok(resp);
        } catch (Exception e) {
            return ApiResponse.error(30010, "下单失败: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Map<String,Object>> pay(@PathVariable String orderId) {
        if (orderId == null || orderId.isBlank()) return ApiResponse.error(30101, "orderId 不能为空");
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_order SET status='已支付' WHERE orderId=? AND status='待支付'")) {
            ps.setString(1, orderId);
            int n = ps.executeUpdate();
            if (n != 1) return ApiResponse.error(30102, "订单不存在或状态不允许支付");
            Map<String,Object> m = new HashMap<>();
            m.put("orderId", orderId);
            m.put("status", "已支付");
            return ApiResponse.ok(m);
        } catch (Exception e) {
            return ApiResponse.error(30103, "支付失败: " + e.getMessage());
        }
    }
}


