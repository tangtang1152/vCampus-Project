package com.vCampus.dao;

import com.vCampus.entity.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao implements IBookDao {

    @Override
    public Book findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_book WHERE bookId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Book> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_book";
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean insert(Book e, Connection conn) throws SQLException {
        String sql = "INSERT INTO tbl_book (isbn, title, author, category, publisher, pubDate, totalCopies, availableCopies, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getIsbn());
            ps.setString(2, e.getTitle());
            ps.setString(3, e.getAuthor());
            ps.setString(4, e.getCategory());
            ps.setString(5, e.getPublisher());
            ps.setDate(6, e.getPubDate());
            ps.setObject(7, e.getTotalCopies());
            ps.setObject(8, e.getAvailableCopies());
            ps.setString(9, e.getLocation());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        e.setBookId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Book e, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_book SET isbn=?, title=?, author=?, category=?, publisher=?, pubDate=?, totalCopies=?, availableCopies=?, location=? WHERE bookId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getIsbn());
            ps.setString(2, e.getTitle());
            ps.setString(3, e.getAuthor());
            ps.setString(4, e.getCategory());
            ps.setString(5, e.getPublisher());
            ps.setDate(6, e.getPubDate());
            ps.setObject(7, e.getTotalCopies());
            ps.setObject(8, e.getAvailableCopies());
            ps.setString(9, e.getLocation());
            ps.setInt(10, e.getBookId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_book WHERE bookId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Book> search(String keyword, int offset, int limit, Connection conn) throws SQLException {
        String like = "%" + keyword + "%";
        String sql = "SELECT * FROM tbl_book WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY bookId DESC";
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                int skipped = 0;
                int taken = 0;
                while (rs.next()) {
                    if (skipped < offset) { skipped++; continue; }
                    list.add(mapRow(rs));
                    taken++;
                    if (taken >= limit) break;
                }
            }
        }
        return list;
    }

    @Override
    public boolean decreaseAvailable(Integer bookId, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_book SET availableCopies = availableCopies - 1 WHERE bookId = ? AND availableCopies > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean increaseAvailable(Integer bookId, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_book SET availableCopies = availableCopies + 1 WHERE bookId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("bookId"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setPublisher(rs.getString("publisher"));
        b.setPubDate(rs.getDate("pubDate"));
        b.setTotalCopies((Integer) rs.getObject("totalCopies"));
        b.setAvailableCopies((Integer) rs.getObject("availableCopies"));
        b.setLocation(rs.getString("location"));
        // 无 status 列
        return b;
    }
}


