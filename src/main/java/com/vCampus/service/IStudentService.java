package com.vCampus.service;

import com.vCampus.entity.Student;
import java.util.List;


public interface IStudentService extends IBaseService<Student,String>{
	
    
    Student getByUserId(Integer userId);
    
    //也输出tbl_user对应记录
    Student getStudentFull(String studentId);
    
    //只更新tbl_student
    boolean updateStudentOnly(Student student);
    
    //只删tbl_student
    boolean deleteStudentOnly(String studentId);
    
    List<Student> getStudentsByClass(String className);
}