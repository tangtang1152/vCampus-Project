package com.vCampus.service;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.StudentDao;
import com.vCampus.dao.UserDao;
import com.vCampus.entity.Student;
import com.vCampus.entity.User;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 用户服务类
 * 提供对用户数据的业务逻辑操作
 */
public class UserService {

    private static final IUserDao userDao = new UserDao();
    private static final IStudentDao studentDao = new StudentDao();

    /**
     * 用户登录验证
     */
    public static User login(String username, String password) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                boolean isValid = userDao.validateUser(username, password, conn);
                if (isValid) {
                    return userDao.findByUsername(username, conn);
                }
                return null;
            });
        } catch (RuntimeException e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用户注册
     */
    public static boolean register(User user) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 如果是Student对象，需要特殊处理
                if (user instanceof Student) {
                    return registerStudent((Student) user, conn);
                }
                
                // 通用的用户验证和注册逻辑
                if (!ValidationService.validateUser(user)) {
                    return false;
                }

                User existingUser = userDao.findByUsername(user.getUsername(), conn);
                if (existingUser != null) {
                    System.out.println("用户名已存在: " + user.getUsername());
                    return false;
                }

                return userDao.insert(user, conn);
            });
        } catch (RuntimeException e) {
            System.err.println("注册失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册学生账户（事务内部使用）
     */
    private static boolean registerStudent(Student student, Connection conn) throws SQLException {
        System.out.println("注册学生: " + student.getStudentName() + ", 学号: " + student.getStudentId());

        // 验证学生数据
        if (!ValidationService.validateStudent(student)) {
            return false;
        }

        // 检查学号是否已存在
        Student existingStudent = studentDao.findByStudentId(student.getStudentId(), conn);
        if (existingStudent != null) {
            System.out.println("学号已存在: " + student.getStudentId());
            return false;
        }

        // 检查用户名是否已存在
        User existingUser = userDao.findByUsername(student.getUsername(), conn);
        if (existingUser != null) {
            System.out.println("用户名已存在: " + student.getUsername());
            return false;
        }

        // 首先创建用户账户
        boolean userCreated = userDao.insert(student, conn);
        if (!userCreated) {
            System.out.println("创建用户账户失败");
            return false;
        }

        // 然后创建学生信息
        return studentDao.insert(student, conn);
    }

    /**
     * 根据用户ID获取用户信息
     */
    public static User getUserById(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findById(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户名获取用户信息
     */
    public static User getUserByUsername(String username) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findByUsername(username, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新用户信息
     */
    public static boolean updateUser(User user) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.update(user, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除用户
     */
    public static boolean deleteUser(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.delete(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 为其他角色预留的方法
    public static boolean registerTeacher(User teacher) {
        // TODO: 实现教师注册逻辑
        return false;
    }

    public static boolean registerAdmin(User admin) {
        // TODO: 实现管理员注册逻辑
        return false;
    }
}