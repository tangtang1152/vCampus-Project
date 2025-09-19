package edu.seu.campus.server.controller;

import edu.seu.campus.server.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

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

    @PostMapping("/borrow")
    public ApiResponse<Map<String,Object>> borrow(@RequestParam("userId") int userId,
                                                  @RequestParam("bookId") int bookId,
                                                  @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);

            // 检查图书
            int available = 0; String status = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT availableCopies,status FROM tbl_book WHERE bookId=?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return ApiResponse.error(22001, "图书不存在"); }
                    available = (rs.getObject(1) == null) ? 0 : rs.getInt(1);
                    status = rs.getString(2);
                }
            }
            if ("下架".equals(status)) { conn.rollback(); return ApiResponse.error(22002, "该图书已下架"); }
            if (available <= 0) { conn.rollback(); return ApiResponse.error(22003, "库存不足"); }

            // 借出上限与逾期检查（简化：上限5本/存在逾期禁止）
            try (PreparedStatement c1 = conn.prepareStatement("SELECT COUNT(*) FROM tbl_borrow_record WHERE userId=? AND borrowStatus='借出'")) {
                c1.setInt(1, userId);
                try (ResultSet r1 = c1.executeQuery()) { if (r1.next() && r1.getInt(1) >= 5) { conn.rollback(); return ApiResponse.error(22004, "超过最大借阅数 5"); } }
            }
            try (PreparedStatement c2 = conn.prepareStatement("SELECT 1 FROM tbl_borrow_record WHERE userId=? AND borrowStatus='逾期'")) {
                c2.setInt(1, userId);
                try (ResultSet r2 = c2.executeQuery()) { if (r2.next()) { conn.rollback(); return ApiResponse.error(22005, "存在逾期记录，无法借书"); } }
            }
            try (PreparedStatement c3 = conn.prepareStatement("SELECT 1 FROM tbl_borrow_record WHERE userId=? AND bookId=? AND borrowStatus='借出'")) {
                c3.setInt(1, userId); c3.setInt(2, bookId);
                try (ResultSet r3 = c3.executeQuery()) { if (r3.next()) { conn.rollback(); return ApiResponse.error(22006, "您已借出该书"); } }
            }

            try (PreparedStatement u = conn.prepareStatement("UPDATE tbl_book SET availableCopies=availableCopies-1 WHERE bookId=? AND availableCopies>0")) {
                u.setInt(1, bookId);
                if (u.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(22007, "扣减库存失败"); }
            }

            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO tbl_borrow_record (bookId,userId,borrowDate,dueDate,returnDate,renewTimes,fine,borrowStatus) VALUES (?,?,?,?,?,?,?,?)")) {
                long now = System.currentTimeMillis();
                java.sql.Date borrowDate = new java.sql.Date(now);
                java.sql.Date dueDate = new java.sql.Date(now + (long)Math.max(1, Math.min(days,30)) * 24 * 3600 * 1000);
                ins.setInt(1, bookId);
                ins.setInt(2, userId);
                ins.setDate(3, borrowDate);
                ins.setDate(4, dueDate);
                ins.setDate(5, null);
                ins.setInt(6, 0);
                ins.setDouble(7, 0.0);
                ins.setString(8, "借出");
                if (ins.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(22008, "创建借阅记录失败"); }
            }

            conn.commit();
            return ApiResponse.ok(Collections.singletonMap("message", "借书成功"));
        } catch (Exception e) {
            return ApiResponse.error(22999, "借书失败: " + e.getMessage());
        }
    }

    @PostMapping("/return")
    public ApiResponse<Map<String,Object>> returnBook(@RequestParam("userId") int userId,
                                                      @RequestParam("recordId") int recordId,
                                                      @RequestParam("bookId") int bookId) {
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);

            java.sql.Date returnDate = new java.sql.Date(System.currentTimeMillis());
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tbl_borrow_record SET returnDate=?, borrowStatus='已还' WHERE recordId=? AND borrowStatus='借出'")) {
                ps.setDate(1, returnDate);
                ps.setInt(2, recordId);
                if (ps.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(23001, "更新归还状态失败"); }
            }
            try (PreparedStatement inc = conn.prepareStatement("UPDATE tbl_book SET availableCopies=availableCopies+1 WHERE bookId=?")) {
                inc.setInt(1, bookId);
                if (inc.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(23002, "库存回滚失败"); }
            }
            conn.commit();
            return ApiResponse.ok(Collections.singletonMap("message", "归还成功"));
        } catch (Exception e) {
            return ApiResponse.error(23999, "还书失败: " + e.getMessage());
        }
    }

    @PostMapping("/renew")
    public ApiResponse<Map<String,Object>> renew(@RequestParam("userId") int userId,
                                                 @RequestParam("recordId") int recordId,
                                                 @RequestParam(value = "days", required = false, defaultValue = "30") int days) {
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);

            java.sql.Date newDue;
            int newTimes;
            try (PreparedStatement sel = conn.prepareStatement("SELECT dueDate,renewTimes,borrowStatus FROM tbl_borrow_record WHERE recordId=?")) {
                sel.setInt(1, recordId);
                try (ResultSet rs = sel.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return ApiResponse.error(24001, "借阅记录不存在"); }
                    if (!"借出".equals(rs.getString("borrowStatus"))) { conn.rollback(); return ApiResponse.error(24002, "仅在借出状态可续借"); }
                    java.sql.Date due = rs.getDate("dueDate");
                    int times = rs.getObject("renewTimes") == null ? 0 : rs.getInt("renewTimes");
                    if (times >= 1) { conn.rollback(); return ApiResponse.error(24003, "超过最大续借次数 1"); }
                    long base = due == null ? System.currentTimeMillis() : due.getTime();
                    newDue = new java.sql.Date(base + (long)Math.max(1, Math.min(days,30)) * 24 * 3600 * 1000);
                    newTimes = times + 1;
                }
            }
            try (PreparedStatement upd = conn.prepareStatement("UPDATE tbl_borrow_record SET dueDate=?, renewTimes=? WHERE recordId=?")) {
                upd.setDate(1, newDue);
                upd.setInt(2, newTimes);
                upd.setInt(3, recordId);
                if (upd.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(24004, "续借失败"); }
            }
            conn.commit();
            Map<String,Object> m = new HashMap<>();
            m.put("message", "续借成功");
            m.put("dueDate", newDue.toString());
            m.put("renewTimes", newTimes);
            return ApiResponse.ok(m);
        } catch (Exception e) {
            return ApiResponse.error(24999, "续借失败: " + e.getMessage());
        }
    }

    @PostMapping("/reserve")
    public ApiResponse<Map<String,Object>> reserve(@RequestParam("userId") int userId,
                                                   @RequestParam("bookId") int bookId) {
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);

            // 检查图书
            Integer available = null; String status = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT availableCopies,status FROM tbl_book WHERE bookId=?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return ApiResponse.error(26001, "图书不存在"); }
                    available = (Integer) rs.getObject(1);
                    status = rs.getString(2);
                }
            }
            if ("下架".equals(status)) { conn.rollback(); return ApiResponse.error(26002, "该图书已下架"); }
            if (available != null && available > 0) { conn.rollback(); return ApiResponse.error(26003, "当前有库存，可直接借阅"); }

            // 是否已存在该用户对该书的排队中预约
            try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM tbl_reservation WHERE userId=? AND bookId=? AND resvStatus='排队中'")) {
                chk.setInt(1, userId);
                chk.setInt(2, bookId);
                try (ResultSet r = chk.executeQuery()) { if (r.next()) { conn.rollback(); return ApiResponse.error(26004, "您已排队中"); } }
            }

            // 计算下一个排队号
            int nextOrder = 1;
            try (PreparedStatement mx = conn.prepareStatement("SELECT MAX(queueOrder) FROM tbl_reservation WHERE bookId=? AND resvStatus='排队中'")) {
                mx.setInt(1, bookId);
                try (ResultSet rs = mx.executeQuery()) { if (rs.next()) { int m = rs.getInt(1); if (!rs.wasNull()) nextOrder = m + 1; } }
            }

            // 插入预约
            int reservationId;
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO tbl_reservation (bookId,userId,reservedAt,expiresAt,queueOrder,resvStatus) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                long now = System.currentTimeMillis();
                java.sql.Date reservedAt = new java.sql.Date(now);
                java.sql.Date expiresAt = new java.sql.Date(now + 3L * 24 * 3600 * 1000);
                ins.setInt(1, bookId);
                ins.setInt(2, userId);
                ins.setDate(3, reservedAt);
                ins.setDate(4, expiresAt);
                ins.setInt(5, nextOrder);
                ins.setString(6, "排队中");
                if (ins.executeUpdate() != 1) { conn.rollback(); return ApiResponse.error(26005, "预约失败"); }
                try (ResultSet keys = ins.getGeneratedKeys()) { if (keys.next()) reservationId = keys.getInt(1); else reservationId = -1; }
            }

            conn.commit();
            Map<String,Object> m = new HashMap<>();
            m.put("reservationId", reservationId);
            m.put("queueOrder", nextOrder);
            return ApiResponse.ok(m);
        } catch (Exception e) {
            return ApiResponse.error(26999, "预约失败: " + e.getMessage());
        }
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<Map<String,Object>> cancel(@PathVariable int reservationId,
                                                  @RequestParam(value = "userId", required = false) Integer userId) {
        try (Connection conn = getConn()) {
            // 可选：校验 userId 拥有该预约（为简化暂不强制）
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tbl_reservation SET resvStatus='取消' WHERE reservationId=? AND resvStatus<>'取消'")) {
                ps.setInt(1, reservationId);
                int n = ps.executeUpdate();
                if (n != 1) return ApiResponse.error(27001, "预约不存在或已取消");
            }
            return ApiResponse.ok(java.util.Collections.singletonMap("status", "已取消"));
        } catch (Exception e) {
            return ApiResponse.error(27999, "取消失败: " + e.getMessage());
        }
    }

    @GetMapping("/books/{bookId}/records")
    public ApiResponse<Map<String,Object>> records(@PathVariable int bookId) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> list = new ArrayList<>();
        try (Connection conn = getConn()) {
            // 统计
            int total = 0, month = 0, current = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=?")) {
                ps.setInt(1, bookId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total = rs.getInt(1); }
            }
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate first = now.withDayOfMonth(1);
            java.time.LocalDate next = first.plusMonths(1);
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=? AND borrowDate>=? AND borrowDate<?")) {
                ps.setInt(1, bookId);
                ps.setDate(2, java.sql.Date.valueOf(first));
                ps.setDate(3, java.sql.Date.valueOf(next));
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) month = rs.getInt(1); }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tbl_borrow_record WHERE bookId=? AND borrowStatus='借出'")) {
                ps.setInt(1, bookId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) current = rs.getInt(1); }
            }
            result.put("stats", Map.of("total", total, "month", month, "current", current));

            // 明细
            String sql = "SELECT recordId,userId,borrowDate,dueDate,returnDate,borrowStatus FROM tbl_borrow_record WHERE bookId=? ORDER BY borrowDate DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String,Object> m = new HashMap<>();
                        m.put("recordId", rs.getInt("recordId"));
                        m.put("userId", rs.getInt("userId"));
                        m.put("borrowDate", String.valueOf(rs.getDate("borrowDate")));
                        m.put("dueDate", String.valueOf(rs.getDate("dueDate")));
                        m.put("returnDate", String.valueOf(rs.getDate("returnDate")));
                        m.put("status", rs.getString("borrowStatus"));
                        list.add(m);
                    }
                }
            }
            result.put("records", list);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error(25001, "获取借阅记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/my-borrows")
    public ApiResponse<List<Map<String,Object>>> myBorrows(@RequestParam("userId") int userId,
                                                           @RequestParam(value = "status", required = false, defaultValue = "借出") String status) {
        List<Map<String,Object>> rows = new ArrayList<>();
        String baseSql = "SELECT r.recordId,r.bookId,r.userId,r.borrowDate,r.dueDate,r.returnDate,r.renewTimes,r.fine,r.borrowStatus,b.title AS bookTitle " +
                "FROM tbl_borrow_record r LEFT JOIN tbl_book b ON r.bookId=b.bookId WHERE r.userId=?";
        boolean all = (status == null || status.isBlank() || "全部".equals(status));
        String sql = all ? (baseSql + " ORDER BY r.borrowDate DESC") : (baseSql + " AND r.borrowStatus=? ORDER BY r.borrowDate DESC");
        try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (!all) ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("recordId", rs.getInt("recordId"));
                    m.put("bookId", rs.getInt("bookId"));
                    m.put("userId", rs.getInt("userId"));
                    m.put("borrowDate", String.valueOf(rs.getDate("borrowDate")));
                    m.put("dueDate", String.valueOf(rs.getDate("dueDate")));
                    m.put("returnDate", String.valueOf(rs.getDate("returnDate")));
                    m.put("renewTimes", rs.getObject("renewTimes"));
                    m.put("fine", rs.getObject("fine"));
                    m.put("status", rs.getString("borrowStatus"));
                    m.put("bookTitle", rs.getString("bookTitle"));
                    rows.add(m);
                }
            }
            return ApiResponse.ok(rows);
        } catch (Exception e) {
            return ApiResponse.error(25002, "获取我的借阅失败: " + e.getMessage());
        }
    }
}


