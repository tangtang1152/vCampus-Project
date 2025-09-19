package com.vCampus.util;

import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.User;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入/导出（用户批量注册：学生/教师/管理员混合）
 * 简化方案：约定表头字段，自动识别角色并构造实体
 */
public final class ExcelUserIO {

    private ExcelUserIO() {}

    public static class ParsedRow {
        public User base;
        public Student student;   // 可空
        public Teacher teacher;   // 可空
        public Admin admin;       // 可空
        public String error;      // 解析错误消息（可空）
    }

    public static List<ParsedRow> importUsers(String xlsxPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(xlsxPath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sh = wb.getSheetAt(0);
            if (sh == null) return List.of();

            // 读取表头
            Row head = sh.getRow(0);
            Map<String, Integer> idx = new LinkedHashMap<>();
            for (int c = 0; head != null && c < head.getLastCellNum(); c++) {
                Cell cell = head.getCell(c);
                String name = cell == null ? null : cell.getStringCellValue();
                if (name != null && !name.isBlank()) idx.put(name.trim().toLowerCase(), c);
            }

            List<ParsedRow> out = new ArrayList<>();
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;
                ParsedRow pr = new ParsedRow();
                try {
                    String username = get(row, idx, "username");
                    String password = get(row, idx, "password");
                    String roles = get(row, idx, "role"); // 支持多角色：STUDENT,TEACHER,ADMIN

                    if (username == null || password == null || roles == null) {
                        pr.error = "缺少必填字段 username/password/role";
                    } else {
                        User u = new User();
                        u.setUsername(username);
                        u.setPassword(password);
                        // 允许多角色，直接写入文本；后续业务层会压缩为短码
                        u.setRole(roles);
                        pr.base = u;
                        // 可选角色明细
                        if (roles.toUpperCase().contains("STUDENT")) {
                            Student s = new Student();
                            s.setUsername(username); s.setPassword(password); s.setRole("STUDENT");
                            s.setStudentId(get(row, idx, "studentid"));
                            s.setStudentName(get(row, idx, "studentname"));
                            s.setClassName(get(row, idx, "classname"));
                            pr.student = s;
                        }
                        if (roles.toUpperCase().contains("TEACHER")) {
                            Teacher t = new Teacher();
                            t.setUsername(username); t.setPassword(password); t.setRole("TEACHER");
                            t.setTeacherId(get(row, idx, "teacherid"));
                            t.setTeacherName(get(row, idx, "teachername"));
                            t.setSex(get(row, idx, "sex"));
                            t.setTechnical(get(row, idx, "technical"));
                            t.setDepartmentId(get(row, idx, "departmentid"));
                            pr.teacher = t;
                        }
                        if (roles.toUpperCase().contains("ADMIN")) {
                            Admin a = new Admin();
                            a.setUsername(username); a.setPassword(password); a.setRole("ADMIN");
                            a.setAdminId(get(row, idx, "adminid"));
                            a.setAdminName(get(row, idx, "adminname"));
                            pr.admin = a;
                        }
                    }
                } catch (Exception ex) {
                    pr.error = ex.getMessage();
                }
                out.add(pr);
            }
            return out;
        }
    }

    private static String get(Row row, Map<String, Integer> idx, String key) {
        Integer c = idx.get(key);
        if (c == null) return null;
        Cell cell = row.getCell(c);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String v = cell.getStringCellValue();
        return v == null ? null : v.trim();
    }

    public static void exportTemplate(String xlsxPath) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("users");
            Row head = sh.createRow(0);
            String[] cols = {
                "username","password","role",
                "studentId","studentName","className",
                "teacherId","teacherName","sex","technical","departmentId",
                "adminId","adminName"
            };
            for (int i = 0; i < cols.length; i++) head.createCell(i).setCellValue(cols[i]);
            try (FileOutputStream fos = new FileOutputStream(xlsxPath)) { wb.write(fos); }
        }
    }
}


