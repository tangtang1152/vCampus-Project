package com.vCampus.service;

import com.vCampus.dao.UserDao;
import com.vCampus.entity.User;
import java.sql.SQLException;

/**
 * 用户服务类
 * 提供对用户数据的业务逻辑操作
 * 作为控制器和数据访问对象之间的中间层
 */
public class UserService {
    
    /**
     * 用户登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回用户对象，否则返回null
     */
    public static User login(String username, String password) {
        try {
            // 验证用户凭据
            boolean isValid = UserDao.validateUser(username, password);
            if (isValid) {
                // 如果验证成功，返回用户信息
                return UserDao.findByUsername(username);
            }
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("用户登录验证时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 用户注册
     * 
     * @param user 要注册的用户对象
     * @return 注册成功返回true，否则返回false
     */
    public static boolean register(User user) {
        try {
        	
            // 首先验证数据长度
            if (!ValidationService.validateUser(user)) {
                return false;
            }
            
            // 检查用户名是否已存在
            User existingUser = UserDao.findByUsername(user.getUsername());
            if (existingUser != null) {
                System.out.println("用户名已存在: " + user.getUsername());
                return false;
            }
            
            // 创建新用户
            return UserDao.createUser(user);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("用户注册时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 要查询的用户ID
     * @return 找到的用户对象，如果未找到或发生错误则返回null
     */
    public static User getUserById(int userId) {
        try {
            return UserDao.findByUserId(userId);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("获取用户信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 要查询的用户名
     * @return 找到的用户对象，如果未找到或发生错误则返回null
     */
    public static User getUserByUsername(String username) {
        try {
            return UserDao.findByUsername(username);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("获取用户信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 更新用户信息
     * 
     * @param user 要更新的用户对象
     * @return 更新成功返回true，否则返回false
     */
    public static boolean updateUser(User user) {
        try {
            return UserDao.updateUser(user);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("更新用户信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteUser(int userId) {
        try {
            return UserDao.deleteUser(userId);
        } catch (SQLException e) {
            // 记录异常信息
            System.err.println("删除用户时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}