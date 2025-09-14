package com.vcampus.main;

import com.vcampus.dao.BookDaoImpl;
import com.vcampus.entity.Book;

public class TestBookDao {
    public static void main(String[] args) {
        System.out.println("=== 开始测试DAO层 ===");
        
        BookDaoImpl bookDao = new BookDaoImpl();
        
        // 先添加一些测试数据
        addTestData(bookDao);
        
        // 测试获取所有图书
        System.out.println("📚 所有图书数量: " + bookDao.getAllBooks().size());
        
        // 测试搜索功能
        System.out.println("🔍 搜索'Java'结果数量: " + bookDao.searchBooks("Java").size());
        System.out.println("🔍 搜索'Python'结果数量: " + bookDao.searchBooks("Python").size());
        
        System.out.println("=== DAO层测试完成 ===");
    }
    
    private static void addTestData(BookDaoImpl bookDao) {
        System.out.println("📝 添加测试数据...");
        
        // 测试数据1：Java图书
        Book book1 = new Book();
        book1.setIsbn("9787111213826");
        book1.setTitle("Java编程思想");
        book1.setAuthor("Bruce Eckel");
        book1.setPublisher("机械工业出版社");
        book1.setPublisherYear(2007);
        book1.setCategory("编程语言");
        book1.setTotalCount(10);
        book1.setAvailable(8);
        book1.setLocation("A区-1架-1层");
        
        boolean result1 = bookDao.addBook(book1);
        System.out.println("添加Java图书: " + (result1 ? "成功" : "失败"));
        
        // 测试数据2：Python图书
        Book book2 = new Book();
        book2.setIsbn("9787115428028");
        book2.setTitle("Python基础教程");
        book2.setAuthor("Magnus Lie Hetland");
        book2.setPublisher("人民邮电出版社");
        book2.setPublisherYear(2018);
        book2.setCategory("编程语言");
        book2.setTotalCount(5);
        book2.setAvailable(3);
        book2.setLocation("A区-1架-2层");
        
        boolean result2 = bookDao.addBook(book2);
        System.out.println("添加Python图书: " + (result2 ? "成功" : "失败"));
        
        // 测试数据3：数据库图书
        Book book3 = new Book();
        book3.setIsbn("9787121200300");
        book3.setTitle("数据库系统概念");
        book3.setAuthor("Abraham Silberschatz");
        book3.setPublisher("机械工业出版社");
        book3.setPublisherYear(2012);
        book3.setCategory("数据库");
        book3.setTotalCount(7);
        book3.setAvailable(5);
        book3.setLocation("B区-2架-1层");
        
        boolean result3 = bookDao.addBook(book3);
        System.out.println("添加数据库图书: " + (result3 ? "成功" : "失败"));
    }
}