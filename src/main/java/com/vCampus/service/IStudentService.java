package com.vCampus.service;

import com.vCampus.entity.Student;
import java.util.List;

/**
 * 学生服务接口
 * 提供学生相关的业务逻辑操作
 */
public interface IStudentService {
    
    /**
     * 根据学号获取学生信息
     * @param studentId 学号
     * @return 学生对象，如果不存在返回null
     */
    Student getStudentById(String studentId);
    
    /**
     * 根据用户ID获取学生信息
     * @param userId 用户ID
     * @return 学生对象，如果不存在返回null
     */
    Student getStudentByUserId(Integer userId);
    
    /**
     * 获取所有学生
     * @return 学生列表
     */
    List<Student> getAllStudents();
    
    /**
     * 添加学生
     * @param student 学生对象
     * @return 添加成功返回true，失败返回false
     */
    boolean addStudent(Student student);
    
    /**
     * 更新学生信息
     * @param student 学生对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStudent(Student student);
    
    /**
     * 完整更新学生信息（包括用户基本信息和学生特定信息）
     * @param student 包含更新信息的学生对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateFullStudent(Student student);
    
    /**
     * 删除学生（包括学生信息和关联的用户信息）
     * @param studentId 学号
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteStudent(String studentId);
    
    /**
     * 只删除学生信息（不删除用户信息）
     * @param studentId 学号
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteStudentInfoOnly(String studentId);
    
    /**
     * 验证学号是否已存在
     * @param studentId 学号
     * @return 存在返回true，不存在返回false
     */
    boolean isStudentIdExists(String studentId);
    
    /**
     * 根据班级获取学生列表
     * @param className 班级名称
     * @return 学生列表
     */
    List<Student> getStudentsByClass(String className);
}