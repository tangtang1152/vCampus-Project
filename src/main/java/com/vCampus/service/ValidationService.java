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
            System.out.println("错误: 用户对象为空");
            return false;
        }

        // 验证用户名长度 (1-50字符)
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.out.println("错误: 用户名为空");
            return false;
        }
        if (user.getUsername().length() > DBConstants.USERNAME_MAX_LENGTH) {
            System.out.println("错误: 用户名超过" + DBConstants.USERNAME_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证密码长度 (1-255字符)
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.out.println("错误: 密码为空");
            return false;
        }
        if (user.getPassword().length() > DBConstants.PASSWORD_MAX_LENGTH) {
            System.out.println("错误: 密码超过" + DBConstants.PASSWORD_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证角色长度 (1-20字符)
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            System.out.println("错误: 角色为空");
            return false;
        }
        // 建议增加角色值的合法性检查，例如只允许 "STUDENT", "TEACHER", "ADMIN"
        if (!"STUDENT".equals(user.getRole()) && !"TEACHER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            System.out.println("错误: 角色不合法，必须是 STUDENT, TEACHER 或 ADMIN");
            return false;
        }
        if (user.getRole().length() > DBConstants.ROLE_MAX_LENGTH) {
            System.out.println("错误: 角色超过" + DBConstants.ROLE_MAX_LENGTH + "字符限制");
            return false;
        }

        return true;
    }

    /**
     * 验证学生数据长度
     */
    public static boolean validateStudent(Student student) {
        if (student == null) {
            System.out.println("错误: 学生对象为空");
            return false;
        }

        // 验证学号 (非空，纯数字，大于0)
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            System.out.println("错误: 学号为空");
            return false;
        }
        try {
            // 修复：安全地解析学号为数字，避免 NumberFormatException
            int studentIdNum = Integer.parseInt(student.getStudentId().trim());
            if (studentIdNum <= 0) {
                System.out.println("错误: 学号必须是大于0的数字");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("错误: 学号必须是有效的数字格式");
            return false;
        }
        // 可以添加学号长度限制 if (student.getStudentId().length() > DBConstants.STUDENT_ID_MAX_LENGTH) {...}

        // 验证学生姓名长度 (1-50字符)
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            System.out.println("错误: 学生姓名为空");
            return false;
        }
        if (student.getStudentName().length() > DBConstants.STUDENT_NAME_MAX_LENGTH) {
            System.out.println("错误: 学生姓名超过" + DBConstants.STUDENT_NAME_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证班级名称长度 (0-50字符，允许为空)
        if (student.getClassName() != null && student.getClassName().length() > DBConstants.CLASS_NAME_MAX_LENGTH) {
            System.out.println("错误: 班级名称超过" + DBConstants.CLASS_NAME_MAX_LENGTH + "字符限制");
            return false;
        }

        // 修复：添加对其他字段的验证
        // 性别 (男/女)
        if (student.getSex() == null || student.getSex().trim().isEmpty()) {
            System.out.println("错误: 性别为空");
            return false;
        }
        if (!"男".equals(student.getSex()) && !"女".equals(student.getSex())) {
            System.out.println("错误: 性别必须为'男'或'女'");
            return false;
        }
        if (student.getSex().length() > DBConstants.SEX_MAX_LENGTH) {
            System.out.println("错误: 性别超过" + DBConstants.SEX_MAX_LENGTH + "字符限制");
            return false;
        }

        // 邮箱 (允许为空，但不为空时需要基本格式和长度)
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            if (student.getEmail().length() > DBConstants.EMAIL_MAX_LENGTH) {
                System.out.println("错误: 邮箱超过" + DBConstants.EMAIL_MAX_LENGTH + "字符限制");
                return false;
            }
            // 简单的邮箱格式验证
            if (!student.getEmail().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
                System.out.println("错误: 邮箱格式不正确");
                return false;
            }
        }

        // 身份证号 (允许为空，但不为空时需要长度验证)
        if (student.getIdCard() != null && !student.getIdCard().trim().isEmpty()) {
            if (student.getIdCard().length() > DBConstants.IDCARD_MAX_LENGTH) {
                System.out.println("错误: 身份证号超过" + DBConstants.IDCARD_MAX_LENGTH + "字符限制");
                return false;
            }
            // 简单的身份证号长度验证 (15或18位)
            if (!(student.getIdCard().length() == 15 || student.getIdCard().length() == 18)) {
                System.out.println("错误: 身份证号长度不正确 (应为15或18位)");
                return false;
            }
        }

        // 学籍状态 (非空，合法值)
        if (student.getStatus() == null || student.getStatus().trim().isEmpty()) {
            System.out.println("错误: 学籍状态为空");
            return false;
        }
        if (!"正常".equals(student.getStatus()) && !"休学".equals(student.getStatus()) &&
                !"退学".equals(student.getStatus()) && !"毕业".equals(student.getStatus())) {
            System.out.println("错误: 学籍状态不合法，必须是 '正常', '休学', '退学' 或 '毕业'");
            return false;
        }
        if (student.getStatus().length() > DBConstants.STATUS_MAX_LENGTH) {
            System.out.println("错误: 学籍状态超过" + DBConstants.STATUS_MAX_LENGTH + "字符限制");
            return false;
        }

        // 入学日期 (可以为空，但不为空时需要是有效日期) - JavaFX DatePicker 会自动处理格式，这里主要确保非空
        // if (student.getEnrollDate() == null) { /* 可以根据业务需求决定是否必须填写 */ }

        return true;
    }

    /**
     * 验证教师数据长度
     */
    public static boolean validateTeacher(Teacher teacher) {
        if (teacher == null) {
            System.out.println("错误: 教师对象为空");
            return false;
        }

        // 验证教师编号 (非空)
        if (teacher.getTeacherId() == null || teacher.getTeacherId().trim().isEmpty()) {
            System.out.println("错误: 教师编号为空");
            return false;
        }
        if (teacher.getTeacherId().length() > DBConstants.TEACHER_ID_MAX_LENGTH) {
            System.out.println("错误: 教师编号超过" + DBConstants.TEACHER_ID_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证教师姓名长度 (1-50字符)
        if (teacher.getTeacherName() == null || teacher.getTeacherName().trim().isEmpty()) {
            System.out.println("错误: 教师姓名为空");
            return false;
        }
        if (teacher.getTeacherName().length() > DBConstants.TEACHER_NAME_MAX_LENGTH) {
            System.out.println("错误: 教师姓名超过" + DBConstants.TEACHER_NAME_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证职称长度 (1-20字符)
        if (teacher.getTechnical() == null || teacher.getTechnical().trim().isEmpty()) {
            System.out.println("错误: 职称为空");
            return false;
        }
        if (teacher.getTechnical().length() > DBConstants.TECHNICAL_MAX_LENGTH) {
            System.out.println("错误: 职称超过" + DBConstants.TECHNICAL_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证部门ID长度 (1-20字符)
        if (teacher.getDepartmentId() == null || teacher.getDepartmentId().trim().isEmpty()) {
            System.out.println("错误: 部门ID为空");
            return false;
        }
        if (teacher.getDepartmentId().length() > DBConstants.DEPARTMENT_ID_MAX_LENGTH) {
            System.out.println("错误: 部门ID超过" + DBConstants.DEPARTMENT_ID_MAX_LENGTH + "字符限制");
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
        if (teacher.getSex().length() > DBConstants.SEX_MAX_LENGTH) {
            System.out.println("错误: 性别超过" + DBConstants.SEX_MAX_LENGTH + "字符限制");
            return false;
        }

        return true;
    }

    /**
     * 验证管理员数据长度
     */
    public static boolean validateAdmin(Admin admin) {
        if (admin == null) {
            System.out.println("错误: 管理员对象为空");
            return false;
        }

        // 验证管理员工号 (非空)
        if (admin.getAdminId() == null || admin.getAdminId().trim().isEmpty()) {
            System.out.println("错误: 管理员工号为空");
            return false;
        }
        if (admin.getAdminId().length() > DBConstants.ADMIN_ID_MAX_LENGTH) {
            System.out.println("错误: 管理员工号超过" + DBConstants.ADMIN_ID_MAX_LENGTH + "字符限制");
            return false;
        }

        // 验证管理员姓名长度 (1-50字符)
        if (admin.getAdminName() == null || admin.getAdminName().trim().isEmpty()) {
            System.out.println("错误: 管理员姓名为空");
            return false;
        }
        if (admin.getAdminName().length() > DBConstants.ADMIN_NAME_MAX_LENGTH) {
            System.out.println("错误: 管理员姓名超过" + DBConstants.ADMIN_NAME_MAX_LENGTH + "字符限制");
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
        System.out.println("警告: 字符串被截断，原长度: " + input.length() + ", 截断后: " + maxLength + ", 原始字符串: " + input);
        return input.substring(0, maxLength);
    }
}