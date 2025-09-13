package com.vcampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vcampus.entity.Student;

public interface IStudentDao {
    // 基本的CRUD操作
    boolean insertStudent(Student student) throws SQLException;
    boolean updateStudent(Student student) throws SQLException;
    boolean deleteStudent(String studentId) throws SQLException;
    Student findStudentById(String studentId) throws SQLException;
    List<Student> findAllStudents() throws SQLException;
    
    // 特定的业务查询
    List<Student> findStudentsByClass(String classId) throws SQLException;
    List<Student> findStudentsByStatus(String status) throws SQLException;
    boolean isStudentIdExists(String studentId) throws SQLException;
    
    // 商店模块相关的新增方法
    Student findStudentByUserId(Integer userId) throws SQLException;
    boolean updateStudentStatus(String studentId, String status) throws SQLException;
    boolean isStudentActive(String studentId) throws SQLException;
    int getStudentOrderCount(String studentId) throws SQLException;
    double getStudentTotalSpending(String studentId) throws SQLException;
    List<String> getStudentRecentOrderIds(String studentId, int limit) throws SQLException;
}