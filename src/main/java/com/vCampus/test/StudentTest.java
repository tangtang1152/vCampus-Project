package com.vCampus.test;

import com.vCampus.entity.Student;
import com.vCampus.service.StudentService;
import com.vCampus.service.UserService;

/**
 * 学生类测试
 * 用于测试Student类及相关服务的功能
 */
public class StudentTest {
    
    /**
     * 主方法，运行所有测试
     */
    public static void main(String[] args) {
        System.out.println("========== 开始学生类测试 ==========");
        
        // 测试1：创建学生对象
        testStudentCreation();
        
        // 测试2：注册学生账户
        testRegisterStudent();
        
        // 测试3：从数据库查询学生
        testGetStudent();
        
        // 测试4：更新学生信息
        testUpdateStudent();
        
        // 测试5：删除学生
        testDeleteStudent();
        
        System.out.println("========== 学生类测试结束 ==========");
    }
    
    /**
     * 测试学生对象创建
     */
    private static void testStudentCreation() {
        System.out.println("\n--- 测试1: 学生对象创建 ---");
        
        // 使用默认构造函数创建学生
        Student student1 = new Student();
        student1.setUserId(1);
        student1.setUsername("zhangsan");
        student1.setPassword("password123");
        student1.setRole("student");
        student1.setStudentId(1001);
        student1.setStudentName("张三");
        student1.setClassName("计算机科学与技术1班");
        
        System.out.println("学生1: " + student1);
        
        // 使用带参数构造函数创建学生
        Student student2 = new Student(2, "lisi", "password456", "student",
                                      1002, "李四", "软件工程2班");
        
        System.out.println("学生2: " + student2);
    }
    
    /**
     * 测试注册学生账户
     */
    private static void testRegisterStudent() {
        System.out.println("\n--- 测试2: 注册学生账户 ---");
        
        // 创建测试学生
        Student student = new Student(0, "wangwu", "password789", "student",
                                     1003, "王五", "网络工程3班");
        
        // 注册学生账户
        boolean result = StudentService.registerStudent(student);
        
        System.out.println("注册学生结果: " + (result ? "成功" : "失败"));
        
        if (result) {
            System.out.println("生成的学生用户ID: " + student.getUserId());
        }
    }
    
    /**
     * 测试从数据库查询学生
     */
    private static void testGetStudent() {
        System.out.println("\n--- 测试3: 从数据库查询学生 ---");
        
        // 查询学生
        Student student = StudentService.getStudentById(1003);
        
        if (student != null) {
            System.out.println("查询到的学生: " + student);
        } else {
            System.out.println("未找到学号为1003的学生");
        }
    }
    
    /**
     * 测试更新学生信息
     */
    private static void testUpdateStudent() {
        System.out.println("\n--- 测试4: 更新学生信息 ---");
        
        // 查询学生
        Student student = StudentService.getStudentById(1003);
        
        if (student != null) {
            // 修改学生信息
            student.setClassName("更新后的班级");
            student.setPassword("newpassword123");
            
            // 更新学生信息
            boolean result = StudentService.updateStudent(student);
            
            System.out.println("更新学生结果: " + (result ? "成功" : "失败"));
            
            // 验证更新结果
            Student updatedStudent = StudentService.getStudentById(1003);
            System.out.println("更新后的学生: " + updatedStudent);
        } else {
            System.out.println("未找到学号为1003的学生，无法更新");
        }
    }
    
    /**
     * 测试删除学生
     */
    private static void testDeleteStudent() {
        System.out.println("\n--- 测试5: 删除学生 ---");
        
        // 删除学生
        boolean result = StudentService.deleteStudent(1003);
        
        System.out.println("删除学生结果: " + (result ? "成功" : "失败"));
        
        // 验证删除结果
        Student student = StudentService.getStudentById(1003);
        if (student == null) {
            System.out.println("学生删除成功，已无法查询到");
        } else {
            System.out.println("学生删除失败，仍然可以查询到: " + student);
        }
    }
}