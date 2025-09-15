package com.vCampus.dao;

import com.vCampus.entity.StatusChange;
import java.util.List;

public interface IStatusChangeDao {
    // 添加状态变更记录
    boolean insertStatusChange(StatusChange statusChange);
    
    // 根据变更ID查询记录
    StatusChange findStatusChangeById(Integer changeId);
    
    // 根据学号查询所有状态变更记录
    List<StatusChange> findStatusChangesByStudentId(String studentId);
    
    // 删除状态变更记录
    boolean deleteStatusChange(Integer changeId);
    
    // 获取所有状态变更记录
    List<StatusChange> findAllStatusChanges();
}