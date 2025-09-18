package com.vCampus.test;

import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.User;
import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ITeacherService;
import com.vCampus.service.IAdminService;
import com.vCampus.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        System.out.println("========== 数据库连接与注册冒烟测试 ==========");
        try {
            Connection conn = DBUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ 数据库连接成功！URL: " + conn.getMetaData().getURL());
                conn.close();
            } else {
                System.out.println("❌ 数据库连接失败");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接异常: " + e.getMessage());
            e.printStackTrace();
        }

        IUserService userService = ServiceFactory.getUserService();
        IStudentService studentService = ServiceFactory.getStudentService();
        ITeacherService teacherService = ServiceFactory.getTeacherService();
        IAdminService adminService = ServiceFactory.getAdminService();
        // ==== 学生：新增 ====
        Student stu = new Student();
        stu.setUsername("stu_smoke");
        stu.setPassword("123456");
        stu.setRole("STUDENT");
        stu.setStudentId("S900001");
        stu.setStudentName("小明");
        stu.setClassName("CS2401");
        System.out.println("注册学生 => " + userService.register(stu));

        // ==== 教师：新增 ====
        Teacher tch = new Teacher();
        tch.setUsername("tch_smoke");
        tch.setPassword("123456");
        tch.setRole("TEACHER");
        tch.setTeacherId("T900001");
        tch.setTeacherName("李老师");
        tch.setSex("男");
        tch.setTechnical("讲师");
        tch.setDepartmentId("D01");
        System.out.println("注册教师 => " + userService.register(tch));

        // ==== 管理员：新增 ====
        Admin adm = new Admin();
        adm.setUsername("adm_smoke");
        adm.setPassword("123456");
        adm.setRole("ADMIN");
        adm.setAdminId("A900001");
        adm.setAdminName("管理员A");
        System.out.println("注册管理员 => " + userService.register(adm));

        // ==== 查询：按用户名/自编号 ====
        User stuU = userService.getByUsername("stu_smoke");
        Teacher tchQ = teacherService.getByUserId(userService.getByUsername("tch_smoke").getUserId());
        Admin admQ = adminService.getByUserId(userService.getByUsername("adm_smoke").getUserId());
        System.out.println("查询用户(stu_smoke) => userId=" + (stuU==null?null:stuU.getUserId()));
        System.out.println("查询教师(T900001) => " + (tchQ==null?null:tchQ.getTeacherName()));
        System.out.println("查询管理员(A900001) => " + (admQ==null?null:admQ.getAdminName()));

        // ==== 更新：学生班级、教师职称、管理员姓名 ====
        Student stuFull = studentService.getByUserId(stuU.getUserId());
        stuFull.setClassName("CS2402");
        System.out.println("更新学生 => " + studentService.update(stuFull));

        tchQ.setTechnical("副教授");
        System.out.println("更新教师 => " + teacherService.update(tchQ));

        admQ.setAdminName("管理员B");
        System.out.println("更新管理员 => " + adminService.update(admQ));

        // ==== 验证更新结果 ====
        System.out.println("学生新班级 => " + studentService.getByUserId(stuU.getUserId()).getClassName());
        System.out.println("教师新职称 => " + teacherService.getByUserId(tchQ.getUserId()).getTechnical());
        System.out.println("管理员新姓名 => " + adminService.getByUserId(admQ.getUserId()).getAdminName());

        // ==== 登录校验 ====
        System.out.println("学生登录 => " + (userService.login("stu_smoke", "123456") != null));
        System.out.println("教师登录 => " + (userService.login("tch_smoke", "123456") != null));
        System.out.println("管理员登录 => " + (userService.login("adm_smoke", "123456") != null));

        // ==== 删除：按自编号删除，并级联删除 user ====
        System.out.println("删除教师(T900001) => " + teacherService.delete("T900001"));
        System.out.println("删除管理员(A900001) => " + adminService.delete("A900001"));
        System.out.println("删除学生(S900001) => " + studentService.delete("S900001"));

        System.out.println("========== 冒烟测试结束 ==========");
    }
}