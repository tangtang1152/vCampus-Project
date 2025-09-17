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
    //------------------------------------------------
    List<Student> getStudentsByStatus(String status);
    
    boolean validateStudentStatus(String studentId, String requiredStatus) throws Exception;
    String getStudentStatus(String studentId) throws Exception;
    //boolean changeStudentStatus(String studentId, String newStatus, 
    //                          String reason, String operator) throws Exception;
    boolean isStudentActive(String studentId);
    boolean updateStudentStatus(String studentId, String status);
    //int getStudentBorrowLimit(String studentId) throws Exception;
    
    // 商店模块相关的方法
    int getStudentOrderCount(String studentId);
    double getStudentTotalSpending(String studentId);

}