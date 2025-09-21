package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.entity.Reservation;
import com.vCampus.entity.User;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;
import com.vCampus.util.LibraryUserRules;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryService {

    public List<com.vCampus.entity.Book> searchBooks(String keyword, int page, int size) {
        return TransactionManager.executeInTransaction(conn -> {
            String q = String.format("/library/books?keyword=%s&page=%d&size=%d",
                    url(keyword), page, size);
            JsonNode data = HttpClientUtil.sendGet(q);
            return HttpClientUtil.parseList(data, com.vCampus.entity.Book.class);
        });
    }

    public List<com.vCampus.entity.Book> searchBooksAdvanced(String keyword, String status, String sort, int page, int size) {
        return TransactionManager.executeInTransaction(conn -> {
            String q = String.format("/library/books/advanced?keyword=%s&status=%s&sort=%s&page=%d&size=%d",
                    url(keyword), url(status), url(sort), page, size);
            JsonNode data = HttpClientUtil.sendGet(q);
            return HttpClientUtil.parseList(data, com.vCampus.entity.Book.class);
        });
    }

    public ServiceResult borrowBookWithReason(String userId, Integer bookId) {
        return borrowBookWithReason(userId, bookId, 30);
    }

    public ServiceResult borrowBookWithReason(String userId, Integer bookId, int days) {
        return TransactionManager.executeInTransaction(conn -> {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("bookId", bookId);
            body.put("days", days);
            JsonNode data = HttpClientUtil.sendPost("/library/borrow", body);
            return HttpClientUtil.parseObject(data, ServiceResult.class);
        });
    }

    public ServiceResult returnBookWithReason(String userId, Integer recordId, Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("recordId", recordId);
            body.put("bookId", bookId);
            JsonNode data = HttpClientUtil.sendPost("/library/return", body);
            return HttpClientUtil.parseObject(data, ServiceResult.class);
        });
    }

    public ServiceResult renewBorrowWithReason(String userId, Integer recordId, int maxTimes) {
        return renewBorrowWithReason(userId, recordId, 30, maxTimes);
    }

    public ServiceResult renewBorrowWithReason(String userId, Integer recordId, int days, int maxTimes) {
        return TransactionManager.executeInTransaction(conn -> {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("recordId", recordId);
            body.put("days", days);
            body.put("maxTimes", maxTimes);
            JsonNode data = HttpClientUtil.sendPost("/library/renew", body);
            return HttpClientUtil.parseObject(data, ServiceResult.class);
        });
    }

    public ServiceResult reserveBookWithReason(String userId, Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("bookId", bookId);
            JsonNode data = HttpClientUtil.sendPost("/library/reserve", body);
            return HttpClientUtil.parseObject(data, ServiceResult.class);
        });
    }

    // 兼容旧布尔接口
    public boolean returnBook(String userId, Integer recordId, Integer bookId) {
        return returnBookWithReason(userId, recordId, bookId).isSuccess();
    }

    public boolean renewBorrow(String userId, Integer recordId, int maxTimes) {
        return renewBorrowWithReason(userId, recordId, 30, maxTimes).isSuccess();
    }

    public boolean reserveBook(String userId, Integer bookId) {
        return reserveBookWithReason(userId, bookId).isSuccess();
    }

    public boolean borrowBook(String userId, Integer bookId) {
        return borrowBookWithReason(userId, bookId).isSuccess();
    }

    public boolean cancelReservation(String userId, Integer reservationId) {
        return TransactionManager.executeInTransaction(conn -> {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("reservationId", reservationId);
            JsonNode data = HttpClientUtil.sendPost("/library/cancel-reservation", body);
            return data.asBoolean(false);
        });
    }

    public List<BorrowRecord> listMyBorrows(String userId) {
        return TransactionManager.executeInTransaction(conn -> {
            JsonNode data = HttpClientUtil.sendGet("/library/my-borrows?userId=" + url(userId));
            return HttpClientUtil.parseList(data, BorrowRecord.class);
        });
    }

    /**
     * 根据状态筛选我的借阅（全部/借出/已还/逾期）
     */
    public List<BorrowRecord> listMyBorrowsByStatus(String userId, String status) {
        return TransactionManager.executeInTransaction(conn -> {
            String path = "/library/my-borrows?userId=" + url(userId) + "&status=" + url(status);
            JsonNode data = HttpClientUtil.sendGet(path);
            return HttpClientUtil.parseList(data, BorrowRecord.class);
        });
    }

    public List<Reservation> listMyReservations(String userId) {
        return TransactionManager.executeInTransaction(conn -> {
            JsonNode data = HttpClientUtil.sendGet("/library/my-reservations?userId=" + url(userId));
            return HttpClientUtil.parseList(data, Reservation.class);
        });
    }

    // ================= 管理员：图书维护（待服务端开放对应端点后再实现） =================
    public boolean addBook(com.vCampus.entity.Book book) { return false; }
    public boolean updateBook(com.vCampus.entity.Book book) { return false; }
    public boolean deleteBook(Integer bookId) { return false; }
    public boolean setBookStatus(Integer bookId, String status) { return false; }
    public int[] statsForBook(Integer bookId) { return new int[]{0,0,0}; }
    public java.util.List<com.vCampus.entity.BorrowRecord> listBorrowsByBook(Integer bookId) { return List.of(); }
    public java.util.List<com.vCampus.entity.BorrowRecord> listActiveBorrowsByBook(Integer bookId) { return List.of(); }
    public boolean increaseStock(Integer bookId, int delta) { return false; }
    public boolean decreaseStock(Integer bookId, int delta) { return false; }

    /**
     * 简单的冒烟测试：搜索→借书→续借→还书→预约→查看列表
     */
    public void smokeTest() {
        try {
            String userId = "48";
            System.out.println("==== LibraryService Smoke Test Start ====");

            // 1) 搜索前10本书
            java.util.List<com.vCampus.entity.Book> books = searchBooks("", 1, 10);
            System.out.println("Found books: " + books.size());
            for (com.vCampus.entity.Book b : books) {
                System.out.println("- [" + b.getBookId() + "] " + b.getTitle() + " (avail=" + b.getAvailableCopies() + ")");
            }

            if (books.isEmpty()) {
                System.out.println("No books to test.");
                return;
            }

            // 选择第一本可借的书；没有则选第一本尝试预约
            com.vCampus.entity.Book target = null;
            for (var b : books) {
                if (b.getAvailableCopies() != null && b.getAvailableCopies() > 0) { target = b; break; }
            }
            if (target == null) target = books.get(0);
            System.out.println("Target bookId=" + target.getBookId() + ", avail=" + target.getAvailableCopies());

            // 2) 借书
            boolean borrowed = borrowBook(userId, target.getBookId());
            System.out.println("borrowBook => " + borrowed);

            Integer recordIdForTarget = null;
            if (borrowed) {
                // 3) 查询我的借阅，找到这本书的记录
                java.util.List<BorrowRecord> my = listMyBorrows(userId);
                for (BorrowRecord r : my) {
                    if (r.getBookId() != null && r.getBookId().equals(target.getBookId())) {
                        recordIdForTarget = r.getRecordId();
                        break;
                    }
                }
                System.out.println("Active borrows: " + my.size() + ", recordIdForTarget=" + recordIdForTarget);

                // 4) 尝试续借一次
                if (recordIdForTarget != null) {
                    boolean renewed = renewBorrow(userId, recordIdForTarget, 1);
                    System.out.println("renewBorrow => " + renewed);
                }

                // 5) 还书
                if (recordIdForTarget != null) {
                    boolean returned = returnBook(userId, recordIdForTarget, target.getBookId());
                    System.out.println("returnBook => " + returned);
                }
            } else {
                // 2b) 借书失败则尝试预约
                boolean reserved = reserveBook(userId, target.getBookId());
                System.out.println("reserveBook => " + reserved);
                java.util.List<Reservation> res = listMyReservations(userId);
                System.out.println("My reservations: " + res.size());
            }

            System.out.println("==== LibraryService Smoke Test End ====");
        } catch (Exception e) {
            System.err.println("Smoke test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据用户ID获取用户信息
     */
    private User getUserById(Integer userId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/users/by-id/" + userId);
            return HttpClientUtil.parseObject(data, User.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取用户借阅规则信息
     */
    public String getUserBorrowRules(String userId) {
        User user = getUserById(Integer.parseInt(userId));
        if (user == null) return "用户不存在";
        
        int maxBorrow = LibraryUserRules.getMaxBorrowCount(user);
        int maxRenew = LibraryUserRules.getMaxRenewCount(user);
        int maxBorrowDays = LibraryUserRules.getMaxBorrowDays(user);
        int maxRenewDays = LibraryUserRules.getMaxRenewDays(user);
        String userType = LibraryUserRules.getUserTypeDescription(user);
        
        return String.format("%s：最多借%d本，最多续借%d次，借阅%d天，续借%d天", 
                userType, maxBorrow, maxRenew, maxBorrowDays, maxRenewDays);
    }

    private String url(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
