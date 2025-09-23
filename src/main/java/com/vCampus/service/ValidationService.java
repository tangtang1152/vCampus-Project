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
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) return false;
        if (user.getUsername().length() > DBConstants.USERNAME_MAX_LENGTH) return false;
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) return false;
        if (user.getPassword().length() > DBConstants.PASSWORD_MAX_LENGTH) return false;
        if (user.getRole() == null || user.getRole().trim().isEmpty()) return false;
        if (user.getRole().length() > DBConstants.ROLE_MAX_LENGTH) return false;
        return true;
    }
    
    /**
     * 验证学生数据长度（学号为短文本，非纯数字）
     */
    public static boolean validateStudent(Student student) {
        if (student == null) return false;
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) return false;
        if (student.getStudentName().length() > DBConstants.STUDENT_NAME_MAX_LENGTH) return false;
        if (student.getClassName() != null && student.getClassName().length() > DBConstants.CLASS_NAME_MAX_LENGTH) return false;
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) return false;
        if (student.getStudentId().length() > DBConstants.STUDENT_ID_MAX_LENGTH) return false;
        return true;
    }
    
    /**
     * 验证教师数据长度
     */
    public static boolean validateTeacher(Teacher teacher) {
        if (teacher == null) return false;
        if (teacher.getTeacherName() == null || teacher.getTeacherName().trim().isEmpty()) return false;
        if (teacher.getTeacherName().length() > DBConstants.TEACHER_NAME_MAX_LENGTH) return false;
        if (teacher.getTechnical() == null || teacher.getTechnical().trim().isEmpty()) return false;
        if (teacher.getTechnical().length() > DBConstants.TECHNICAL_MAX_LENGTH) return false;
        if (teacher.getDepartmentId() == null || teacher.getDepartmentId().trim().isEmpty()) return false;
        if (teacher.getDepartmentId().length() > DBConstants.DEPARTMENT_ID_MAX_LENGTH) return false;
        if (teacher.getSex() == null || teacher.getSex().trim().isEmpty()) return false;
        if (!"男".equals(teacher.getSex()) && !"女".equals(teacher.getSex())) return false;
        if (teacher.getSex().length() > DBConstants.SEX_MAX_LENGTH) return false;
        return true;
    }

    /**
     * 验证管理员数据长度
     */
    public static boolean validateAdmin(Admin admin) {
        if (admin == null) return false;
        if (admin.getAdminId() == null || admin.getAdminId().trim().isEmpty()) return false;
        if (admin.getAdminId().length() > DBConstants.ADMIN_ID_MAX_LENGTH) return false;
        if (admin.getAdminName() == null || admin.getAdminName().trim().isEmpty()) return false;
        if (admin.getAdminName().length() > DBConstants.ADMIN_NAME_MAX_LENGTH) return false;
        return true;
    }
    
    /**
     * 截断字符串以适应字段长度
     */
    public static String truncateString(String input, int maxLength) {
        if (input == null) return null;
        if (input.length() <= maxLength) return input;
        return input.substring(0, maxLength);
    }
}