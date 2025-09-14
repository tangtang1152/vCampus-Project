package com.vcampus.main;

import com.vcampus.dao.BorrowRecordDaoImpl;
import com.vcampus.dao.IBorrowRecordDao;
import com.vcampus.entity.BorrowRecord;
import java.util.Date;
import java.util.List;

public class TestBorrowFunction {
    public static void main(String[] args) {
        IBorrowRecordDao borrowRecordDao = new BorrowRecordDaoImpl();
        
        // 测试插入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setStudentId("2023001");
        record.setBookId(1);
        record.setBorrowDate(new Date());
        
        // 设置30天后为应还日期
        long dueTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000;
        record.setDueDate(new Date(dueTime));
        record.setStatus("borrowing");
        
        boolean insertSuccess = borrowRecordDao.insertBorrowRecord(record);
        System.out.println("插入借阅记录: " + (insertSuccess ? "成功" : "失败"));
        
        // 测试查询借阅记录
        List<BorrowRecord> records = borrowRecordDao.getBorrowRecordsByStudentId("2023001");
        System.out.println("学生2023001的借阅记录:");
        for (BorrowRecord r : records) {
            System.out.println(r);
        }
        
        // 测试归还图书
        if (!records.isEmpty()) {
            boolean returnSuccess = borrowRecordDao.returnBook(records.get(0).getRecordId());
            System.out.println("归还图书: " + (returnSuccess ? "成功" : "失败"));
        }
    }
}