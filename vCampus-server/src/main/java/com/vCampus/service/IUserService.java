package com.vCampus.service;

import com.vCampus.entity.User;

public interface IUserService extends IBaseService<User, Integer> {
    
    // 注册结果枚举需要移到接口中或单独定义
    enum RegisterResult {
        SUCCESS("注册成功"),
        USERNAME_EXISTS("用户名已存在"),
        STUDENT_ID_EXISTS("学号已存在"),
        TEACHER_ID_EXISTS("教师编号已存在"),
        ADMIN_ID_EXISTS("管理员工号已存在"),
        VALIDATION_FAILED("数据验证失败"),
        DATABASE_ERROR("数据库错误");

        private final String message;

        RegisterResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
    
    // 注册方法
    RegisterResult register(User user);
    
    //登陆方法
    User login(String username, String password);
        
    User getByUsername(String username);
    
    boolean validateUser(String username, String password);
    
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
    
    boolean resetPassword(Integer userId, String newPassword);
    
    // 其他特定方法
    boolean isUsernameExists(String username);
}