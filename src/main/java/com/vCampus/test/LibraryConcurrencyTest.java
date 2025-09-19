package com.vCampus.test;

import com.vCampus.entity.BorrowRecord;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LibraryConcurrencyTest {
    public static void main(String[] args) throws Exception {
        LibraryService lib = new LibraryService();
        Integer testBookId = 1; // 请根据实际数据库中的书籍ID调整
        String userA = "48";   // 请根据实际数据库中的用户ID调整
        String userB = "49";   // 可选的第二个用户

        // 预清理：归还两个用户对测试书籍的任何未还记录，避免历史残留影响本次测试
        cleanupActiveBorrow(lib, userA, testBookId);
        cleanupActiveBorrow(lib, userB, testBookId);

        System.out.println("==== 并发借书测试：同一本书被多个线程同时借阅 ====");
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(threads, Runtime.getRuntime().availableProcessors() * 2));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<String> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    start.await();
                    String uid = (idx % 2 == 0) ? userA : userB;
                    ServiceResult r = lib.borrowBookWithReason(uid, testBookId, 30);
                    synchronized (results) { results.add("T"+idx+" => "+ (r==null?"null":(r.isSuccess()?"OK":"FAIL")+"|"+(r==null?"":r.getMessage()))); }
                } catch (Exception e) {
                    synchronized (results) { results.add("T"+idx+" => EX:"+e.getMessage()); }
                } finally {
                    done.countDown();
                }
            });
        }

        long t0 = System.currentTimeMillis();
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        long t1 = System.currentTimeMillis();
        pool.shutdownNow();

        int ok = 0, fail = 0;
        for (String s : results) {
            System.out.println(s);
            if (s.contains("=> OK")) ok++; else fail++;
        }
        System.out.println("并发借书完成，用时=" + (t1 - t0) + "ms, 成功=" + ok + ", 失败=" + fail);

        // 收尾清理：把本次测试两位用户借到的测试书籍（若有）归还，便于重复运行
        cleanupActiveBorrow(lib, userA, testBookId);
        cleanupActiveBorrow(lib, userB, testBookId);

        System.out.println("\n==== 续借与还书测试（串行示例） ====");
        System.out.println("提示：可在运行后调用 LibraryService.smokeTest() 进行串行验证");
    }

    private static void cleanupActiveBorrow(LibraryService lib, String userId, Integer bookId) {
        try {
            List<BorrowRecord> list = lib.listMyBorrowsByStatus(userId, "借出");
            for (BorrowRecord r : list) {
                if (r.getBookId() != null && r.getBookId().equals(bookId)) {
                    lib.returnBookWithReason(userId, r.getRecordId(), bookId);
                }
            }
        } catch (Exception ignored) {}
    }
}
