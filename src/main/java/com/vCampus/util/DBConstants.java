package com.vCampus.util;

/**
 * 数据库配置常量
 * 定义字段长度限制，与数据库结构保持一致
 */
public class DBConstants {
    // tbl_user 表字段长度限制
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int PASSWORD_MAX_LENGTH = 255;
    public static final int ROLE_MAX_LENGTH = 20;

    // tbl_student 表字段长度限制
    public static final int STUDENT_NAME_MAX_LENGTH = 50;
    public static final int CLASS_NAME_MAX_LENGTH = 50;

    // tbl_teacher 表字段长度限制
    public static final int TEACHER_ID_MAX_LENGTH = 20;
    public static final int TEACHER_NAME_MAX_LENGTH = 50;
    public static final int SEX_MAX_LENGTH = 10;
    public static final int TECHNICAL_MAX_LENGTH = 20;
    public static final int DEPARTMENT_ID_MAX_LENGTH = 20;

    // tbl_admin 表字段长度限制
    public static final int ADMIN_ID_MAX_LENGTH = 20;
    public static final int ADMIN_NAME_MAX_LENGTH = 50;
    
 // 在 com.vCampus.util.DBConstants 中添加以下常量
    public static final int EMAIL_MAX_LENGTH = 100; // 根据实际需求调整
    public static final int IDCARD_MAX_LENGTH = 18; // 根据实际需求调整
    public static final int STATUS_MAX_LENGTH = 20; // 根据实际需求调整
}