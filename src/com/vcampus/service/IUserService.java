package com.vcampus.service;

import com.vcampus.entity.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑操作
 */
public interface IUserService {
    
    /**
     * 用户注册
     * @param user 用户对象
     * @return 注册是否成功
     */
    boolean register(User user);
    
    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    User login(String username, String password);
    
    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    User getUserById(Integer userId);
    
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 是否更新成功
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Integer userId);
    
    /**
     * 检查用户角色
     * @param userId 用户ID
     * @param expectedRole 期望的角色
     * @return 是否符合期望角色
     */
    boolean checkUserRole(Integer userId, String expectedRole);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     * @throws SQLException 
     */
    boolean updateUserPassword(Integer userId, String newPassword) throws SQLException;
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 是否更新成功
     */
    boolean updateRole(Integer userId, String newRole);
    
    /**
     * 根据角色获取用户列表
     * @param role 角色
     * @return 用户列表
     */
    List<User> getUsersByRole(String role);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    int getTotalUserCount();
    
    /**
     * 根据角色获取用户数量
     * @param role 角色
     * @return 用户数量
     */
    int getUserCountByRole(String role);
}