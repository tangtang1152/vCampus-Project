package com.vCampus.service;

import com.vCampus.entity.Teacher;
import java.util.List;

public interface ITeacherService extends IBaseService<Teacher, String> {
    
    Teacher getByUserId(Integer userId);
    
    // 也输出tbl_user对应记录
    Teacher getTeacherFull(String teacherId);
    
    // 只更新tbl_teacher
    boolean updateTeacherOnly(Teacher teacher);
    
    // 只删tbl_teacher
    boolean deleteTeacherOnly(String teacherId);
    
    List<Teacher> getTeachersByDepartment(String departmentId);
}