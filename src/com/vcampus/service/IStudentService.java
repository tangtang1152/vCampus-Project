package com.vcampus.service;

import java.util.List;
import com.vcampus.entity.Student;

/**
 * 学生服务接口
 * 定义学籍管理模块的所有业务方法
 */
public interface IStudentService {
    
    // 基础CRUD操作
    boolean addStudent(Student student);
    Student getStudentById(String studentId);
    List<Student> getAllStudents();
    boolean updateStudent(Student student);
    boolean deleteStudent(String studentId);
    
    // 查询操作
    List<Student> getStudentsByClass(String classId);
    List<Student> getStudentsByStatus(String status);
    boolean isStudentIdExists(String studentId);
    
    // 模块间交互接口
    //Student getStudentByUserId(int userId) throws Exception;
    boolean validateStudentStatus(String studentId, String requiredStatus) throws Exception;
    int getStudentBorrowLimit(String studentId) throws Exception;
    String getStudentStatus(String studentId) throws Exception;
    boolean changeStudentStatus(String studentId, String newStatus, 
                              String reason, String operator) throws Exception;
    
    // 商店模块相关的方法
    Student getStudentByUserId(Integer userId);
    int getStudentOrderCount(String studentId);
    double getStudentTotalSpending(String studentId);
    boolean isStudentActive(String studentId);
    boolean updateStudentStatus(String studentId, String status);
    
}
