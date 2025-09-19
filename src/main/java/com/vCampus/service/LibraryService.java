package com.vCampus.service;

import com.vCampus.dao.*;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.entity.Reservation;
import com.vCampus.entity.User;
import com.vCampus.util.TransactionManager;
import com.vCampus.util.LibraryUserRules;

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class LibraryService {
    private final IBookDao bookDao = new BookDao();
    private final IBorrowRecordDao borrowRecordDao = new BorrowRecordDao();
    private final IReservationDao reservationDao = new ReservationDao();
    private final IUserDao userDao = new UserDaoImpl();

    public List<com.vCampus.entity.Book> searchBooks(String keyword, int page, int size) {
        return TransactionManager.executeInTransaction(conn -> {
            int offset = Math.max(0, (page - 1) * size);
            return bookDao.search(keyword == null ? "" : keyword, offset, size, conn);
        });
    }

    public List<com.vCampus.entity.Book> searchBooksAdvanced(String keyword, String status, String sort, int page, int size) {
        return TransactionManager.executeInTransaction(conn -> {
            int offset = Math.max(0, (page - 1) * size);
            String kw = keyword == null ? "" : keyword;
            String st = status == null ? "全部" : status;
            String order;
            if ("书名↑".equals(sort)) order = "title ASC";
            else if ("书名↓".equals(sort)) order = "title DESC";
            else if ("可借↑".equals(sort)) order = "availableCopies ASC";
            else if ("可借↓".equals(sort)) order = "availableCopies DESC";
            else order = "bookId DESC";
            return ((com.vCampus.dao.BookDao)bookDao).searchAdvanced(kw, st, order, offset, size, conn);
        });
    }

    public ServiceResult borrowBookWithReason(String userId, Integer bookId) {
        return borrowBookWithReason(userId, bookId, 30);
    }

    public ServiceResult borrowBookWithReason(String userId, Integer bookId, int days) {
        return TransactionManager.executeInTransaction(conn -> {
            // 规则校验：下架、库存、上限、逾期
            com.vCampus.entity.Book bk = bookDao.findById(bookId, conn);
            if (bk == null) return ServiceResult.fail("图书不存在");
            if (bk.getStatus() != null && bk.getStatus().equals("下架")) return ServiceResult.fail("该图书已下架");
            if (bk.getAvailableCopies() == null || bk.getAvailableCopies() <= 0) return ServiceResult.fail("库存不足");

            Integer uid = Integer.parseInt(userId);
            
            // 暂时使用默认规则，避免复杂的用户检查
            int maxBorrowCount = 5; // 默认值
            int maxBorrowDays = 30; // 默认值
            
            int active = borrowRecordDao.countActiveBorrowsByUser(uid, conn);
            if (active >= maxBorrowCount)
                return ServiceResult.fail("超过最大借阅数 " + maxBorrowCount);
            if (borrowRecordDao.existsOverdueByUser(uid, conn))
                return ServiceResult.fail("存在逾期记录，无法借书");

            if (!bookDao.decreaseAvailable(bookId, conn)) return ServiceResult.fail("扣减库存失败");
            BorrowRecord r = new BorrowRecord();
            r.setBookId(bookId);
            r.setUserId(uid);
            LocalDate now = LocalDate.now();
            r.setBorrowDate(Date.valueOf(now));
            int d = Math.max(1, Math.min(maxBorrowDays, days));
            r.setDueDate(Date.valueOf(now.plusDays(d)));
            r.setReturnDate(null);
            r.setRenewTimes(0);
            r.setFine(0.0);
            r.setStatus("借出");
            if (!borrowRecordDao.createBorrow(r, conn)) {
                // 回滚前恢复库存
                bookDao.increaseAvailable(bookId, conn);
                return ServiceResult.fail("创建借阅记录失败");
            }
            return ServiceResult.ok("借书成功（" + d + " 天）");
        });
    }

    public ServiceResult returnBookWithReason(String userId, Integer recordId, Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> {
            // 计算罚金
            BorrowRecord r = borrowRecordDao.findById(recordId, conn);
            if (r == null) return ServiceResult.fail("借阅记录不存在");
            LocalDate today = LocalDate.now();
            long overdueDays = 0;
            if (r.getDueDate() != null && r.getDueDate().toLocalDate().isBefore(today)) {
                overdueDays = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate().toLocalDate(), today);
            }
            if (overdueDays > 0) {
                double fine = overdueDays * com.vCampus.util.DBConstants.DAILY_FINE;
                r.setFine(fine);
                r.setStatus("逾期"); // 先标为逾期
                borrowRecordDao.update(r, conn);
            }

            boolean ok = borrowRecordDao.markReturn(recordId, Date.valueOf(today), conn);
            if (!ok) return ServiceResult.fail("更新归还状态失败");
            if (!bookDao.increaseAvailable(bookId, conn)) return ServiceResult.fail("库存回滚失败");
            String msg = overdueDays > 0 ? ("归还成功，罚金 " + (overdueDays * com.vCampus.util.DBConstants.DAILY_FINE) + " 元") : "归还成功";
            return ServiceResult.ok(msg);
        });
    }

    public ServiceResult renewBorrowWithReason(String userId, Integer recordId, int maxTimes) {
        return renewBorrowWithReason(userId, recordId, 30, maxTimes);
    }

    public ServiceResult renewBorrowWithReason(String userId, Integer recordId, int days, int maxTimes) {
        return TransactionManager.executeInTransaction(conn -> {
            BorrowRecord r = borrowRecordDao.findById(recordId, conn);
            if (r == null) return ServiceResult.fail("借阅记录不存在");
            if (!"借出".equals(r.getStatus())) return ServiceResult.fail("仅在借出状态可续借");
            // 禁止逾期续借
            if (r.getDueDate() != null && r.getDueDate().toLocalDate().isBefore(LocalDate.now())) return ServiceResult.fail("已逾期，无法续借");
            
            // 规则：管理员可续借2次，其它1次；续借天数上限30天
            int userMaxRenewDays = 30; // 固定
            int userMaxRenewTimes = 1;  // 默认
            try {
                com.vCampus.entity.User u = getUserById(r.getUserId());
                if (com.vCampus.util.RBACUtil.isAdmin(u)) {
                    userMaxRenewTimes = 2;
                }
            } catch (Exception ignored) {}
            
            if (r.getRenewTimes() != null && r.getRenewTimes() >= userMaxRenewTimes) 
                return ServiceResult.fail("超过最大续借次数 " + userMaxRenewTimes);
            
            int d = Math.max(1, Math.min(userMaxRenewDays, days));
            LocalDate newDue = r.getDueDate().toLocalDate().plusDays(d);
            r.setDueDate(Date.valueOf(newDue));
            r.setRenewTimes((r.getRenewTimes() == null ? 0 : r.getRenewTimes()) + 1);
            boolean ok = borrowRecordDao.update(r, conn);
            return ok ? ServiceResult.ok("续借成功（"+ d +" 天），新的到期日：" + newDue + "，剩余续借次数：" + (userMaxRenewTimes - r.getRenewTimes())) : ServiceResult.fail("续借失败");
        });
    }

    public ServiceResult reserveBookWithReason(String userId, Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> {
            com.vCampus.entity.Book bk = bookDao.findById(bookId, conn);
            if (bk == null) return ServiceResult.fail("图书不存在");
            if (bk.getStatus() != null && bk.getStatus().equals("下架")) return ServiceResult.fail("该图书已下架");
            if (bk.getAvailableCopies() != null && bk.getAvailableCopies() > 0) return ServiceResult.fail("当前有库存，可直接借阅");
            // 取该书当前最大排队号
            List<Reservation> list = reservationDao.listActiveByBook(bookId, conn);
            int nextOrder = list.size() == 0 ? 1 : list.stream().map(Reservation::getQueueOrder).max(Integer::compareTo).orElse(0) + 1;
            Reservation r = new Reservation();
            r.setBookId(bookId);
            r.setUserId(Integer.parseInt(userId));
            r.setReservedAt(Date.valueOf(LocalDate.now()));
            r.setExpiresAt(Date.valueOf(LocalDate.now().plusDays(3)));
            r.setQueueOrder(nextOrder);
            r.setStatus("排队中");
            boolean ok = reservationDao.createReservation(r, conn);
            return ok ? ServiceResult.ok("预约成功，您的排队序号：" + nextOrder) : ServiceResult.fail("预约失败");
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
        return TransactionManager.executeInTransaction(conn -> reservationDao.cancelReservation(reservationId, conn));
    }

    public List<BorrowRecord> listMyBorrows(String userId) {
        return TransactionManager.executeInTransaction(conn -> borrowRecordDao.findActiveByUser(userId, conn));
    }

    /**
     * 根据状态筛选我的借阅（全部/借出/已还/逾期）
     */
    public List<BorrowRecord> listMyBorrowsByStatus(String userId, String status) {
        return TransactionManager.executeInTransaction(conn -> {
            if (status == null || status.isBlank() || "全部".equals(status)) {
                return borrowRecordDao.listByUser(userId, conn);
            }
            return borrowRecordDao.listByUserAndStatus(userId, status, conn);
        });
    }

    public List<Reservation> listMyReservations(String userId) {
        return TransactionManager.executeInTransaction(conn -> reservationDao.listActiveByUser(userId, conn));
    }

    // ================= 管理员：图书维护 =================
    public boolean addBook(com.vCampus.entity.Book book) {
        return TransactionManager.executeInTransaction(conn -> bookDao.insert(book, conn));
    }

    public boolean updateBook(com.vCampus.entity.Book book) {
        return TransactionManager.executeInTransaction(conn -> bookDao.update(book, conn));
    }

    public boolean deleteBook(Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> bookDao.delete(bookId, conn));
    }

    public boolean setBookStatus(Integer bookId, String status) {
        return TransactionManager.executeInTransaction(conn -> {
            com.vCampus.entity.Book b = bookDao.findById(bookId, conn);
            if (b == null) return false;
            b.setStatus(status);
            return bookDao.update(b, conn);
        });
    }

    /**
     * 统计某书的借阅数据：总次数、本月次数、当前借出数
     */
    public int[] statsForBook(Integer bookId) {
        return TransactionManager.executeInTransaction(conn -> {
            int total = borrowRecordDao.countTotalByBook(bookId, conn);
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate first = now.withDayOfMonth(1);
            java.time.LocalDate next = first.plusMonths(1);
            int month = borrowRecordDao.countMonthlyByBook(bookId, java.sql.Date.valueOf(first), java.sql.Date.valueOf(next), conn);
            int current = borrowRecordDao.countCurrentBorrowedByBook(bookId, conn);
            return new int[]{ total, month, current };
        });
    }

    public boolean increaseStock(Integer bookId, int delta) {
        return TransactionManager.executeInTransaction(conn -> {
            if (delta <= 0) return false;
            com.vCampus.entity.Book b = bookDao.findById(bookId, conn);
            if (b == null) return false;
            int total = (b.getTotalCopies() == null ? 0 : b.getTotalCopies()) + delta;
            int available = (b.getAvailableCopies() == null ? 0 : b.getAvailableCopies()) + delta;
            b.setTotalCopies(total);
            b.setAvailableCopies(available);
            return bookDao.update(b, conn);
        });
    }

    public boolean decreaseStock(Integer bookId, int delta) {
        return TransactionManager.executeInTransaction(conn -> {
            if (delta <= 0) return false;
            com.vCampus.entity.Book b = bookDao.findById(bookId, conn);
            if (b == null) return false;
            int total = (b.getTotalCopies() == null ? 0 : b.getTotalCopies()) - delta;
            int available = (b.getAvailableCopies() == null ? 0 : b.getAvailableCopies()) - delta;
            if (total < 0 || available < 0) return false;
            b.setTotalCopies(total);
            b.setAvailableCopies(available);
            return bookDao.update(b, conn);
        });
    }
    /**
     * 简单的冒烟测试：搜索→借书→续借→还书→预约→查看列表
     * 注意：依赖数据库中存在 userId=1 的用户与至少一本图书
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
        return TransactionManager.executeInTransaction(conn -> userDao.findById(userId, conn));
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

    public static void main(String[] args) {
        new LibraryService().smokeTest();
    }
}


