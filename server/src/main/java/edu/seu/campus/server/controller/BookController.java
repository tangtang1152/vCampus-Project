package edu.seu.campus.server.controller;

import edu.seu.campus.server.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

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

    @GetMapping
    public ApiResponse<List<Map<String,Object>>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "status", required = false) String status,
                                                      @RequestParam(value = "sort", required = false) String sort,
                                                      @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                      @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        List<Map<String,Object>> rows = new ArrayList<>();
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        boolean filterStatus = status != null && !status.isBlank() && !"全部".equals(status);
        String orderBy;
        if ("书名↑".equals(sort)) orderBy = "title ASC";
        else if ("书名↓".equals(sort)) orderBy = "title DESC";
        else if ("可借↑".equals(sort)) orderBy = "availableCopies ASC";
        else if ("可借↓".equals(sort)) orderBy = "availableCopies DESC";
        else orderBy = "bookId DESC";

        StringBuilder sql = new StringBuilder("SELECT bookId,isbn,title,author,category,publisher,pubDate,totalCopies,availableCopies,location,status FROM tbl_book WHERE (title LIKE ? OR author LIKE ? OR isbn LIKE ?)");
        if (filterStatus) sql.append(" AND status = ?");
        sql.append(" ORDER BY ").append(orderBy);

        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            ps.setString(idx++, like);
            if (filterStatus) ps.setString(idx++, status);
            try (ResultSet rs = ps.executeQuery()) {
                int offset = Math.max(0, (page - 1) * size);
                int skipped = 0, taken = 0;
                while (rs.next()) {
                    if (skipped < offset) { skipped++; continue; }
                    Map<String,Object> m = new HashMap<>();
                    m.put("bookId", rs.getInt("bookId"));
                    m.put("isbn", rs.getString("isbn"));
                    m.put("title", rs.getString("title"));
                    m.put("author", rs.getString("author"));
                    m.put("category", rs.getString("category"));
                    m.put("publisher", rs.getString("publisher"));
                    m.put("pubDate", rs.getString("pubDate"));
                    m.put("totalCopies", rs.getObject("totalCopies"));
                    m.put("availableCopies", rs.getObject("availableCopies"));
                    m.put("location", rs.getString("location"));
                    try { m.put("status", rs.getString("status")); } catch (SQLException ignore) {}
                    rows.add(m);
                    taken++;
                    if (taken >= size) break;
                }
            }
            return ApiResponse.ok(rows);
        } catch (Exception e) {
            return ApiResponse.error(21001, "图书查询失败: " + e.getMessage());
        }
    }
}


