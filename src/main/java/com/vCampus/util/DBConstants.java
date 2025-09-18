package com.vCampus.util;

/**
 * 数据库配置常量
 * 定义字段长度限制，与数据库结构保持一致
 */
public class DBConstants {
    // tbl_user 表字段长度限制
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final int PASSWORD_MAX_LENGTH = 255;
    public static final int ROLE_MAX_LENGTH = 10;

    // tbl_student 表字段长度限制
<<<<<<< HEAD
    public static final int STUDENT_NAME_MAX_LENGTH = 10;
    public static final int CLASS_NAME_MAX_LENGTH = 20;

=======
    public static final int STUDENT_NAME_MAX_LENGTH = 50;
    public static final int CLASS_NAME_MAX_LENGTH = 50;
	public static final int STUDENT_ID_MAX_LENGTH = 20;
	
>>>>>>> refs/heads/feature/course-selection-finalllll
    // tbl_teacher 表字段长度限制
    public static final int TEACHER_ID_MAX_LENGTH = 8;
    public static final int TEACHER_NAME_MAX_LENGTH = 10;
    public static final int SEX_MAX_LENGTH = 2;
    public static final int TECHNICAL_MAX_LENGTH = 10;
    public static final int DEPARTMENT_ID_MAX_LENGTH = 4;

    // tbl_admin 表字段长度限制
    public static final int ADMIN_ID_MAX_LENGTH = 20;
<<<<<<< HEAD
    public static final int ADMIN_NAME_MAX_LENGTH = 255;

    // 图书馆规则
    public static final int MAX_BORROW_CONCURRENT = 5; // 同时借出上限
    public static final double DAILY_FINE = 1.0;       // 逾期每日罚金（元）
=======
    public static final int ADMIN_NAME_MAX_LENGTH = 50;
    
    // department 表字段长度限制
	public static final int DEPARTMENT_NAME_MAX_LENGTH = 50;
	public static final int DEPARTMENT_LOCATION_MAX_LENGTH = 100;

	// tbl_course 表字段长度限制
	public static final int COURSE_ID_MAX_LENGTH = 20;
	public static final int COURSE_NAME_MAX_LENGTH = 100;
	public static final int COURSE_CREDIT_MAX_LENGTH = 10;
	public static final int COURSE_TEACHER_ID_MAX_LENGTH = 20;

	// tbl_subject 表字段长度限制
	public static final int SUBJECT_NAME_MAX_LENGTH = 50;
	public static final int SUBJECT_ID_MAX_LENGTH = 20;

>>>>>>> refs/heads/feature/course-selection-finalllll
}