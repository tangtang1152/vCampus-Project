package com.vCampus.dao;

import com.vCampus.entity.Student;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 学生数据访问对象接口
 */
public interface IStudentDao extends IBaseDao<Student,String> {
    Student findByStudentId(String studentId, Connection conn) throws SQLException;
    Student findByUserId(Integer userId, Connection conn) throws SQLException;
    
    // 学籍模块
    List<Student> findStudentsByClass(String className,Connection conn) throws SQLException;
    List<Student> findStudentsByStatus(String status,Connection conn) throws SQLException;
    boolean isStudentIdExists(String studentId,Connection conn) throws SQLException;
    boolean updateStudentStatus(String studentId, String status,Connection conn) throws SQLException;
    boolean isStudentActive(String studentId,Connection conn) throws SQLException;
    
    // 商店模块相关的新增方法
   // Student findStudentByUserId(Integer userId) throws SQLException;    
    int getStudentOrderCount(String studentId,Connection conn) throws SQLException;
    double getStudentTotalSpending(String studentId,Connection conn) throws SQLException;
    List<String> getStudentRecentOrderIds(String studentId, int limit,Connection conn) throws SQLException;
    
}