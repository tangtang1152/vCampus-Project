package com.vcampus.service;

import com.vcampus.dao.BookDaoImpl;
import com.vcampus.dao.BorrowRecordDaoImpl;
import com.vcampus.dao.IBookDao;
import com.vcampus.dao.IBorrowRecordDao;
import com.vcampus.entity.BorrowRecord;
import com.vcampus.entity.Book;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LibraryService {
    private IBookDao bookDao = new BookDaoImpl();
    private IBorrowRecordDao borrowRecordDao = new BorrowRecordDaoImpl();

    /**
     * 借阅图书
     */
    public boolean borrowBook(Integer bookId, String studentId, int borrowDays) {
        if (borrowDays <= 0) {
            System.out.println("❌ 借阅天数必须大于0");
            return false;
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            System.out.println("❌ 学生ID不能为空");
            return false;
        }

        if (!bookDao.isBookAvailable(bookId)) {
            System.out.println("❌ 图书不可借或已借完");
            return false;
        }
        if (hasUserOverdueBooks(studentId)) {
            System.out.println("❌ 用户有超期未还的图书，无法借阅");
            return false;
        }
        if (hasUserBorrowedSameBook(studentId, bookId)) {
            System.out.println("❌ 用户已借阅该图书且未归还");
            return false;
        }

        if (!bookDao.borrowBook(bookId)) {
            System.out.println("❌ 更新图书库存失败");
            return false;
        }

        BorrowRecord record = new BorrowRecord();
        record.setStudentId(studentId);
        record.setBookId(bookId);
        record.setBorrowDate(new Date());
        long dueTime = System.currentTimeMillis() + borrowDays * 24L * 60 * 60 * 1000;
        record.setDueDate(new Date(dueTime));
        record.setStatus("borrowing");

        boolean result = borrowRecordDao.insertBorrowRecord(record);
        if (result) {
            System.out.println("✅ 借阅成功");
            System.out.println("   应还日期: " + record.getDueDate());
        } else {
            System.out.println("❌ 保存借阅记录失败");
            bookDao.returnBook(bookId);
        }

        return result;
    }


    /**
     * 判断用户是否已借过同一本书（忽略 deleted 状态）
     */
    public boolean hasUserBorrowedSameBook(String studentId, int bookId) {
        try {
            List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
            for (BorrowRecord record : records) {
                if (record.getBookId() == bookId && "borrowing".equals(record.getStatus())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 归还图书
     */
    public boolean returnBook(String studentId, int bookId) {
        try {
            // 查询该学生该图书的最新借阅记录（状态为 borrowing）
            BorrowRecord record = borrowRecordDao.getLatestBorrowingRecord(studentId, bookId);
            if (record == null) {
                System.out.println("❌ 没有可归还的借阅记录");
                return false;
            }

            // 更新归还信息
            record.setReturnDate(new Date());
            record.setStatus("returned"); // 可以使用 returned 状态
            borrowRecordDao.updateBorrowRecord(record); // ✅ 修改这里

            System.out.println("✅ 归还成功: 学生 " + studentId + ", 图书 " + bookId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 续借图书
     */
    public boolean renewBook(Integer recordId, int additionalDays) {
        // 1. 获取借阅记录
        BorrowRecord record = borrowRecordDao.getBorrowRecordById(recordId);
        if (record == null) {
            System.out.println("❌ 借阅记录不存在");
            return false;
        }

        if (record.isReturned()) {
            System.out.println("❌ 图书已归还，无法续借");
            return false;
        }

        if (!record.canRenew()) {
            System.out.println("❌ 图书已超期或不符合续借条件");
            return false;
        }

        // 2. 更新应还日期
        long newDueTime = record.getDueDate().getTime() + additionalDays * 24L * 60 * 60 * 1000;
        record.setDueDate(new Date(newDueTime));

        // 3. 更新数据库
        boolean updated = borrowRecordDao.updateBorrowRecord(record); // ✅ 修改这里
        if (!updated) {
            System.out.println("❌ 更新续借信息到数据库失败");
            return false;
        }

        System.out.println("✅ 续借成功，新的应还日期: " + record.getDueDate());
        return true;
    }
    
    /**
     * 显示用户借阅记录
     */
    public void showUserBorrowRecords(String studentId) {
        List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
        System.out.println("📋 用户 " + studentId + " 的借阅记录:");

        if (records.isEmpty()) {
            System.out.println("   暂无借阅记录");
            return;
        }

        for (BorrowRecord record : records) {
            System.out.println("   - 记录ID: " + record.getRecordId() +
                             ", 图书ID: " + record.getBookId() +
                             ", 状态: " + record.getStatus() +
                             ", 借出日期: " + record.getBorrowDate());

            if (record.isOverdue()) {
                System.out.println("     ⚠️ 超期警告: 已超期 " + Math.abs(record.getRemainingDays()) + "天");
            }
        }
    }

    /**
     * 判断用户是否有超期未归还的图书（忽略 deleted 状态）
     */
    public boolean hasUserOverdueBooks(String studentId) {
        try {
            List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
            Date now = new Date();
            for (BorrowRecord record : records) {
                if ("borrowing".equals(record.getStatus()) && record.getDueDate().before(now)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户当前借阅中的图书数量
     */
    public int getUserBorrowingCount(String studentId) {
        List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
        int count = 0;
        for (BorrowRecord record : records) {
            if (record.isBorrowing()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 显示所有超期图书
     */
    public void showAllOverdueBooks() {
        // 获取所有借阅记录
        List<BorrowRecord> allRecords = borrowRecordDao.getAllBorrowRecords();
        
        System.out.println("📋 所有超期图书:");
        
        if (allRecords.isEmpty()) {
            System.out.println("   暂无借阅记录");
            return;
        }
        
        boolean hasOverdue = false;
        
        for (BorrowRecord record : allRecords) {
            // 只显示未归还且超期的记录
            if (record.getReturnDate() == null && record.isOverdue()) {
                hasOverdue = true;
                System.out.println("   - 记录ID: " + record.getRecordId() +
                                 ", 学生ID: " + record.getStudentId() +
                                 ", 图书ID: " + record.getBookId() +
                                 ", 借出日期: " + record.getBorrowDate() +
                                 ", 应还日期: " + record.getDueDate() +
                                 ", 超期天数: " + Math.abs(record.getRemainingDays()) + "天");
            }
        }
        
        if (!hasOverdue) {
            System.out.println("   暂无超期图书");
        }
    }

    /**
     * 获取所有超期图书列表（返回List）
     */
    public List<BorrowRecord> getAllOverdueBooks() {
        List<BorrowRecord> allRecords = borrowRecordDao.getAllBorrowRecords();
        List<BorrowRecord> overdueRecords = new ArrayList<>();
        
        for (BorrowRecord record : allRecords) {
            if (record.getReturnDate() == null && record.isOverdue()) {
                overdueRecords.add(record);
            }
        }
        
        return overdueRecords;
    }

    /**
     * 显示指定学生的超期图书
     */
    public void showStudentOverdueBooks(String studentId) {
        List<BorrowRecord> studentRecords = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
        
        System.out.println("📋 学生 " + studentId + " 的超期图书:");
        
        if (studentRecords.isEmpty()) {
            System.out.println("   该学生暂无借阅记录");
            return;
        }
        
        boolean hasOverdue = false;
        
        for (BorrowRecord record : studentRecords) {
            if (record.getReturnDate() == null && record.isOverdue()) {
                hasOverdue = true;
                System.out.println("   - 记录ID: " + record.getRecordId() +
                                 ", 图书ID: " + record.getBookId() +
                                 ", 借出日期: " + record.getBorrowDate() +
                                 ", 应还日期: " + record.getDueDate() +
                                 ", 超期天数: " + Math.abs(record.getRemainingDays()) + "天");
            }
        }
        
        if (!hasOverdue) {
            System.out.println("   该学生暂无超期图书");
        }
    }

    /**
     * 获取图书借阅历史
     */
    public void showBookBorrowHistory(Integer bookId) {
        List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByBookId(bookId);
        System.out.println("📋 图书 " + bookId + " 的借阅历史:");

        if (records.isEmpty()) {
            System.out.println("   暂无借阅历史");
            return;
        }

        for (BorrowRecord record : records) {
            System.out.println("   - 学生: " + record.getStudentId() +
                             ", 状态: " + record.getStatus() +
                             ", 借出: " + record.getBorrowDate() +
                             (record.getReturnDate() != null ? ", 归还: " + record.getReturnDate() : ""));
        }
    }

    /**
     * 获取所有借阅记录
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordDao.getAllBorrowRecords();
    }

    
    /**
     * 根据图书ID获取借阅记录
     */
    public List<BorrowRecord> getBorrowRecordsByBookId(int bookId) {
        return borrowRecordDao.getBorrowRecordsByBookId(bookId);
    }

    
    /**
     * 根据学生ID获取借阅记录
     */
    public List<BorrowRecord> getBorrowRecordsByStudentId(String studentId) {
        return borrowRecordDao.getBorrowRecordsByStudentId(studentId);
    }
    
    
    /**
     * 使用硬删除清理学生的借阅记录
     */
    public boolean cleanupStudentRecordsHard(String studentId) {
        try {
            List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
            for (BorrowRecord record : records) {
                if (!record.isReturned()) {
                    Book book = bookDao.getBookById(record.getBookId());
                    if (book != null) {
                        book.setAvailable(book.getAvailable() + 1);
                        bookDao.updateBook(book);
                    }
                }
                borrowRecordDao.deleteBorrowRecord(record.getRecordId());
            }
            System.out.println("✅ 硬删除完成: 学生 " + studentId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 使用软删除清理学生的借阅记录
     */
    public boolean cleanupStudentRecordsSoft(String studentId) {
        try {
            List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId(studentId);
            for (BorrowRecord record : records) {
                if (!record.isReturned()) {
                    Book book = bookDao.getBookById(record.getBookId());
                    if (book != null) {
                        book.setAvailable(book.getAvailable() + 1);
                        bookDao.updateBook(book);
                    }
                }
                borrowRecordDao.softDeleteBorrowRecord(record.getRecordId());
            }
            System.out.println("✅ 软删除完成: 学生 " + studentId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重置图书库存 - 将指定图书的可借数量重置
     */
    public boolean resetBookInventory(int bookId, int availableCount) {
        try {
            Book book = bookDao.getBookById(bookId);
            if (book != null) {
                book.setAvailable(availableCount);
                return bookDao.updateBook(book);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

      
}

