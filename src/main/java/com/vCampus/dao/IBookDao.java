package com.vCampus.dao;

import com.vCampus.entity.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IBookDao extends IBaseDao<Book, Integer> {
    List<Book> search(String keyword, int offset, int limit, Connection conn) throws SQLException;
    boolean decreaseAvailable(Integer bookId, Connection conn) throws SQLException;
    boolean increaseAvailable(Integer bookId, Connection conn) throws SQLException;
    // 统计用于分页的总数（与 searchAdvanced 的条件一致）
    int countAdvanced(String keyword, String status, Connection conn) throws SQLException;
}


