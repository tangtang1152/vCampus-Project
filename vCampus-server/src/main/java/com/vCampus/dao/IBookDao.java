package com.vCampus.dao;

import com.vCampus.entity.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IBookDao extends IBaseDao<Book, Integer> {
    List<Book> search(String keyword, int offset, int limit, Connection conn) throws SQLException;
    boolean decreaseAvailable(Integer bookId, Connection conn) throws SQLException;
    boolean increaseAvailable(Integer bookId, Connection conn) throws SQLException;
}


