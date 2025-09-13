package com.vcampus.service;

import com.vcampus.dao.IUserDao;
import com.vcampus.dao.UserDaoImpl;
import com.vcampus.entity.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户服务实现类
 * 处理用户相关的业务逻辑
 */
public class UserServiceImpl implements IUserService {
    
    private IUserDao userDao;
    
    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }
    
    /**
     * 构造方法，允许注入不同的DAO实现（用于测试等场景）
     * @param userDao 用户DAO实现
     */
    public UserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            if (userDao.getUserByUsername(user.getUsername()) != null) {
                System.err.println("注册失败：用户名 '" + user.getUsername() + "' 已存在");
                return false;
            }
            
            // 验证角色合法性
            if (!isValidRole(user.getRole())) {
                System.err.println("注册失败：角色 '" + user.getRole() + "' 不合法");
                return false;
            }
            
            // 验证用户名和密码长度
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                System.err.println("注册失败：用户名不能为空");
                return false;
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                System.err.println("注册失败：密码不能为空");
                return false;
            }
            
            if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
                System.err.println("注册失败：用户名长度必须在3-20个字符之间");
                return false;
            }
            
            if (user.getPassword().length() < 6) {
                System.err.println("注册失败：密码长度不能少于6个字符");
                return false;
            }
            
            return userDao.addUser(user);
            
        } catch (SQLException e) {
            System.err.println("注册过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public User login(String username, String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                System.err.println("登录失败：用户名不能为空");
                return null;
            }
            
            if (password == null || password.trim().isEmpty()) {
                System.err.println("登录失败：密码不能为空");
                return null;
            }
            
            User user = userDao.validateUser(username, password);
            if (user != null) {
                System.out.println("用户登录成功: " + username);
            } else {
                System.err.println("登录失败：用户名或密码错误");
            }
            return user;
            
        } catch (SQLException e) {
            System.err.println("登录过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public User getUserById(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                System.err.println("获取用户信息失败：用户ID不合法");
                return null;
            }
            
            return userDao.getUserById(userId);
            
        } catch (SQLException e) {
            System.err.println("获取用户信息过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public User getUserByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                System.err.println("获取用户信息失败：用户名不能为空");
                return null;
            }
            
            return userDao.getUserByUsername(username);
            
        } catch (SQLException e) {
            System.err.println("获取用户信息过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<User> getAllUsers() {
        try {
            return userDao.getAllUsers();
            
        } catch (SQLException e) {
            System.err.println("获取所有用户过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateUser(User user) {
        try {
            if (user == null || user.getUserId() == null) {
                System.err.println("更新用户信息失败：用户对象或用户ID不能为空");
                return false;
            }
            
            // 检查用户是否存在
            User existingUser = userDao.getUserById(user.getUserId());
            if (existingUser == null) {
                System.err.println("更新用户信息失败：用户ID " + user.getUserId() + " 不存在");
                return false;
            }
            
            // 验证角色合法性
            if (user.getRole() != null && !isValidRole(user.getRole())) {
                System.err.println("更新用户信息失败：角色 '" + user.getRole() + "' 不合法");
                return false;
            }
            
            return userDao.updateUser(user);
            
        } catch (SQLException e) {
            System.err.println("更新用户信息过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteUser(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                System.err.println("删除用户失败：用户ID不合法");
                return false;
            }
            
            // 检查用户是否存在
            User existingUser = userDao.getUserById(userId);
            if (existingUser == null) {
                System.err.println("删除用户失败：用户ID " + userId + " 不存在");
                return false;
            }
            
            return userDao.deleteUser(userId);
            
        } catch (SQLException e) {
            System.err.println("删除用户过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean checkUserRole(Integer userId, String expectedRole) {
        try {
            if (userId == null || userId <= 0 || expectedRole == null) {
                return false;
            }
            
            User user = userDao.getUserById(userId);
            return user != null && expectedRole.equals(user.getRole());
            
        } catch (SQLException e) {
            System.err.println("检查用户角色过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updatePassword(Integer userId, String newPassword) {
        try {
            if (userId == null || userId <= 0) {
                System.err.println("更新密码失败：用户ID不合法");
                return false;
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                System.err.println("更新密码失败：新密码不能为空");
                return false;
            }
            
            if (newPassword.length() < 6) {
                System.err.println("更新密码失败：密码长度不能少于6个字符");
                return false;
            }
            
            // 检查用户是否存在
            User existingUser = userDao.getUserById(userId);
            if (existingUser == null) {
                System.err.println("更新密码失败：用户ID " + userId + " 不存在");
                return false;
            }
            
            return userDao.updateUserPassword(userId, newPassword);
            
        } catch (SQLException e) {
            System.err.println("更新密码过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateRole(Integer userId, String newRole) {
        try {
            if (userId == null || userId <= 0) {
                System.err.println("更新角色失败：用户ID不合法");
                return false;
            }
            
            if (!isValidRole(newRole)) {
                System.err.println("更新角色失败：角色 '" + newRole + "' 不合法");
                return false;
            }
            
            // 检查用户是否存在
            User existingUser = userDao.getUserById(userId);
            if (existingUser == null) {
                System.err.println("更新角色失败：用户ID " + userId + " 不存在");
                return false;
            }
            
            return userDao.updateUserRole(userId, newRole);
            
        } catch (SQLException e) {
            System.err.println("更新角色过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<User> getUsersByRole(String role) {
        try {
            if (!isValidRole(role)) {
                System.err.println("获取用户列表失败：角色 '" + role + "' 不合法");
                return null;
            }
            
            return userDao.getUsersByRole(role);
            
        } catch (SQLException e) {
            System.err.println("获取用户列表过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            return userDao.usernameExists(username);
            
        } catch (SQLException e) {
            System.err.println("检查用户名是否存在过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getTotalUserCount() {
        try {
            return userDao.getTotalUserCount();
            
        } catch (SQLException e) {
            System.err.println("获取用户总数过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public int getUserCountByRole(String role) {
        try {
            if (!isValidRole(role)) {
                System.err.println("获取用户数量失败：角色 '" + role + "' 不合法");
                return 0;
            }
            
            return userDao.getUserCountByRole(role);
            
        } catch (SQLException e) {
            System.err.println("获取用户数量过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 验证角色是否合法
     * @param role 角色
     * @return 是否合法
     */
    private boolean isValidRole(String role) {
        return "student".equals(role) || "teacher".equals(role) || "admin".equals(role);
    }
    
    /**
     * 搜索用户（根据用户名模糊搜索）
     * @param keyword 关键词
     * @return 用户列表
     */
    public List<User> searchUsers(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                System.err.println("搜索用户失败：关键词不能为空");
                return null;
            }
            
            return userDao.searchUsers(keyword);
            
        } catch (SQLException e) {
            System.err.println("搜索用户过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
