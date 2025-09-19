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
    private static volatile IChooseService chooseService;
    private static volatile ISubjectService subjectService;
    private static volatile IShopService shopService;
    private static volatile IProductService productService;
    
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

    public static IChooseService getChooseService() {
        if (chooseService == null) {
            synchronized (ServiceFactory.class) {
                if (chooseService == null) {
                    chooseService = new ChooseServiceImpl();
                }
            }
        }
        return chooseService;
    }

    public static ISubjectService getSubjectService() {
        if (subjectService == null) {
            synchronized (ServiceFactory.class) {
                if (subjectService == null) {
                    subjectService = new SubjectServiceImpl();
                }
            }
        }
        return subjectService;
    }

    public static IShopService getShopService() {
        if (shopService == null) {
            synchronized (ServiceFactory.class) {
                if (shopService == null) {
                    shopService = new ShopServiceImpl();
                }
            }
        }
        return shopService;
    }

    public static IProductService getProductService() {
        if (productService == null) {
            synchronized (ServiceFactory.class) {
                if (productService == null) {
                    productService = new ProductServiceImpl();
                }
            }
        }
        return productService;
    }
}