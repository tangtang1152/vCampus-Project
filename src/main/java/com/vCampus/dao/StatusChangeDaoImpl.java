package com.vCampus.dao;

import com.vCampus.entity.StatusChange;
import com.vCampus.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatusChangeDaoImpl implements IStatusChangeDao {

    @Override
    public boolean insertStatusChange(StatusChange statusChange) {
        String sql = "INSERT INTO tbl_status_change (studentId, oldStatus, newStatus, changeDate, reason) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, statusChange.getStudentId());
            ps.setString(2, statusChange.getOldStatus());
            ps.setString(3, statusChange.getNewStatus());
            ps.setTimestamp(4, new Timestamp(statusChange.getChangeDate().getTime()));
            ps.setString(5, statusChange.getReason());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public StatusChange findStatusChangeById(Integer changeId) {
        String sql = "SELECT * FROM tbl_status_change WHERE changeId=?";
        StatusChange statusChange = null;
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, changeId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                statusChange = new StatusChange();
                statusChange.setChangeId(rs.getInt("changeId"));
                statusChange.setStudentId(rs.getString("studentId"));
                statusChange.setOldStatus(rs.getString("oldStatus"));
                statusChange.setNewStatus(rs.getString("newStatus"));
                statusChange.setChangeDate(rs.getTimestamp("changeDate"));
                statusChange.setReason(rs.getString("reason"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return statusChange;
    }

    @Override
    public List<StatusChange> findStatusChangesByStudentId(String studentId) {
        String sql = "SELECT * FROM tbl_status_change WHERE studentId=? ORDER BY changeDate DESC";
        List<StatusChange> statusChanges = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                StatusChange statusChange = new StatusChange();
                statusChange.setChangeId(rs.getInt("changeId"));
                statusChange.setStudentId(rs.getString("studentId"));
                statusChange.setOldStatus(rs.getString("oldStatus"));
                statusChange.setNewStatus(rs.getString("newStatus"));
                statusChange.setChangeDate(rs.getTimestamp("changeDate"));
                statusChange.setReason(rs.getString("reason"));
                
                statusChanges.add(statusChange);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return statusChanges;
    }

    @Override
    public boolean deleteStatusChange(Integer changeId) {
        String sql = "DELETE FROM tbl_status_change WHERE changeId=?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, changeId);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<StatusChange> findAllStatusChanges() {
        String sql = "SELECT * FROM tbl_status_change ORDER BY changeDate DESC";
        List<StatusChange> statusChanges = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                StatusChange statusChange = new StatusChange();
                statusChange.setChangeId(rs.getInt("changeId"));
                statusChange.setStudentId(rs.getString("studentId"));
                statusChange.setOldStatus(rs.getString("oldStatus"));
                statusChange.setNewStatus(rs.getString("newStatus"));
                statusChange.setChangeDate(rs.getTimestamp("changeDate"));
                statusChange.setReason(rs.getString("reason"));
                
                statusChanges.add(statusChange);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return statusChanges;
    }
}
