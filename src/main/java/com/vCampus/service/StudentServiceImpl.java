package com.vCampus.service;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.StudentDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
public class StudentServiceImpl implements IStudentService {

    private static final IStudentDao studentDao = new StudentDaoImpl();
    private static final IUserDao userDao = new UserDaoImpl();
    
    /**
     * 根据学号获取学生信息
     */
    @Override
    public Student getStudentById(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByStudentId(studentId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户ID获取学生信息
     */
    @Override
    public Student getStudentByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByUserId(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Student> getAllStudents() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findAll(conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取所有学生失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 增加学生
     */
    @Override
    public boolean addStudent(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.insert(student, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("添加学生失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新学生信息
     */
    @Override
    public boolean updateStudent(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.update(student, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("更新学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateFullStudent(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 更新用户基本信息
                boolean userUpdated = userDao.update(student, conn);
                if (!userUpdated) {
                    System.out.println("更新用户信息失败");
                    return false;
                }
                
                // 更新学生特定信息
                boolean studentUpdated = studentDao.update(student, conn);
                if (!studentUpdated) {
                    System.out.println("更新学生信息失败");
                    // 如果学生信息更新失败，可以回滚用户更新
                    throw new RuntimeException("更新学生信息失败，事务已回滚");
                }
                
                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("完整更新学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteStudent(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 首先获取学生信息
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student == null) {
                    System.out.println("学生不存在，学号: " + studentId);
                    return false;
                }

                // 先删除学生信息
                boolean studentDeleted = studentDao.delete(studentId, conn);
                if (!studentDeleted) {
                    System.out.println("删除学生信息失败");
                    return false;
                }

                // 再删除用户账户
                boolean userDeleted = userDao.delete(student.getUserId(), conn);
                if (!userDeleted) {
                    System.out.println("删除用户信息失败");
                    // 如果用户删除失败，回滚学生删除操作
                    throw new RuntimeException("删除用户信息失败，事务已回滚");
                }

                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("删除学生失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteStudentInfoOnly(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.delete(studentId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("删除学生信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isStudentIdExists(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                studentDao.findByStudentId(studentId, conn) != null
            );
        } catch (RuntimeException e) {
            System.err.println("检查学号是否存在失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        try {
            return TransactionManager.<List<Student>>executeInTransaction(conn -> {
                List<Student> allStudents = studentDao.findAll(conn);
                return allStudents.stream()
                    .filter(student -> className.equals(student.getClassName()))
                    .collect(Collectors.toList());
            });
        } catch (RuntimeException e) {
            System.err.println("根据班级获取学生失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}