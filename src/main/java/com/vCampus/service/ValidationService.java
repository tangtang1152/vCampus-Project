package com.vCampus.service;

import com.vCampus.entity.Student;
import com.vCampus.entity.User;
import com.vCampus.util.DBConstants;

/**
 * 数据验证服务类
 * 负责验证数据的完整性和长度限制
 */
public class ValidationService {
    
    /**
     * 验证用户数据长度
     */
    public static boolean validateUser(User user) {
        if (user == null) {
            return false;
        }
        
        // 验证用户名长度 (1-50字符)
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.out.println("错误: 用户名为空");
            return false;
        }
        if (user.getUsername().length() > DBConstants.USERNAME_MAX_LENGTH) {
            System.out.println("错误: 用户名超过50字符限制");
            return false;
        }
        
        // 验证密码长度 (1-255字符)
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.out.println("错误: 密码为空");
            return false;
        }
        if (user.getPassword().length() > DBConstants.PASSWORD_MAX_LENGTH) {
            System.out.println("错误: 密码超过255字符限制");
            return false;
        }
        
        // 验证角色长度 (1-20字符)
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            System.out.println("错误: 角色为空");
            return false;
        }
        if (user.getRole().length() > DBConstants.ROLE_MAX_LENGTH) {
            System.out.println("错误: 角色超过20字符限制");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证学生数据长度
     */
    public static boolean validateStudent(Student student) {
        if (student == null) {
            return false;
        }
        
        // 验证学生姓名长度 (1-50字符)
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            System.out.println("错误: 学生姓名为空");
            return false;
        }
        if (student.getStudentName().length() > DBConstants.STUDENT_NAME_MAX_LENGTH) {
            System.out.println("错误: 学生姓名超过50字符限制");
            return false;
        }
        
        // 验证班级名称长度 (0-50字符，允许为空)
        if (student.getClassName() != null && student.getClassName().length() > DBConstants.CLASS_NAME_MAX_LENGTH) {
            System.out.println("错误: 班级名称超过50字符限制");
            return false;
        }
        
        // 验证学号范围
        if (Integer.parseInt(student.getStudentId()) <= 0) {
            System.out.println("错误: 学号必须大于0");
            return false;
        }
        
        return true;
    }
    
    /**
     * 截断字符串以适应字段长度
     */
    public static String truncateString(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        System.out.println("警告: 字符串被截断，原长度: " + input.length() + ", 截断后: " + maxLength);
        return input.substring(0, maxLength);
    }
}