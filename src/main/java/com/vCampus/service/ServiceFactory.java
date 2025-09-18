package com.vCampus.service;

/**
 * 服务工厂类
 * 用于创建各种服务实例
 */
public class ServiceFactory {
    
    private static volatile IUserService userService;
    private static volatile IStudentService studentService;
    private static volatile ITeacherService teacherService;
    private static volatile IAdminService adminService;
    
    public static IUserService getUserService() {
        if (userService == null) {
            synchronized (ServiceFactory.class) {
                if (userService == null) {
                    userService = new UserServiceImpl();
                }
            }
        }
        return userService;
    }
    
    public static IStudentService getStudentService() {
        if (studentService == null) {
            synchronized (ServiceFactory.class) {
                if (studentService == null) {
                    studentService = new StudentServiceImpl();
                }
            }
        }
        return studentService;
    }
    
    public static ITeacherService getTeacherService() {
        if (teacherService == null) {
            synchronized (ServiceFactory.class) {
                if (teacherService == null) {
                    teacherService = new TeacherServiceImpl();
                }
            }
        }
        return teacherService;
    }
    
    public static IAdminService getAdminService() {
        if (adminService == null) {
            synchronized (ServiceFactory.class) {
                if (adminService == null) {
                    adminService = new AdminServiceImpl();
                }
            }
        }
        return adminService;
    }
}