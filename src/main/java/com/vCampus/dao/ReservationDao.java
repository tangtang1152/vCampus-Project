package com.vCampus.dao;

import com.vCampus.entity.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao implements IReservationDao {
    @Override
    public Reservation findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_reservation WHERE reservationId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Reservation> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_reservation";
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean insert(Reservation e, Connection conn) throws SQLException {
        return createReservation(e, conn);
    }

    @Override
    public boolean update(Reservation e, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_reservation SET bookId=?, userId=?, reservedAt=?, expiresAt=?, queueOrder=?, resvStatus=? WHERE reservationId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, e.getBookId());
            ps.setObject(2, e.getUserId());
            ps.setDate(3, e.getReservedAt());
            ps.setDate(4, e.getExpiresAt());
            ps.setObject(5, e.getQueueOrder());
            ps.setString(6, e.getStatus());
            ps.setObject(7, e.getReservationId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM tbl_reservation WHERE reservationId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean createReservation(Reservation r, Connection conn) throws SQLException {
        String sql = "INSERT INTO tbl_reservation (bookId, userId, reservedAt, expiresAt, queueOrder, resvStatus) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, r.getBookId());
            ps.setObject(2, r.getUserId());
            ps.setDate(3, r.getReservedAt());
            ps.setDate(4, r.getExpiresAt());
            ps.setObject(5, r.getQueueOrder());
            ps.setString(6, r.getStatus());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) r.setReservationId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancelReservation(Integer reservationId, Connection conn) throws SQLException {
        String sql = "UPDATE tbl_reservation SET resvStatus='取消' WHERE reservationId=? AND resvStatus<>'取消'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Reservation> listActiveByBook(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_reservation WHERE bookId=? AND resvStatus='排队中' ORDER BY queueOrder ASC";
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Reservation> listActiveByUser(String userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tbl_reservation WHERE userId=? AND resvStatus='排队中' ORDER BY reservedAt ASC";
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Reservation nextInQueue(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 * FROM tbl_reservation WHERE bookId=? AND resvStatus='排队中' ORDER BY queueOrder ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId((Integer) rs.getObject("reservationId"));
        r.setBookId((Integer) rs.getObject("bookId"));
        r.setUserId((Integer) rs.getObject("userId"));
        r.setReservedAt(rs.getDate("reservedAt"));
        r.setExpiresAt(rs.getDate("expiresAt"));
        r.setQueueOrder((Integer) rs.getObject("queueOrder"));
        r.setStatus(rs.getString("resvStatus"));
        return r;
    }
}


