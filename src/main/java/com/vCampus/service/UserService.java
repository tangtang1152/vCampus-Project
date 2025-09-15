package com.vCampus.service;

import com.vCampus.dao.*;
import com.vCampus.entity.*;
import com.vCampus.util.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 用户服务类
 * 提供对用户数据的业务逻辑操作
 */
public class UserService {

    private static final IUserDao userDao = new UserDao();
    private static final IStudentDao studentDao = new StudentDao();
    private static final ITeacherDao teacherDao = new TeacherDao();
    private static final IAdminDao adminDao = new AdminDao();

    // 定义注册结果枚举 - 扩展原有的枚举
    public enum RegisterResult {
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
    
    /**
     * 用户登录验证
     */
    public static User login(String username, String password) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                boolean isValid = userDao.validateUser(username, password, conn);
                if (isValid) {
                    return userDao.findByUsername(username, conn);
                }
                return null;
            });
        } catch (RuntimeException e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用户注册 - 扩展支持多种角色
     */
    public static RegisterResult register(User user) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 根据用户类型进行不同的注册处理
                if (user instanceof Student) {
                    return registerStudent((Student) user, conn);
                } else if (user instanceof Teacher) {
                    return registerTeacher((Teacher) user, conn);
                } else if (user instanceof Admin) {
                    return registerAdmin((Admin) user, conn);
                }
                
                // 通用的用户验证和注册逻辑
                if (!ValidationService.validateUser(user)) {
                    return RegisterResult.VALIDATION_FAILED;
                }

                User existingUser = userDao.findByUsername(user.getUsername(), conn);
                if (existingUser != null) {
                    System.out.println("用户名已存在: " + user.getUsername());
                    return RegisterResult.USERNAME_EXISTS;
                }

                boolean success = userDao.insert(user, conn);
                return success ? RegisterResult.SUCCESS : RegisterResult.DATABASE_ERROR;
            });
        } catch (RuntimeException e) {
            System.err.println("注册失败: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
    }
    
    /**
     * 注册教师账户（事务内部使用）
     */
    private static RegisterResult registerTeacher(Teacher teacher, Connection conn) {
        try {
            System.out.println("=== 开始注册教师 ===");
            System.out.println("教师编号: " + teacher.getTeacherId());
            System.out.println("用户名: " + teacher.getUsername());
            System.out.println("姓名: " + teacher.getTeacherName());

            // 验证教师数据
            if (!ValidationService.validateTeacher(teacher)) {
                System.out.println("教师数据验证失败");
                return RegisterResult.VALIDATION_FAILED;
            }

            System.out.println("数据验证通过");

            // 检查用户名是否已存在
            User existingUser = userDao.findByUsername(teacher.getUsername(), conn);
            if (existingUser != null) {
                System.out.println("用户名已存在: " + teacher.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }

            System.out.println("用户名不存在，可以注册");

            // 检查教师编号是否已存在
            Teacher existingTeacher = teacherDao.findByTeacherId(teacher.getTeacherId(), conn);
            if (existingTeacher != null) {
                System.out.println("教师编号已存在: " + teacher.getTeacherId());
                return RegisterResult.TEACHER_ID_EXISTS;
            }

            System.out.println("教师编号不存在，可以注册");

            // 首先创建用户账户
            boolean userCreated = userDao.insert(teacher, conn);
            if (!userCreated) {
                System.out.println("创建用户账户失败");
                return RegisterResult.DATABASE_ERROR;
            }

            System.out.println("用户账户创建成功，用户ID: " + teacher.getUserId());

            // 然后创建教师信息
            boolean teacherCreated = teacherDao.insert(teacher, conn);
            if (teacherCreated) {
                System.out.println("教师信息创建成功");
                return RegisterResult.SUCCESS;
            } else {
                System.out.println("教师信息创建失败");
                return RegisterResult.DATABASE_ERROR;
            }

        } catch (SQLException e) {
            System.err.println("注册教师时数据库错误: " + e.getMessage());
            System.err.println("SQL状态: " + e.getSQLState());
            System.err.println("错误代码: " + e.getErrorCode());
            
            // 处理唯一性约束违反错误
            if (isUniqueConstraintViolation(e)) {
                return identifyTeacherConstraintType(e, teacher);
            }
            
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        } catch (Exception e) {
            System.err.println("注册教师时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
    }

    /**
     * 注册管理员账户（事务内部使用）
     */
    private static RegisterResult registerAdmin(Admin admin, Connection conn) {
        try {
            System.out.println("=== 开始注册管理员 ===");
            System.out.println("管理员工号: " + admin.getAdminId());
            System.out.println("用户名: " + admin.getUsername());
            System.out.println("姓名: " + admin.getAdminName());

            // 验证管理员数据
            if (!ValidationService.validateAdmin(admin)) {
                System.out.println("管理员数据验证失败");
                return RegisterResult.VALIDATION_FAILED;
            }

            System.out.println("数据验证通过");

            // 检查用户名是否已存在
            User existingUser = userDao.findByUsername(admin.getUsername(), conn);
            if (existingUser != null) {
                System.out.println("用户名已存在: " + admin.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }

            System.out.println("用户名不存在，可以注册");

            // 检查管理员工号是否已存在
            Admin existingAdmin = adminDao.findByAdminId(admin.getAdminId(), conn);
            if (existingAdmin != null) {
                System.out.println("管理员工号已存在: " + admin.getAdminId());
                return RegisterResult.ADMIN_ID_EXISTS;
            }

            System.out.println("管理员工号不存在，可以注册");

            // 首先创建用户账户
            boolean userCreated = userDao.insert(admin, conn);
            if (!userCreated) {
                System.out.println("创建用户账户失败");
                return RegisterResult.DATABASE_ERROR;
            }

            System.out.println("用户账户创建成功，用户ID: " + admin.getUserId());

            // 然后创建管理员信息
            boolean adminCreated = adminDao.insert(admin, conn);
            if (adminCreated) {
                System.out.println("管理员信息创建成功");
                return RegisterResult.SUCCESS;
            } else {
                System.out.println("管理员信息创建失败");
                return RegisterResult.DATABASE_ERROR;
            }

        } catch (SQLException e) {
            System.err.println("注册管理员时数据库错误: " + e.getMessage());
            System.err.println("SQL状态: " + e.getSQLState());
            System.err.println("错误代码: " + e.getErrorCode());
            
            // 处理唯一性约束违反错误
            if (isUniqueConstraintViolation(e)) {
                return identifyAdminConstraintType(e, admin);
            }
            
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        } catch (Exception e) {
            System.err.println("注册管理员时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
    }

    /**
     * 识别教师相关的约束类型
     */
    private static RegisterResult identifyTeacherConstraintType(SQLException e, Teacher teacher) {
        String message = e.getMessage();
        System.out.println("识别教师约束类型，错误信息: " + message);
        
        if (message != null) {
            // 检查是否是教师编号冲突
            if (message.contains("tbl_teacher") && 
                (message.contains("PrimaryKey") || message.contains("teacher_id"))) {
                System.out.println("识别为教师编号唯一性约束违反: " + teacher.getTeacherId());
                return RegisterResult.TEACHER_ID_EXISTS;
            }
            
            // 检查是否是用户名冲突
            if (message.contains("tbl_user") && 
                (message.contains("username") || message.contains("USERNAME"))) {
                System.out.println("识别为用户名唯一性约束违反: " + teacher.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }
        }
        
        System.out.println("无法识别的唯一性约束类型");
        return RegisterResult.DATABASE_ERROR;
    }

    /**
     * 识别管理员相关的约束类型
     */
    private static RegisterResult identifyAdminConstraintType(SQLException e, Admin admin) {
        String message = e.getMessage();
        System.out.println("识别管理员约束类型，错误信息: " + message);
        
        if (message != null) {
            // 检查是否是管理员工号冲突
            if (message.contains("tbl_admin") && 
                (message.contains("PrimaryKey") || message.contains("admin_id"))) {
                System.out.println("识别为管理员工号唯一性约束违反: " + admin.getAdminId());
                return RegisterResult.ADMIN_ID_EXISTS;
            }
            
            // 检查是否是用户名冲突
            if (message.contains("tbl_user") && 
                (message.contains("username") || message.contains("USERNAME"))) {
                System.out.println("识别为用户名唯一性约束违反: " + admin.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }
        }
        
        System.out.println("无法识别的唯一性约束类型");
        return RegisterResult.DATABASE_ERROR;
    }

    
    /**
     * 注册学生账户（事务内部使用）
     */
    private static RegisterResult registerStudent(Student student, Connection conn) {
        try {
            System.out.println("=== 开始注册学生 ===");
            System.out.println("学号: " + student.getStudentId());
            System.out.println("用户名: " + student.getUsername());
            System.out.println("姓名: " + student.getStudentName());

            // 验证学生数据
            if (!ValidationService.validateStudent(student)) {
                System.out.println("数据验证失败");
                return RegisterResult.VALIDATION_FAILED;
            }
            System.out.println("数据验证通过");
            
            // 检查用户名是否已存在
            System.out.println("检查用户名是否存在: " + student.getUsername());
            User existingUser = userDao.findByUsername(student.getUsername(), conn);
            if (existingUser != null) {
                System.out.println("用户名已存在: " + student.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }
            System.out.println("用户名不存在，可以注册");
            
            // 检查学号是否已存在
            System.out.println("检查学号是否存在: " + student.getStudentId());
            Student existingStudent = studentDao.findByStudentId(student.getStudentId(), conn);
            if (existingStudent != null) {
                System.out.println("学号已存在: " + student.getStudentId());
                return RegisterResult.STUDENT_ID_EXISTS;
            }
            System.out.println("学号不存在，可以注册");

            // 首先创建用户账户
            System.out.println("开始创建用户账户...");
            boolean userCreated = userDao.insert(student, conn);
            if (!userCreated) {
                System.out.println("创建用户账户失败");
                return RegisterResult.DATABASE_ERROR;
            }
            System.out.println("用户账户创建成功，用户ID: " + student.getUserId());

            // 然后创建学生信息
            System.out.println("开始创建学生信息...");
            boolean studentCreated = studentDao.insert(student, conn);
            if (studentCreated) {
                System.out.println("学生信息创建成功");
                return RegisterResult.SUCCESS;
            } else {
                System.out.println("学生信息创建失败");
                return RegisterResult.DATABASE_ERROR;
            }
            
        } catch (SQLException e) {
            System.err.println("注册学生时数据库错误: " + e.getMessage());
            System.err.println("SQL状态: " + e.getSQLState());
            System.err.println("错误代码: " + e.getErrorCode());
            
            // 关键修改：识别特定的唯一性约束违反错误
            if (isUniqueConstraintViolation(e)) {
                return identifyConstraintType(e, student);
            }
            
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        } catch (Exception e) {
            System.err.println("注册学生时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
    }
    
    /**
     * 判断是否为唯一性约束违反错误
     */
    private static boolean isUniqueConstraintViolation(SQLException e) {
        // UCanAccess/JDBC 错误代码和状态码判断
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
        String message = e.getMessage();
        
        // 常见的唯一性约束错误标识
        return "23000".equals(sqlState) || // 完整性约束违反
               errorCode == 15000 || // UCanAccess 特定错误代码
               (message != null && (
                   message.contains("violates uniqueness constraint") ||
                   message.contains("unique constraint") ||
                   message.contains("PrimaryKey") ||
                   message.contains("duplicate key")
               ));
    }

    /**
     * 识别具体的约束类型
     */
    private static RegisterResult identifyConstraintType(SQLException e, Student student) {
        String message = e.getMessage();
        
        System.out.println("识别约束类型，错误信息: " + message);
        
        if (message != null) {
            // 检查是否是学号冲突
            if (message.contains("tbl_student") && 
                (message.contains("PrimaryKey") || message.contains("student_id"))) {
                System.out.println("识别为学号唯一性约束违反: " + student.getStudentId());
                return RegisterResult.STUDENT_ID_EXISTS;
            }
            
            // 检查是否是用户名冲突
            if (message.contains("tbl_user") && 
                (message.contains("username") || message.contains("USERNAME"))) {
                System.out.println("识别为用户名校一性约束违反: " + student.getUsername());
                return RegisterResult.USERNAME_EXISTS;
            }
        }
        
        // 如果无法识别具体类型，返回通用的数据库错误
        System.out.println("无法识别的唯一性约束类型");
        return RegisterResult.DATABASE_ERROR;
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    public static User getUserById(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findById(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户名获取用户信息
     */
    public static User getUserByUsername(String username) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findByUsername(username, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新用户信息
     */
    public static boolean updateUser(User user) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.update(user, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除用户
     */
    public static boolean deleteUser(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.delete(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 为其他角色预留的方法
    public static boolean registerTeacher(User teacher) {
        // TODO: 实现教师注册逻辑
        return false;
    }

    public static boolean registerAdmin(User admin) {
        // TODO: 实现管理员注册逻辑
        return false;
    }
}