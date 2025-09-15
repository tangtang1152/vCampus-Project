package com.vCampus.service;

import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
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
     * 验证教师数据长度
     */
    public static boolean validateTeacher(Teacher teacher) {
        if (teacher == null) {
            return false;
        }

        // 验证教师姓名长度 (1-50字符)
        if (teacher.getTeacherName() == null || teacher.getTeacherName().trim().isEmpty()) {
            System.out.println("错误: 教师姓名为空");
            return false;
        }
        if (teacher.getTeacherName().length() > DBConstants.TEACHER_NAME_MAX_LENGTH) {
            System.out.println("错误: 教师姓名超过50字符限制");
            return false;
        }

        // 验证职称长度 (1-20字符)
        if (teacher.getTechnical() == null || teacher.getTechnical().trim().isEmpty()) {
            System.out.println("错误: 职称为空");
            return false;
        }
        if (teacher.getTechnical().length() > DBConstants.TECHNICAL_MAX_LENGTH) {
            System.out.println("错误: 职称超过20字符限制");
            return false;
        }

        // 验证部门ID长度 (1-20字符)
        if (teacher.getDepartmentId() == null || teacher.getDepartmentId().trim().isEmpty()) {
            System.out.println("错误: 部门ID为空");
            return false;
        }
        if (teacher.getDepartmentId().length() > DBConstants.DEPARTMENT_ID_MAX_LENGTH) {
            System.out.println("错误: 部门ID超过20字符限制");
            return false;
        }

        // 验证性别 (男/女)
        if (teacher.getSex() == null || teacher.getSex().trim().isEmpty()) {
            System.out.println("错误: 性别为空");
            return false;
        }
        if (!teacher.getSex().equals("男") && !teacher.getSex().equals("女")) {
            System.out.println("错误: 性别必须为'男'或'女'");
            return false;
        }

        return true;
    }

    /**
     * 验证管理员数据长度
     */
    public static boolean validateAdmin(Admin admin) {
        if (admin == null) {
            return false;
        }

        // 验证管理员姓名长度 (1-50字符)
        if (admin.getAdminName() == null || admin.getAdminName().trim().isEmpty()) {
            System.out.println("错误: 管理员姓名为空");
            return false;
        }
        if (admin.getAdminName().length() > DBConstants.ADMIN_NAME_MAX_LENGTH) {
            System.out.println("错误: 管理员姓名超过50字符限制");
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