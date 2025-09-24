package com.vCampus.dao;

import com.vCampus.entity.Reservation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IReservationDao extends IBaseDao<Reservation, Integer> {
    boolean createReservation(Reservation reservation, Connection conn) throws SQLException;
    boolean cancelReservation(Integer reservationId, Connection conn) throws SQLException;
    // 新增：按书目删除全部预约记录（用于级联清理）
    boolean deleteByBook(Integer bookId, Connection conn) throws SQLException;
    List<Reservation> listActiveByBook(Integer bookId, Connection conn) throws SQLException;
    List<Reservation> listActiveByUser(String userId, Connection conn) throws SQLException;
    Reservation nextInQueue(Integer bookId, Connection conn) throws SQLException;
}


