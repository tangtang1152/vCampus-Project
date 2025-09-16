package com.vCampus.test;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.StudentDaoImpl;
import com.vCampus.entity.Student;
import com.vCampus.entity.User;
import com.vCampus.service.IStudentService;
import com.vCampus.service.StudentServiceImpl;
import com.vCampus.service.UserServiceImpl;
import com.vCampus.service.UserServiceImpl.RegisterResult;
import com.vCampus.util.TransactionManager;
import com.vCampus.service.IUserService;
import com.vCampus.view.RegisterController;


/**
 * 简单的Student增删改查测试
 * 直接运行即可测试
 */
public class SimpleStudentCRUDTest {
    
    private static final IStudentService studentService= new StudentServiceImpl();
    private static final IUserService userService= new UserServiceImpl();
    
    private static final String TEST_STUDENT_ID = "TEST2024001";
    private static final String TEST_USERNAME = "test_user_001";
    

    public static void main(String[] args) {
        System.out.println("🚀 开始Student CRUD测试...");
        

        
        try {
            // 1. 测试创建学生
            testCreateStudent();
            
            // 2. 测试查询学生
            testGetStudent();
            
            // 3. 测试更新学生
            testUpdateStudent();
            
            // 4. 测试删除学生
            testDeleteStudent();
            
            System.out.println("✅ 所有测试完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试创建学生
     */
    private static void testCreateStudent() {
        System.out.println("\n1. 测试创建学生...");
        
        try {
            // 先删除可能存在的测试数据
            studentService.deleteStudent(TEST_STUDENT_ID);
            
            // 创建测试学生
            Student student = new Student();
            student.setStudentId(TEST_STUDENT_ID);
            student.setStudentName("测试学生");
            student.setClassName("测试班级");
            student.setUsername(TEST_USERNAME);
            student.setPassword("test123");
            student.setRole("STUDENT");
            

            userService.register(student);
            
        } catch (Exception e) {
            System.err.println("❌ 创建学生异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试查询学生
     */
    private static void testGetStudent() {
        System.out.println("\n2. 测试查询学生...");
        
        try {
            Student student = studentService.getStudentById(TEST_STUDENT_ID);
            
            if (student != null) {
                System.out.println("✅ 查询学生成功:");
                System.out.println("   学号: " + student.getStudentId());
                System.out.println("   姓名: " + student.getStudentName());
                System.out.println("   班级: " + student.getClassName());
                System.out.println("   用户名: " + student.getUsername());
            } else {
                System.out.println("❌ 查询学生失败 - 学生不存在");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 查询学生异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试更新学生
     */
    private static void testUpdateStudent() {
        System.out.println("\n3. 测试更新学生...");
        
        try {
            // 先获取学生
            Student student = studentService.getStudentById(TEST_STUDENT_ID);
            
            if (student != null) {
                // 修改信息
                student.setStudentName("修改后的学生");
                student.setClassName("修改后的班级");
                
                boolean result = studentService.updateStudent(student);
                
                if (result) {
                    System.out.println("✅ 更新学生成功");
                    
                    // 验证更新结果
                    Student updatedStudent = studentService.getStudentById(TEST_STUDENT_ID);
                    System.out.println("   更新后姓名: " + updatedStudent.getStudentName());
                    System.out.println("   更新后班级: " + updatedStudent.getClassName());
                } else {
                    System.out.println("❌ 更新学生失败");
                }
            } else {
                System.out.println("❌ 更新失败 - 学生不存在");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 更新学生异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试删除学生
     */
    private static void testDeleteStudent() {
        System.out.println("\n4. 测试删除学生...");
        
        try {
            boolean result = studentService.deleteStudent(TEST_STUDENT_ID);
            
            if (result) {
                System.out.println("✅ 删除学生成功");
                
                // 验证删除结果
                Student student = studentService.getStudentById(TEST_STUDENT_ID);
                if (student == null) {
                    System.out.println("✅ 验证通过 - 学生已彻底删除");
                } else {
                    System.out.println("❌ 验证失败 - 学生仍然存在");
                }
            } else {
                System.out.println("❌ 删除学生失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 删除学生异常: " + e.getMessage());
        }
    }
}