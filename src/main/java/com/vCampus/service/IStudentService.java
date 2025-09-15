package com.vCampus.service;

import java.util.List;
import com.vCampus.entity.Student;

/**
 * 学生服务接口
 * 定义学籍管理模块的所有业务方法
 */
public interface IStudentService {
    
    // 基础CRUD操作
    boolean addStudent(Student student); // 添加学生
    boolean updateStudent(Student student); // 更新学生信息
    boolean deleteStudent(String studentId); // 删除学生
    Student getStudentById(String studentId); // 根据学号查询学生
    List<Student> getAllStudents(); // 查询所有学生
    
    // 查询操作
    List<Student> getStudentsByClass(String classId); // 根据班级查询学生
    List<Student> getStudentsByStatus(String status); // 根据学籍状态查询学生
    boolean isStudentIdExists(String studentId);
    
    
    // 验证学生信息
   // boolean validateStudentInfo(Student student);
    
    // 模块间交互接口
    //Student getStudentByUserId(int userId) throws Exception;
    boolean validateStudentStatus(String studentId, String requiredStatus) throws Exception;
    int getStudentBorrowLimit(String studentId) throws Exception;
    String getStudentStatus(String studentId) throws Exception;
    boolean changeStudentStatus(String studentId, String newStatus, 
                              String reason, String operator) throws Exception;  // 更新学籍状态（带变更记录）
    
    // 商店模块相关的方法
    Student getStudentByUserId(Integer userId);
    int getStudentOrderCount(String studentId);
    double getStudentTotalSpending(String studentId);
    boolean isStudentActive(String studentId);
    boolean updateStudentStatus(String studentId, String status);
    
}
