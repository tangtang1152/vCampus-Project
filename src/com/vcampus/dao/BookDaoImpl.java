package com.vcampus.dao;

import com.vcampus.entity.Book;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDaoImpl implements IBookDao {

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("获取所有图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, stmt, rs);
        }
        return books;
    }

    @Override
    public Book getBookById(Integer bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToBook(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据ID查询图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                books.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("搜索图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return books;
    }

    @Override
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, publisher_year, category, total_count, available, location) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setBookParameters(pstmt, book);
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("添加图书失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeResources(conn, pstmt);
        }
    }

    // ============ 其他方法先留空，后面再实现 ============
    
    @Override
    public boolean updateBook(Book book) {
    	String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, publisher_year=?, " +
                "category=?, total_count=?, available=?, location=? WHERE book_id=?";
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DBUtil.getConnection();
        pstmt = conn.prepareStatement(sql);
        setBookParameters(pstmt, book);
        pstmt.setInt(10, book.getBookId());  // 第10个参数：book_id
        
        int result = pstmt.executeUpdate();
        return result > 0;
        
    } catch (SQLException e) {
        System.err.println("更新图书失败: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        DBUtil.closeResources(conn, pstmt);
    }
    }

    @Override
    public boolean deleteBook(Integer bookId) {
    	 String sql = "DELETE FROM books WHERE book_id = ?";
         
         Connection conn = null;
         PreparedStatement pstmt = null;
         
         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, bookId);
             
             int result = pstmt.executeUpdate();
             return result > 0;
             
         } catch (SQLException e) {
             System.err.println("删除图书失败: " + e.getMessage());
             e.printStackTrace();
             return false;
         } finally {
             DBUtil.closeResources(conn, pstmt);
         }
    }

    @Override
    public boolean borrowBook(Integer bookId) {
    	  String sql = "UPDATE books SET available = available - 1 WHERE book_id = ? AND available > 0";
          
          Connection conn = null;
          PreparedStatement pstmt = null;
          
          try {
              conn = DBUtil.getConnection();
              pstmt = conn.prepareStatement(sql);
              pstmt.setInt(1, bookId);
              
              int result = pstmt.executeUpdate();
              return result > 0;
              
          } catch (SQLException e) {
              System.err.println("借阅图书失败: " + e.getMessage());
              e.printStackTrace();
              return false;
          } finally {
              DBUtil.closeResources(conn, pstmt);
          }
    }

    @Override
    public boolean returnBook(Integer bookId) {
    	 String sql = "UPDATE books SET available = available + 1 WHERE book_id = ? AND available < total_count";
         
         Connection conn = null;
         PreparedStatement pstmt = null;
         
         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, bookId);
             
             int result = pstmt.executeUpdate();
             return result > 0;
             
         } catch (SQLException e) {
             System.err.println("归还图书失败: " + e.getMessage());
             e.printStackTrace();
             return false;
         } finally {
             DBUtil.closeResources(conn, pstmt);
         }
    }

    @Override
    public List<Book> getBooksByCategory(String category) {
    	 List<Book> books = new ArrayList<>();
         String sql = "SELECT * FROM books WHERE category = ?";
         
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         
         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, category);
             rs = pstmt.executeQuery();
             
             while (rs.next()) {
                 books.add(resultSetToBook(rs));
             }
         } catch (SQLException e) {
             System.err.println("按分类查询图书失败: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(conn, pstmt, rs);
         }
         return books;
    }

    @Override
    public int getTotalBookCount() {
    	 String sql = "SELECT COUNT(*) FROM books";
         
         Connection conn = null;
         Statement stmt = null;
         ResultSet rs = null;
         
         try {
             conn = DBUtil.getConnection();
             stmt = conn.createStatement();
             rs = stmt.executeQuery(sql);
             
             if (rs.next()) {
                 return rs.getInt(1);
             }
         } catch (SQLException e) {
             System.err.println("获取图书总数失败: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(conn, stmt, rs);
         }
         return 0;
    }

    @Override
    public int getAvailableCount(Integer bookId) {
String sql = "SELECT available FROM books WHERE book_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("available");
            }
        } catch (SQLException e) {
            System.err.println("获取可用数量失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    @Override
    public boolean isBookAvailable(Integer bookId) {
    	 return getAvailableCount(bookId) > 0;
    }

    // ============ 私有辅助方法 ============
    
    private Book resultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublisherYear(rs.getInt("publisher_year"));
        book.setCategory(rs.getString("category"));
        book.setTotalCount(rs.getInt("total_count"));
        book.setAvailable(rs.getInt("available"));
        book.setLocation(rs.getString("location"));
        return book;
    }
    
    private void setBookParameters(PreparedStatement pstmt, Book book) throws SQLException {
        pstmt.setString(1, book.getIsbn());
        pstmt.setString(2, book.getTitle());
        pstmt.setString(3, book.getAuthor());
        pstmt.setString(4, book.getPublisher());
        pstmt.setInt(5, book.getPublisherYear());
        pstmt.setString(6, book.getCategory());
        pstmt.setInt(7, book.getTotalCount());
        pstmt.setInt(8, book.getAvailable());
        pstmt.setString(9, book.getLocation());
    }
}