package com.vCampus.service;

import com.vCampus.dao.IStatusChangeDao;
import com.vCampus.dao.StatusChangeDaoImpl;
import com.vCampus.entity.StatusChange;
import java.util.List;

public class StatusChangeServiceImpl implements IStatusChangeService {

    private IStatusChangeDao statusChangeDAO = new StatusChangeDaoImpl();

    @Override
    public boolean addStatusChange(StatusChange statusChange) {
        if (statusChange == null) return false;
        if (statusChange.getStudentId() == null || statusChange.getStudentId().trim().isEmpty()) return false;
        if (statusChange.getNewStatus() == null || statusChange.getNewStatus().trim().isEmpty()) return false;
        if (statusChange.getChangeDate() == null) return false;
        
        return statusChangeDAO.insertStatusChange(statusChange);
    }

    @Override
    public StatusChange getStatusChangeById(Integer changeId) {
        return statusChangeDAO.findStatusChangeById(changeId);
    }

    @Override
    public List<StatusChange> getStatusChangesByStudentId(String studentId) {
        return statusChangeDAO.findStatusChangesByStudentId(studentId);
    }

    @Override
    public boolean deleteStatusChange(Integer changeId) {
        return statusChangeDAO.deleteStatusChange(changeId);
    }

    @Override
    public List<StatusChange> getAllStatusChanges() {
        return statusChangeDAO.findAllStatusChanges();
    }
}
