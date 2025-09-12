package com.vCampus.service;

import com.vCampus.dao.StudentDao;
import com.vCampus.dao.UserDao;
import com.vCampus.entity.Student;
import java.sql.SQLException;

/**
 * 学生服务类
 * 提供对学生数据的业务逻辑操作
 * 作为控制器和数据访问对象之间的中间层
 */
public class StudentService {
    
    /**
     * 根据学号获取学生信息
     * 
     * @param studentId 要查询的学生学号
     * @return 找到的学生对象，如果未找到或发生错误则返回null
     */
    public static Student getStudentById(int studentId) {
        try {
            return StudentDao.findByStudentId(studentId);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("获取学生信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据用户ID获取学生信息
     * 
     * @param userId 要查询的用户ID
     * @return 找到的学生对象，如果未找到或发生错误则返回null
     */
    public static Student getStudentByUserId(int userId) {
        try {
            return StudentDao.findByUserId(userId);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("获取学生信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 注册学生账户
     * 包括创建用户账户和学生信息
     * 
     * @param student 要注册的学生对象
     * @return 注册成功返回true，否则返回false
     */
    public static boolean registerStudent(Student student) {
        try {
            // 首先创建用户账户
            boolean userCreated = UserDao.createUser(student);
            if (!userCreated) {
                System.out.println("创建用户账户失败");
                return false;
            }
            
            // 然后创建学生信息
            return StudentDao.createStudent(student);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("注册学生时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新学生信息
     * 
     * @param student 要更新的学生对象
     * @return 更新成功返回true，否则返回false
     */
    public static boolean updateStudent(Student student) {
        try {
            // 首先更新用户信息
            boolean userUpdated = UserDao.updateUser(student);
            if (!userUpdated) {
                System.out.println("更新用户信息失败");
                return false;
            }
            
            // 然后更新学生信息
            return StudentDao.updateStudent(student);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("更新学生信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除学生账户
     * 包括删除学生信息和用户账户
     * 
     * @param studentId 要删除的学生学号
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteStudent(int studentId) {
        try {
            // 首先获取学生信息
            Student student = StudentDao.findByStudentId(studentId);
            if (student == null) {
                System.out.println("学生不存在，学号: " + studentId);
                return false;
            }
            
            // 然后删除学生信息
            boolean studentDeleted = StudentDao.deleteStudent(studentId);
            if (!studentDeleted) {
                System.out.println("删除学生信息失败");
                return false;
            }
            
            // 最后删除用户账户
            return UserDao.deleteUser(student.getUserId());
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("删除学生时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 验证学生信息是否完整有效
     * 
     * @param student 要验证的学生对象
     * @return 如果学生信息完整有效返回true，否则返回false
     */
    public static boolean validateStudent(Student student) {
        // 检查学号是否有效
        if (student.getStudentId() <= 0) {
            System.out.println("学号无效: " + student.getStudentId());
            return false;
        }
        
        // 检查学生姓名是否为空
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            System.out.println("学生姓名为空");
            return false;
        }
        
        // 检查班级名称是否为空
        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            System.out.println("班级名称为空");
            return false;
        }
        
        // 检查用户名是否为空
        if (student.getUsername() == null || student.getUsername().trim().isEmpty()) {
            System.out.println("用户名为空");
            return false;
        }
        
        // 检查密码是否为空
        if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
            System.out.println("密码为空");
            return false;
        }
        
        return true;
    }
}