package com.vcampus.dao;

import com.vcampus.entity.Book;
import java.util.List;

/**
 * 图书数据访问对象接口
 * 定义所有对图书表的操作方法
 */
public interface IBookDao {
    
    // ============ 基本的CRUD操作 ============
    
    /**
     * 获取所有图书列表
     */
    List<Book> getAllBooks();
    
    /**
     * 根据ID查询图书
     * @param bookId 图书ID
     */
    Book getBookById(Integer bookId);
    
    /**
     * 根据关键词搜索图书
     * @param keyword 搜索关键词（书名、作者、ISBN）
     */
    List<Book> searchBooks(String keyword);
    
    /**
     * 添加新图书
     * @param book 图书对象
     */
    boolean addBook(Book book);
    
    /**
     * 更新图书信息
     * @param book 图书对象
     */
    boolean updateBook(Book book);
    
    /**
     * 删除图书
     * @param bookId 图书ID
     */
    boolean deleteBook(Integer bookId);
    
    // ============ 借阅相关操作 ============
    
    /**
     * 借出图书（减少可借数量）
     * @param bookId 图书ID
     */
    boolean borrowBook(Integer bookId);
    
    /**
     * 归还图书（增加可借数量）
     * @param bookId 图书ID
     */
    boolean returnBook(Integer bookId);
    
    // ============ 统计查询操作 ============
    
    /**
     * 根据分类获取图书
     * @param category 图书分类
     */
    List<Book> getBooksByCategory(String category);
    
    /**
     * 获取图书总数
     */
    int getTotalBookCount();
    
    /**
     * 获取指定图书的可用数量
     * @param bookId 图书ID
     */
    int getAvailableCount(Integer bookId);
    
    /**
     * 检查图书是否可借
     * @param bookId 图书ID
     */
    boolean isBookAvailable(Integer bookId);
}