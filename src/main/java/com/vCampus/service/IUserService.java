package com.vCampus.service;

import com.vCampus.entity.User;
import java.util.List;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作
 */
public interface IUserService {
    
    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，如果登录失败返回null
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     * @param user 要注册的用户对象
     * @return 注册结果枚举
     */
    UserServiceImpl.RegisterResult register(User user);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回null
     */
    User getUserById(Integer userId);
    
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     */
    User getUserByUsername(String username);
    
    /**
     * 更新用户信息
     * @param user 要更新的用户对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteUser(Integer userId);
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 验证用户名是否已存在
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean isUsernameExists(String username);
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功返回true，失败返回false
     */
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 重置成功返回true，失败返回false
     */
    boolean resetPassword(Integer userId, String newPassword);
}