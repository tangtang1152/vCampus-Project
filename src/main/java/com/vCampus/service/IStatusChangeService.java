package com.vCampus.service;

import com.vCampus.entity.StatusChange;
import java.util.List;

public interface IStatusChangeService {
   
    boolean addStatusChange(StatusChange statusChange); // 添加状态变更记录 
    StatusChange getStatusChangeById(Integer changeId); // 根据ID查询状态变更记录
    List<StatusChange> getStatusChangesByStudentId(String studentId); // 根据学号查询状态变更记录
    boolean deleteStatusChange(Integer changeId); // 删除状态变更记录     
    List<StatusChange> getAllStatusChanges(); // 获取所有状态变更记录
    
}
