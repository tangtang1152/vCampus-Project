package com.vCampus.service;

import com.vCampus.entity.Choose;
import com.vCampus.entity.Subject;

import java.util.List;

/**
 * 选课服务接口
 * 提供选课相关的业务逻辑操作
 */
public interface IChooseService {
    
    /**
     * 学生选课
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @return 选课是否成功
     */
    boolean chooseSubject(String studentId, String subjectId);
    
    /**
     * 管理员代选课程 (可选择性地绕过某些校验，如时间冲突)
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @param ignoreTimeConflict 是否无视时间冲突
     * @return 代选是否成功
     */
    boolean adminAssistChooseSubject(String studentId, String subjectId, boolean ignoreTimeConflict); // 新增方法
    
    
    /**
     * 学生退选课程
     * @param selectid 选课记录ID
     * @return 退选是否成功
     */
    boolean dropSubject(String selectid); // 参数名改为selectid
    
    /**
     * 根据学生ID获取已选课程列表
     * @param studentId 学生ID
     * @return 已选课程列表
     */
    List<Subject> getStudentSubjects(String studentId);
    
    /**
     * 根据课程ID获取选课学生列表
     * @param subjectId 课程ID
     * @return 选课学生选课记录列表
     */
    List<Choose> getSubjectChooses(String subjectId);
    
    /**
     * 检查学生是否已选某课程
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @return 是否已选
     */
    boolean isSubjectChosen(String studentId, String subjectId);
    
    /**
     * 获取选课记录详情
     * @param selectid 选课记录ID
     * @return 选课记录
     */
    Choose getChooseDetail(String selectid); // 参数名改为selectid
    
    /**
     * 验证选课信息
     * @param choose 选课记录
     * @return 是否有效
     */
    boolean validateChoose(Choose choose);
    
    /**
     * 根据学生ID和课程ID查找特定的选课记录
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @return 对应的选课记录，如果不存在则返回 null
     */
    Choose findByStudentAndSubject(String studentId, String subjectId); // 新增接口方法
}