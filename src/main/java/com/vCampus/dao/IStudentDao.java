package com.vCampus.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.vCampus.entity.Student;

public interface IStudentDao extends IBaseDao<Student,String> {
   
	
    Student findByStudentId(String studentId, Connection conn) throws SQLException;
    Student findByUserId(Integer userId, Connection conn) throws SQLException;

    // 特定的业务查询
    List<Student> findStudentsByClass(String classId) throws SQLException; // 根据班级查询学生
    List<Student> findStudentsByStatus(String status) throws SQLException;  // 根据学籍状态查询学生
    boolean isStudentIdExists(String studentId) throws SQLException;
    boolean updateStudentStatus(String studentId, String newStatus) throws SQLException; // 更新学籍状态
    
    // 商店模块相关的新增方法
    Student findStudentByUserId(Integer userId) throws SQLException;
    boolean updateStudentStatus2(String studentId, String status) throws SQLException;
    boolean isStudentActive(String studentId) throws SQLException;
    int getStudentOrderCount(String studentId) throws SQLException;
    double getStudentTotalSpending(String studentId) throws SQLException;
    List<String> getStudentRecentOrderIds(String studentId, int limit) throws SQLException;
}