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
    
    // 其他常量...
}