package com.vcampus.main;

import com.vcampus.entity.Book;
import com.vcampus.entity.BorrowRecord;
import java.util.Date;

public class TestEntities {
    public static void main(String[] args) {
        System.out.println("=== 测试实体类 ===");
        
        // 测试Book实体类
        Book book = new Book();
        book.setBookId(1);
        book.setIsbn("9787111213826");
        book.setTitle("Java编程思想");
        book.setAuthor("Bruce Eckel");
        book.setPublisher("机械工业出版社");
        book.setPublisherYear(2020);
        book.setCategory("计算机");
        book.setTotalCount(10);
        book.setAvailable(8);
        book.setLocation("A区-1架-2层");
        
        System.out.println("📚 图书信息: " + book);
        System.out.println("书名: " + book.getTitle());
        System.out.println("作者: " + book.getAuthor());
        System.out.println("可借数量: " + book.getAvailable());
        
        // 测试BorrowRecord实体类
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(1);
        record.setStudentId("2021001");
        record.setBookId(1);
        record.setBorrowDate(new Date());
        record.setDueDate(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        record.setStatus("borrowing");
        
        System.out.println("\n📝 借阅记录: " + record);
        System.out.println("学生ID: " + record.getStudentId());
        System.out.println("状态: " + record.getStatus());
        
        System.out.println("\n✅ 实体类测试完成！");
    }
}