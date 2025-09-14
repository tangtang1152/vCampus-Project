package com.vCampus.service;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.StudentDao;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 学生服务类
 * 提供对学生数据的业务逻辑操作
 */
public class StudentService {

    private static final IStudentDao studentDao = new StudentDao();

    /**
     * 根据学号获取学生信息
     */
    public static Student getStudentById(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByStudentId(studentId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户ID获取学生信息
     */
    public static Student getStudentByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByUserId(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新学生信息
     */
    public static boolean updateStudent(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 首先更新用户信息
                boolean userUpdated = UserService.updateUser(student);
                if (!userUpdated) {
                    System.out.println("更新用户信息失败");
                    return false;
                }

                // 然后更新学生信息
                return studentDao.update(student, conn);
            });
        } catch (RuntimeException e) {
            System.err.println("更新学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除学生账户
     */
    public static boolean deleteStudent(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 首先获取学生信息
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student == null) {
                    System.out.println("学生不存在，学号: " + studentId);
                    return false;
                }

                // 然后删除学生信息
                boolean studentDeleted = studentDao.delete(studentId, conn);
                if (!studentDeleted) {
                    System.out.println("删除学生信息失败");
                    return false;
                }

                // 最后删除用户账户
                return UserService.deleteUser(student.getUserId());
            });
        } catch (RuntimeException e) {
            System.err.println("删除学生失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 验证学生信息是否完整有效
     */
    public static boolean validateStudent(Student student) {
    	
        // 检查学号是否有效
        if (Integer.parseInt(student.getStudentId()) <= 0) {
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