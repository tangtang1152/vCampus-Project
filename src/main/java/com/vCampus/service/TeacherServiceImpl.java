package com.vCampus.service;

import com.vCampus.dao.ITeacherDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.TeacherDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Teacher;
import com.vCampus.util.TransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师服务实现类
 */
public class TeacherServiceImpl implements ITeacherService {
    
    private final ITeacherDao teacherDao = new TeacherDaoImpl();
    private static final IUserDao userDao = new UserDaoImpl();

    @Override
    public Teacher getTeacherById(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.findByTeacherId(teacherId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Teacher getTeacherByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.findByUserId(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Teacher> getAllTeachers() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.findAll(conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取所有教师失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.insert(teacher, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("添加教师失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTeacher(Teacher teacher) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.update(teacher, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("更新教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateFullTeacher(Teacher teacher) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 更新用户基本信息
                boolean userUpdated = userDao.update(teacher, conn);
                if (!userUpdated) {
                    System.out.println("更新教师信息失败");
                    return false;
                }
                
                // 更新教师特定信息
                boolean teacherUpdated = teacherDao.update(teacher, conn);
                if (!teacherUpdated) {
                    System.out.println("更新教师信息失败");
                    // 如果教师信息更新失败，可以回滚用户更新
                    throw new RuntimeException("更新教师信息失败，事务已回滚");
                }
                
                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("完整更新教师信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTeacherInfoOnly(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.delete(teacherId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("删除教师失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteTeacher(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 首先获取教师信息
                Teacher teacher = teacherDao.findByTeacherId(teacherId, conn);
                if (teacher == null) {
                    System.out.println("教师不存在，教师编号: " + teacherId);
                    return false;
                }

                // 先删除教师信息
                boolean teacherDeleted = teacherDao.delete(teacherId, conn);
                if (!teacherDeleted) {
                    System.out.println("删除教师信息失败");
                    return false;
                }

                // 再删除用户账户
                boolean userDeleted = userDao.delete(teacher.getUserId(), conn);
                if (!userDeleted) {
                    System.out.println("删除用户信息失败");
                    // 如果用户删除失败，回滚教师删除操作
                    throw new RuntimeException("删除用户信息失败，事务已回滚");
                }

                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("删除教师失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isTeacherIdExists(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.findByTeacherId(teacherId, conn) != null
            );
        } catch (RuntimeException e) {
            System.err.println("检查教师编号是否存在失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Teacher> getTeachersByDepartment(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<Teacher> allTeachers = teacherDao.findAll(conn);
                return allTeachers.stream()
                    .filter(teacher -> departmentId.equals(teacher.getDepartmentId()))
                    .collect(Collectors.toList());
            });
        } catch (RuntimeException e) {
            System.err.println("根据部门获取教师失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}