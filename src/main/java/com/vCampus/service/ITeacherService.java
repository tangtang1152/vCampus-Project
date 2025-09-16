package com.vCampus.service;

import com.vCampus.entity.Teacher;
import java.util.List;

/**
 * 教师服务接口
 * 提供教师相关的业务逻辑操作
 */
public interface ITeacherService {
    
    /**
     * 根据教师编号获取教师信息
     * @param teacherId 教师编号
     * @return 教师对象，如果不存在返回null
     */
    Teacher getTeacherById(String teacherId);
    
    /**
     * 根据用户ID获取教师信息
     * @param userId 用户ID
     * @return 教师对象，如果不存在返回null
     */
    Teacher getTeacherByUserId(Integer userId);
    
    /**
     * 获取所有教师
     * @return 教师列表
     */
    List<Teacher> getAllTeachers();
    
    /**
     * 添加教师
     * @param teacher 教师对象
     * @return 添加成功返回true，失败返回false
     */
    boolean addTeacher(Teacher teacher);
    
    /**
     * 更新教师信息
     * @param teacher 教师对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateTeacher(Teacher teacher);
    
    boolean updateFullTeacher(Teacher teacher);
    
    /**
     * 删除教师
     * @param teacherId 教师编号
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteTeacher(String teacherId);
    
    boolean deleteTeacherInfoOnly(String teahcerId);
    
    /**
     * 验证教师编号是否已存在
     * @param teacherId 教师编号
     * @return 存在返回true，不存在返回false
     */
    boolean isTeacherIdExists(String teacherId);
    
    /**
     * 根据部门获取教师列表
     * @param departmentId 部门ID
     * @return 教师列表
     */
    List<Teacher> getTeachersByDepartment(String departmentId);
}