package com.vCampus.service;

import com.vCampus.dao.*;
import com.vCampus.entity.*;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 用户服务类
 * 提供对用户数据的业务逻辑操作
 */
public class UserServiceImpl 
extends AbstractBaseServiceImpl<User, Integer> implements IUserService {

    private static final IUserDao userDao = new UserDaoImpl();
    private static final IStudentDao studentDao = new StudentDaoImpl();
    private static final ITeacherDao teacherDao = new TeacherDaoImpl();
    private static final IAdminDao adminDao = new AdminDaoImpl();

    // 实现抽象方法
    @Override
    protected User doGetBySelfId(Integer userId, Connection conn) throws Exception {
        return userDao.findById(userId, conn);
    }

    @Override
    protected List<User> doGetAll(Connection conn) throws Exception {
        return userDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(User user, Connection conn) throws Exception {
        return userDao.insert(user, conn);
    }

    @Override
    protected boolean doUpdate(User user, Connection conn) throws Exception {
        return userDao.update(user, conn);
    }

    @Override
    protected boolean doDelete(Integer userId, Connection conn) throws Exception {
        return userDao.delete(userId, conn);
    }

    @Override
    protected boolean doExists(Integer userId, Connection conn) throws Exception {
        return userDao.findById(userId, conn) != null;
    }

    @Override
    public User login(String username, String password) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                boolean isValid = userDao.validateUser(username, password, conn);
                if (isValid) {
                    return userDao.findByUsername(username, conn);
                }
                return null;
            });
            /*
             * 验证成功：返回完整的 User 对象
               验证失败：返回 null
               */
        } catch (RuntimeException e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getByUsername(String username) {
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
	
    @Override
    public boolean validateUser(String username, String password) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.validateUser(username, password, conn)
            );
        } catch (Exception e) {
            handleException("验证用户失败", e);
            return false;
        }
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                userDao.findByUsername(username, conn) != null
            );
        } catch (RuntimeException e) {
            System.err.println("检查用户名是否存在失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                User user = userDao.findById(userId, conn);
                if (user != null && user.getPassword().equals(oldPassword)) {
                    user.setPassword(newPassword);
                    return userDao.update(user, conn);
                }
                return false;
            });
        } catch (RuntimeException e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean resetPassword(Integer userId, String newPassword) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                User user = userDao.findById(userId, conn);
                if (user != null) {
                    user.setPassword(newPassword);
                    return userDao.update(user, conn);
                }
                return false;
            });
        } catch (RuntimeException e) {
            System.err.println("重置密码失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public RegisterResult register(User user) {//调用了registerStudent registerTeacher registerAdmin
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
        } catch (Exception e) {
            handleException("注册失败", e);
            return RegisterResult.DATABASE_ERROR;
        }
    }
    //私有辅助方法保持不变
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
        } catch (Exception e) {
            System.err.println("注册管理员时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
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
        } catch (Exception e) {
            System.err.println("注册学生时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return RegisterResult.DATABASE_ERROR;
        }
    }

}