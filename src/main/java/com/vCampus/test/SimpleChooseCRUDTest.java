package com.vCampus.test;

import com.vCampus.entity.Choose;
import com.vCampus.entity.Student;
import com.vCampus.entity.Subject;
import com.vCampus.service.ChooseServiceImpl;
import com.vCampus.service.IChooseService;
import com.vCampus.service.StudentServiceImpl;
import com.vCampus.service.SubjectServiceImpl;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ISubjectService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 选课服务测试类
 * 测试选课、退选、时间冲突等功能
 */
public class SimpleChooseCRUDTest {
    
    private static final IChooseService chooseService = new ChooseServiceImpl();
    private static final ISubjectService subjectService = new SubjectServiceImpl();
    private static final IStudentService studentService = new StudentServiceImpl();
    
    // 测试数据（与提供的课程数据匹配）
    private static final String TEST_STUDENT_ID = "09023433"; // 学生ID
    private static final String TEST_SUBJECT_ID = "S001"; // 计算机组成原理
    private static final String TEST_SUBJECT_ID_2 = "S002"; // 冲突课程ID
    private static final String TEST_SUBJECT_ID_3 = "S003"; // 非冲突课程ID
    private static String testChooseId; // 存储创建的选课记录ID
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    public static void main(String[] args) {
        System.out.println("🚀 开始选课服务测试...");
        
        try {
            // 0. 准备工作 - 确保测试学生和课程存在
            prepareTestData();
            
            // 1. 测试选课功能（基础功能）
            testChooseSubject();
            
            // 2. 测试查询选课状态
            testCheckSubjectChosen();
            
            // 3. 测试获取学生已选课程
            testGetStudentSubjects();
            
            // 4. 测试获取课程选课记录
            testGetSubjectChooses();
            
            // 5. 测试获取选课详情
            testGetChooseDetail();
            
            // 6. 测试验证选课记录
            testValidateChoose();
            
            // 7. 测试退选功能
            testDropSubject();
            
            // 8. 测试重复选课（边界情况）
            testDuplicateChoose();
            
            // 9. 测试课程容量限制
            testCourseCapacity();
            
            // 10. 新增：测试时间冲突检测
            testTimeConflict();
            
            // 11. 新增：测试选课时间有效期
            testCourseSelectionPeriod();
            
            // 12. 新增：测试退选时间有效期
            testDropCoursePeriod();
            
            System.out.println("✅ 所有选课服务测试完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 准备测试数据 - 确保测试学生和课程存在
     */
    private static void prepareTestData() {
        System.out.println("\n0. 准备测试数据...");
        
        // 检查测试学生是否存在
        Student student = studentService.getBySelfId(TEST_STUDENT_ID);
        if (student == null) {
            System.out.println("⚠️  测试学生不存在，请先创建学号为 " + TEST_STUDENT_ID + " 的学生");
        } else {
            System.out.println("✅ 测试学生存在: " + student.getStudentName());
        }
        
        // 检查测试课程1（S001-计算机组成原理）是否存在
        Subject subject = subjectService.getSubjectById(TEST_SUBJECT_ID);
        if (subject == null) {
            System.out.println("⚠️  测试课程不存在，请先创建课程ID为 " + TEST_SUBJECT_ID + " 的课程");
            System.out.println("   提示: 课程信息应为[S001, 计算机组成原理, 2025/9/1, 20, 5, T001, 1-16, ALL, 周一第 1-2 节, ...]");
        } else {
            System.out.println("✅ 测试课程存在: " + subject.getSubjectName() + 
                               ", 容量: " + subject.getSubjectNum() +
                               ", 时间: " + subject.getClassTime() +
                               ", 周次: " + subject.getWeekRange());
        }
        
        // 检查冲突课程S002是否存在（用于时间冲突测试）
        Subject conflictSubject = subjectService.getSubjectById(TEST_SUBJECT_ID_2);
        if (conflictSubject == null) {
            System.out.println("⚠️  冲突测试课程不存在，请先创建课程ID为 " + TEST_SUBJECT_ID_2 + " 的课程");
            System.out.println("   提示: 应设置与S001相同时间[周一第 1-2 节]以测试冲突");
        } else {
            System.out.println("✅ 冲突测试课程存在: " + conflictSubject.getSubjectName() +
                               ", 时间: " + conflictSubject.getClassTime());
        }
        
        // 检查非冲突课程S003是否存在
        Subject nonConflictSubject = subjectService.getSubjectById(TEST_SUBJECT_ID_3);
        if (nonConflictSubject == null) {
            System.out.println("⚠️  非冲突测试课程不存在，请先创建课程ID为 " + TEST_SUBJECT_ID_3 + " 的课程");
            System.out.println("   提示: 应设置与S001不同时间[如周二第 1-2 节]");
        } else {
            System.out.println("✅ 非冲突测试课程存在: " + nonConflictSubject.getSubjectName() +
                               ", 时间: " + nonConflictSubject.getClassTime());
        }
        
        // 清理可能存在的旧选课记录
        cleanupOldChooseRecords();
    }
    
    /**
     * 清理旧的选课记录
     */
    private static void cleanupOldChooseRecords() {
        System.out.println("清理旧的选课记录...");
        try {
            // 清理测试学生的所有选课记录
            List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID);
            if (chooses != null) {
                for (Choose choose : chooses) {
                    if (TEST_STUDENT_ID.equals(choose.getStudentId())) {
                        chooseService.dropSubject(choose.getSelectid());
                        System.out.println("已清理旧选课记录: " + choose.getSelectid());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("清理选课记录时出现异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试选课功能
     */
    private static void testChooseSubject() {
        System.out.println("\n1. 测试选课功能...");
        
        try {
            // 执行选课操作
            boolean result = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            
            if (result) {
                System.out.println("✅ 选课成功");
                
                // 获取选课记录ID用于后续测试
                List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID);
                if (chooses != null && !chooses.isEmpty()) {
                    for (Choose choose : chooses) {
                        if (TEST_STUDENT_ID.equals(choose.getStudentId())) {
                            testChooseId = choose.getSelectid();
                            System.out.println("   选课记录ID: " + testChooseId);
                            break;
                        }
                    }
                }
            } else {
                System.out.println("❌ 选课失败（可能因时间冲突或选课时间过期）");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 选课测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试检查选课状态
     */
    private static void testCheckSubjectChosen() {
        System.out.println("\n2. 测试检查选课状态...");
        
        try {
            boolean isChosen = chooseService.isSubjectChosen(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            
            if (isChosen) {
                System.out.println("✅ 学生已选该课程");
            } else {
                System.out.println("❌ 学生未选该课程");
            }
            
            // 测试未选的课程
            boolean notChosen = chooseService.isSubjectChosen(TEST_STUDENT_ID, TEST_SUBJECT_ID_2);
            if (!notChosen) {
                System.out.println("✅ 学生未选其他课程 - 验证正确");
            } else {
                System.out.println("❌ 学生选课状态异常");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 检查选课状态异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试获取学生已选课程
     */
    private static void testGetStudentSubjects() {
        System.out.println("\n3. 测试获取学生已选课程...");
        
        try {
            List<Subject> subjects = chooseService.getStudentSubjects(TEST_STUDENT_ID);
            
            if (subjects != null && !subjects.isEmpty()) {
                System.out.println("✅ 获取学生已选课程成功，共 " + subjects.size() + " 门课程:");
                for (Subject sub : subjects) {
                    System.out.println("   - " + sub.getSubjectId() + ": " + sub.getSubjectName() + 
                                     " (时间: " + sub.getClassTime() + ", 学分: " + sub.getCredit() + ")");
                }
            } else {
                System.out.println("❌ 学生没有已选课程或查询失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 获取学生已选课程异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试获取课程选课记录
     */
    private static void testGetSubjectChooses() {
        System.out.println("\n4. 测试获取课程选课记录...");
        
        try {
            List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID);
            
            if (chooses != null && !chooses.isEmpty()) {
                System.out.println("✅ 获取课程选课记录成功，共 " + chooses.size() + " 条记录:");
                for (Choose choose : chooses) {
                    System.out.println("   - 选课ID: " + choose.getSelectid() + 
                                     ", 学生ID: " + choose.getStudentId());
                }
            } else {
                System.out.println("❌ 课程没有选课记录或查询失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 获取课程选课记录异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试获取选课详情
     */
    private static void testGetChooseDetail() {
        System.out.println("\n5. 测试获取选课详情...");
        
        try {
            if (testChooseId != null) {
                Choose choose = chooseService.getChooseDetail(testChooseId);
                
                if (choose != null) {
                    System.out.println("✅ 获取选课详情成功:");
                    System.out.println("   选课ID: " + choose.getSelectid());
                    System.out.println("   学生ID: " + choose.getStudentId());
                    System.out.println("   课程ID: " + choose.getSubjectId());
                    
                    // 获取课程详情（包含时间信息）
                    Subject subject = subjectService.getSubjectById(choose.getSubjectId());
                    if (subject != null) {
                        System.out.println("   课程名称: " + subject.getSubjectName());
                        System.out.println("   上课时间: " + subject.getClassTime());
                        System.out.println("   周次范围: " + subject.getWeekRange());
                        System.out.println("   周类型: " + subject.getWeekType());
                    }
                } else {
                    System.out.println("❌ 获取选课详情失败 - 记录不存在");
                }
            } else {
                System.out.println("⚠️  跳过选课详情测试 - 没有有效的选课ID");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 获取选课详情异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试验证选课记录
     */
    private static void testValidateChoose() {
        System.out.println("\n6. 测试验证选课记录...");
        
        try {
            if (testChooseId != null) {
                Choose choose = chooseService.getChooseDetail(testChooseId);
                if (choose != null) {
                    boolean isValid = chooseService.validateChoose(choose);
                    System.out.println("选课记录验证结果: " + (isValid ? "✅ 有效" : "❌ 无效"));
                    
                    // 测试无效的选课记录
                    Choose invalidChoose = new Choose();
                    invalidChoose.setSelectid("");
                    invalidChoose.setStudentId("");
                    invalidChoose.setSubjectId("");
                    boolean isInvalid = chooseService.validateChoose(invalidChoose);
                    System.out.println("无效选课记录验证结果: " + (!isInvalid ? "✅ 正确拒绝" : "❌ 错误接受"));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 验证选课记录异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试退选功能
     */
    private static void testDropSubject() {
        System.out.println("\n7. 测试退选功能...");
        
        try {
            if (testChooseId != null) {
                boolean result = chooseService.dropSubject(testChooseId);
                
                if (result) {
                    System.out.println("✅ 退选成功");
                    
                    // 验证退选结果
                    boolean isStillChosen = chooseService.isSubjectChosen(TEST_STUDENT_ID, TEST_SUBJECT_ID);
                    if (!isStillChosen) {
                        System.out.println("✅ 验证通过 - 学生已成功退选");
                    } else {
                        System.out.println("❌ 验证失败 - 学生仍然显示已选该课程");
                    }
                } else {
                    System.out.println("❌ 退选失败（可能已超过退选时间）");
                }
            } else {
                System.out.println("⚠️  跳减退选测试 - 没有有效的选课ID");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 退选测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试重复选课（边界情况测试）
     */
    private static void testDuplicateChoose() {
        System.out.println("\n8. 测试重复选课（边界情况）...");
        
        try {
            // 先确保学生未选该课程
            cleanupOldChooseRecords();
            
            // 第一次选课
            System.out.println("第一次选课尝试...");
            boolean firstResult = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            System.out.println("第一次选课结果: " + (firstResult ? "✅ 成功" : "❌ 失败"));
            
            // 第二次选课（应该失败）
            System.out.println("第二次选课尝试（重复选课）...");
            boolean secondResult = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            System.out.println("第二次选课结果: " + (secondResult ? "❌ 成功（异常）" : "✅ 失败（预期行为）"));
            
            if (!secondResult) {
                System.out.println("✅ 重复选课被正确阻止");
            } else {
                System.out.println("❌ 重复选课未被阻止");
            }
            
            // 清理测试数据
            cleanupOldChooseRecords();
            
        } catch (Exception e) {
            System.err.println("❌ 重复选课测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试课程容量限制
     */
    private static void testCourseCapacity() {
        System.out.println("\n9. 测试课程容量限制...");
        
        try {
            // 获取课程信息
            Subject subject = subjectService.getSubjectById(TEST_SUBJECT_ID);
            if (subject != null) {
                System.out.println("课程容量: " + subject.getSubjectNum());
                
                // 获取当前选课人数
                List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID);
                int currentEnrollment = chooses != null ? chooses.size() : 0;
                System.out.println("当前选课人数: " + currentEnrollment);
                
                if (currentEnrollment >= subject.getSubjectNum()) {
                    // 尝试选课（应失败）
                    boolean result = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
                    System.out.println("课程已满时选课结果: " + (result ? "❌ 成功（异常）" : "✅ 失败（预期行为）"));
                } else {
                    System.out.println("⚠️  课程尚未满，无法测试容量限制（当前人数: " + currentEnrollment + "/" + subject.getSubjectNum() + "）");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 课程容量测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 新增测试：时间冲突检测
     */
    private static void testTimeConflict() {
        System.out.println("\n10. 测试时间冲突检测...");
        
        try {
            // 先选S001（周一第1-2节）
            cleanupOldChooseRecords();
            boolean chooseS001 = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            if (!chooseS001) {
                System.out.println("⚠️  无法选中基础课程，跳过冲突测试");
                return;
            }
            System.out.println("已成功选中基础课程: " + TEST_SUBJECT_ID);
            
            // 尝试选冲突课程S002（周一第1-2节）
            System.out.println("尝试选择冲突课程" + TEST_SUBJECT_ID_2 + "...");
            boolean chooseConflict = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID_2);
            System.out.println("冲突课程选课结果: " + (chooseConflict ? "❌ 成功（异常）" : "✅ 失败（预期行为）"));
            
            // 尝试选非冲突课程S003（不同时间）
            System.out.println("尝试选择非冲突课程" + TEST_SUBJECT_ID_3 + "...");
            boolean chooseNonConflict = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID_3);
            System.out.println("非冲突课程选课结果: " + (chooseNonConflict ? "✅ 成功（预期行为）" : "❌ 失败（异常）"));
            
            // 清理测试数据
            cleanupOldChooseRecords();
            if (chooseNonConflict) {
                List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID_3);
                for (Choose choose : chooses) {
                    if (TEST_STUDENT_ID.equals(choose.getStudentId())) {
                        chooseService.dropSubject(choose.getSelectid());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ 时间冲突测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 新增测试：选课时间有效期（S001开课日期2025/9/1，应在之前7天内可选中）
     */
    private static void testCourseSelectionPeriod() {
        System.out.println("\n11. 测试选课时间有效期...");
        
        try {
            Subject subject = subjectService.getSubjectById(TEST_SUBJECT_ID);
            if (subject == null) {
                System.out.println("⚠️  课程不存在，跳过测试");
                return;
            }
            
            Date startDate = subject.getSubjectDate();
            Date sevenDaysBefore = new Date(startDate.getTime() - (7 * 24 * 60 * 60 * 1000L));
            Date now = new Date();
            
            System.out.println("课程开课日期: " + DATE_FORMAT.format(startDate));
            System.out.println("选课有效开始时间: " + DATE_FORMAT.format(sevenDaysBefore));
            System.out.println("当前时间: " + DATE_FORMAT.format(now));
            
            // 判断当前是否在选课有效期内
            boolean isWithinPeriod = now.after(sevenDaysBefore) && now.before(startDate);
            System.out.println("当前是否在选课有效期内: " + (isWithinPeriod ? "是" : "否"));
            
            // 实际测试选课
            cleanupOldChooseRecords();
            boolean result = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            
            if (isWithinPeriod && result) {
                System.out.println("✅ 符合预期: 有效期内选课成功");
            } else if (!isWithinPeriod && !result) {
                System.out.println("✅ 符合预期: 有效期外选课失败");
            } else if (isWithinPeriod && !result) {
                System.out.println("❌ 不符合预期: 有效期内选课失败");
            } else {
                System.out.println("❌ 不符合预期: 有效期外选课成功");
            }
            
            cleanupOldChooseRecords();
            
        } catch (Exception e) {
            System.err.println("❌ 选课时间测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 新增测试：退选时间有效期（S001开课日期2025/9/1，开课前可退选）
     */
    private static void testDropCoursePeriod() {
        System.out.println("\n12. 测试退选时间有效期...");
        
        try {
            Subject subject = subjectService.getSubjectById(TEST_SUBJECT_ID);
            if (subject == null) {
                System.out.println("⚠️  课程不存在，跳过测试");
                return;
            }
            
            Date startDate = subject.getSubjectDate();
            Date now = new Date();
            
            System.out.println("课程开课日期: " + DATE_FORMAT.format(startDate));
            System.out.println("当前时间: " + DATE_FORMAT.format(now));
            System.out.println("当前是否在退选有效期内（开课前）: " + (now.before(startDate) ? "是" : "否"));
            
            // 先选中课程
            cleanupOldChooseRecords();
            boolean chooseSuccess = chooseService.chooseSubject(TEST_STUDENT_ID, TEST_SUBJECT_ID);
            if (!chooseSuccess) {
                System.out.println("⚠️  选课失败，无法测试退选");
                return;
            }
            
            // 获取选课ID
            List<Choose> chooses = chooseService.getSubjectChooses(TEST_SUBJECT_ID);
            String tempChooseId = null;
            for (Choose choose : chooses) {
                if (TEST_STUDENT_ID.equals(choose.getStudentId())) {
                    tempChooseId = choose.getSelectid();
                    break;
                }
            }
            
            if (tempChooseId == null) {
                System.out.println("⚠️  未找到选课记录，无法测试退选");
                return;
            }
            
            // 测试退选
            boolean dropSuccess = chooseService.dropSubject(tempChooseId);
            
            if (now.before(startDate) && dropSuccess) {
                System.out.println("✅ 符合预期: 退选有效期内退选成功");
            } else if (!now.before(startDate) && !dropSuccess) {
                System.out.println("✅ 符合预期: 退选有效期外退选失败");
            } else if (now.before(startDate) && !dropSuccess) {
                System.out.println("❌ 不符合预期: 退选有效期内退选失败");
            } else {
                System.out.println("❌ 不符合预期: 退选有效期外退选成功");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 退选时间测试异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanupOldChooseRecords();
        }
    }
}