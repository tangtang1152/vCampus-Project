package com.vCampus.service;

import com.vCampus.entity.Subject;
import java.util.List;

/**
 * 课程服务接口
 * 提供对课程数据的业务逻辑操作
 */
public interface ISubjectService {
    
    /**
     * 根据课程ID获取课程信息
     */
    Subject getSubjectById(String subjectId);
    
    /**
     * 获取所有课程信息
     */
    List<Subject> getAllSubjects();
    
    /**
     * 添加新课程
     */
    boolean addSubject(Subject subject);
    
    /**
     * 更新课程信息
     */
    boolean updateSubject(Subject subject);
    
    /**
     * 删除课程
     */
    boolean deleteSubject(String subjectId);
    
    /**
     * 根据教师ID获取课程列表
     */
    List<Subject> getSubjectsByTeacherId(String teacherId);
    
    /**
     * 根据课程名称模糊查询课程
     */
    List<Subject> getSubjectsByName(String subjectName);
    
    /**
     * 验证课程信息是否完整有效
     */
    boolean validateSubject(Subject subject);
}