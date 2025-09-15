package com.vCampus.service;

import java.sql.SQLException;
import java.util.List;
import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.StudentDao;
import com.vCampus.entity.Student;

/**
 * 学生业务逻辑层 - 处理学生相关的业务规则和逻辑
 */
public class StudentServiceImpl implements IStudentService {
    private IStudentDao studentDao;
    
    public StudentServiceImpl() {
        this.studentDao = new StudentDao();
    }
    
    /**
     * 添加学生（包含业务验证）
     */
    public boolean addStudent(Student student) {
        try {
            // 1. 数据验证
            if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
                throw new IllegalArgumentException("学生姓名不能为空");
            }
            
            if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
                throw new IllegalArgumentException("学号不能为空");
            }
            
            // 添加长度验证
            if (student.getStudentId().length() > 10) {
                throw new IllegalArgumentException("学号长度不能超过10个字符");
            }
            
            if (studentDao.isStudentIdExists(student.getStudentId())) {
                throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已存在");
            }
            
            // 2. 设置默认值（如果需要）
            if (student.getStatus() == null) {
                student.setStatus("正常"); // 默认学籍状态
            }
            
            // 3. 调用DAO层插入数据
            return studentDao.insertStudent(student);
            
        } catch (SQLException e) {
            System.err.println("数据库错误: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("数据验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据学号查询学生
     */
    public Student getStudentById(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                throw new IllegalArgumentException("学号不能为空");
            }
            
            return studentDao.findStudentById(studentId);
        } catch (SQLException e) {
            System.err.println("查询学生失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取所有学生列表
     */
    public List<Student> getAllStudents() {
        try {
            return studentDao.findAllStudents();
        } catch (SQLException e) {
            System.err.println("查询所有学生失败: " + e.getMessage());
            return List.of(); // 返回空列表
        }
    }
    
    /**
     * 更新学生信息
     */
    public boolean updateStudent(Student student) {
        try {
            if (student.getStudentId() == null) {
                throw new IllegalArgumentException("学号不能为空");
            }
            
            // 检查学生是否存在
            if (!studentDao.isStudentIdExists(student.getStudentId())) {
                throw new IllegalArgumentException("学号 " + student.getStudentId() + " 不存在");
            }
            
            return studentDao.updateStudent(student);
        } catch (SQLException e) {
            System.err.println("更新学生失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除学生
     */
    public boolean deleteStudent(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                throw new IllegalArgumentException("学号不能为空");
            }
            
            return studentDao.deleteStudent(studentId);
        } catch (SQLException e) {
            System.err.println("删除学生失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 按班级查询学生
     */
    public List<Student> getStudentsByClass(String classId) {
        try {
            if (classId == null || classId.trim().isEmpty()) {
                throw new IllegalArgumentException("班级不能为空");
            }
            
            return studentDao.findStudentsByClass(classId);
        } catch (SQLException e) {
            System.err.println("按班级查询失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 按学籍状态查询学生
     */
    public List<Student> getStudentsByStatus(String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("状态不能为空");
            }
            
            return studentDao.findStudentsByStatus(status);
        } catch (SQLException e) {
            System.err.println("按状态查询失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 检查学号是否存在
     */
    public boolean isStudentIdExists(String studentId) {
        try {
            return studentDao.isStudentIdExists(studentId);
        } catch (SQLException e) {
            System.err.println("检查学号存在失败: " + e.getMessage());
            return false;
        }
    }
    
    
 // 以下是需要新增的实现方法

	/*
	 * @Override public Student getStudentByUserId(int userId) throws Exception {
	 * try { // 这里需要根据你的数据库设计来实现 // 假设你的Student表中有userId字段 return
	 * studentDao.findStudentByUserId(userId); } catch (SQLException e) { throw new
	 * Exception("根据用户ID查询学生失败: " + e.getMessage()); } }
	 */

    @Override
    public boolean validateStudentStatus(String studentId, String requiredStatus) throws Exception {
        try {
            Student student = studentDao.findStudentById(studentId);
            if (student == null) {
                throw new Exception("学生不存在: " + studentId);
            }
            return requiredStatus.equals(student.getStatus());
        } catch (SQLException e) {
            throw new Exception("验证学生状态失败: " + e.getMessage());
        }
    }

    @Override
    public int getStudentBorrowLimit(String studentId) throws Exception {
        try {
            Student student = studentDao.findStudentById(studentId);
            if (student == null) {
                throw new Exception("学生不存在: " + studentId);
            }
            
            // 根据学生状态设置借阅限制
            switch (student.getStatus()) {
                case "正常":
                    return 5; // 正常学生可借5本
                case "毕业班":
                    return 3; // 毕业班学生可借3本
                case "休学":
                    return 1; // 休学学生可借1本
                case "退学":
                    return 0; // 退学学生不可借书
                default:
                    return 2; // 其他状态默认2本
            }
        } catch (SQLException e) {
            throw new Exception("获取借阅限制失败: " + e.getMessage());
        }
    }

    @Override
    public String getStudentStatus(String studentId) throws Exception {
        try {
            Student student = studentDao.findStudentById(studentId);
            if (student == null) {
                throw new Exception("学生不存在: " + studentId);
            }
            return student.getStatus();
        } catch (SQLException e) {
            throw new Exception("获取学生状态失败: " + e.getMessage());
        }
    }

    
    @Override
    public boolean changeStudentStatus(String studentId, String newStatus, 
                                     String reason, String operator) throws Exception {
        try {
            // 1. 验证学生存在
            if (!studentDao.isStudentIdExists(studentId)) {
                throw new Exception("学生不存在: " + studentId);
            }
            
            // 2. 权限验证（这里需要接入你们的权限系统）
            // if (!hasPermission(operator, "change_student_status")) {
            //     throw new Exception("操作员无权限修改学生状态");
            // }
            
            // 3. 状态变更验证（可选）
            // if (!isValidStatusTransition(studentId, newStatus)) {
            //     throw new Exception("无效的状态变更");
            // }
            
            // 4. 更新状态
            Student student = studentDao.findStudentById(studentId);
            student.setStatus(newStatus);
            
            // 5. 记录变更日志（如果需要）
            // logStatusChange(studentId, student.getStatus(), newStatus, reason, operator);
            
            return studentDao.updateStudent(student);
            
        } catch (SQLException e) {
            throw new Exception("修改学生状态失败: " + e.getMessage());
        }
    }
    
    
    // 商店模块相关的方法实现
    @Override
    public Student getStudentByUserId(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return null;
            }
            return studentDao.findStudentByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public int getStudentOrderCount(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                return 0;
            }
            return studentDao.getStudentOrderCount(studentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public double getStudentTotalSpending(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                return 0.0;
            }
            return studentDao.getStudentTotalSpending(studentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    @Override
    public boolean isStudentActive(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                return false;
            }
            return studentDao.isStudentActive(studentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateStudentStatus(String studentId, String status) {
        try {
            if (studentId == null || studentId.trim().isEmpty() || status == null) {
                return false;
            }
            return studentDao.updateStudentStatus(studentId, status);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
  
}