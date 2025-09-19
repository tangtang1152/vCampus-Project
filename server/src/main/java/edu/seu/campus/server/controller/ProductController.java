package edu.seu.campus.server.controller;

import edu.seu.campus.server.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private Connection getConn() throws Exception {
        // 简化版本：直接用系统已有 DBUtil 路径逻辑
        // 这里使用 UCanAccess 直连，期望 server.application.properties 配置了 vcampus.db.path
        String path = System.getProperty("vcampus.db.path");
        if (path == null || path.isBlank()) {
            path = System.getProperty("user.dir") + java.io.File.separator +
                    "src" + java.io.File.separator + "main" + java.io.File.separator +
                    "resources" + java.io.File.separator + "database" + java.io.File.separator + "vCampus.accdb";
        }
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        return DriverManager.getConnection("jdbc:ucanaccess://" + path);
    }

    @GetMapping
    public ApiResponse<List<Map<String,Object>>> list(@RequestParam(value = "category", required = false) String category,
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        List<Map<String,Object>> rows = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT productId, productName, price, stock, category, description FROM tbl_product WHERE 1=1");
        if (category != null && !category.isBlank()) sql.append(" AND category = ?");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND productName LIKE ?");
        sql.append(" ORDER BY productId");

        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (category != null && !category.isBlank()) ps.setString(idx++, category);
            if (keyword != null && !keyword.isBlank()) ps.setString(idx++, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("productId", rs.getString("productId"));
                    m.put("productName", rs.getString("productName"));
                    m.put("price", rs.getDouble("price"));
                    m.put("stock", rs.getInt("stock"));
                    m.put("category", rs.getString("category"));
                    m.put("description", rs.getString("description"));
                    rows.add(m);
                }
            }
            return ApiResponse.ok(rows);
        } catch (Exception e) {
            return ApiResponse.error(20001, "查询失败: " + e.getMessage());
        }
    }
}


