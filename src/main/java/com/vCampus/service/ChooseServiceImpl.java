package com.vCampus.service;

import com.vCampus.dao.IChooseDao;
import com.vCampus.dao.ISubjectDao;
import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.ChooseDaoImpl;
import com.vCampus.dao.SubjectDaoImpl;
import com.vCampus.dao.StudentDaoImpl;
import com.vCampus.entity.Choose;
import com.vCampus.entity.Subject;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 选课服务实现类
 */
public class ChooseServiceImpl extends AbstractBaseServiceImpl<Choose, String> implements IChooseService {

    private static final IChooseDao chooseDao = new ChooseDaoImpl();
    private static final ISubjectDao subjectDao = new SubjectDaoImpl();
    private static final IStudentDao studentDao = new StudentDaoImpl();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // 实现抽象方法（略，保持不变）
    @Override
    protected Choose doGetBySelfId(String selectid, Connection conn) throws Exception {
        return chooseDao.findById(selectid, conn);
    }

    @Override
    protected List<Choose> doGetAll(Connection conn) throws Exception {
        return chooseDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Choose choose, Connection conn) throws Exception {
        if (!validateChoose(choose)) {
            System.out.println("选课信息验证失败");
            return false;
        }
        return chooseDao.insert(choose, conn);
    }

    @Override
    protected boolean doUpdate(Choose choose, Connection conn) throws Exception {
        if (!validateChoose(choose)) {
            System.out.println("选课信息验证失败");
            return false;
        }
        return chooseDao.update(choose, conn);
    }

    @Override
    protected boolean doDelete(String selectid, Connection conn) throws Exception {
        return chooseDao.delete(selectid, conn);
    }

    @Override
    protected boolean doExists(String selectid, Connection conn) throws Exception {
        return chooseDao.findById(selectid, conn) != null;
    }

    @Override
    public boolean chooseSubject(String studentId, String subjectId) {
        // 正常学生选课，不做“选课有效期”检查（按你的需求），仍保留容量/重复/时间冲突检查
        return doChooseSubject(studentId, subjectId, false, true); // 不忽略时间冲突，忽略选课有效期
    }

    @Override
    public boolean adminAssistChooseSubject(String studentId, String subjectId, boolean ignoreTimeConflict) {
        // 管理员代选：忽略选课有效期，时间冲突是否忽略由参数控制
        return doChooseSubject(studentId, subjectId, ignoreTimeConflict, true);
    }

    // 提供带原因的选课接口，便于 UI 精确提示
    public ServiceResult chooseSubjectWithReason(String studentId, String subjectId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 1. 验证学生存在性
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student == null) {
                    return ServiceResult.fail("学生不存在");
                }
                // 2. 课程存在
                Subject subject = subjectDao.findById(subjectId, conn);
                if (subject == null) {
                    return ServiceResult.fail("课程不存在");
                }
                // 3. 重复选
                Choose existingChoose = chooseDao.findByStudentAndSubject(studentId, subjectId, conn);
                if (existingChoose != null) {
                    return ServiceResult.fail("已选过");
                }
                // 4. 名额
                if (!subjectDao.decreaseSlotIfAvailable(subjectId, conn)) {
                    return ServiceResult.fail("名额已满");
                }
                // 5. 时间冲突
                if (hasTimeConflictWithinConn(studentId, subject, conn)) {
                    // 回滚名额
                    subjectDao.increaseSlot(subjectId, conn);
                    return ServiceResult.fail("时间冲突");
                }
                // 6. 插入记录
                Choose choose = new Choose();
                choose.setSelectid(generateSelectId());
                choose.setStudentId(studentId);
                choose.setSubjectId(subjectId);
                boolean success = chooseDao.insert(choose, conn);
                if (!success) {
                    subjectDao.increaseSlot(subjectId, conn);
                    return ServiceResult.fail("插入失败");
                }
                return ServiceResult.ok("选课成功");
            });
        } catch (Exception e) {
            return ServiceResult.fail("服务器错误");
        }
    }

    /**
     * 核心选课逻辑的私有方法，包含是否忽略时间冲突和选课有效期的参数
     */
    private boolean doChooseSubject(String studentId, String subjectId, boolean ignoreTimeConflict, boolean ignoreCourseSelectionPeriod) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 1. 验证学生存在性
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student == null) {
                    System.out.println("选课失败: 学生不存在，学号: " + studentId);
                    return false;
                }

                // 2. 验证课程存在性及有效性
                Subject subject = subjectDao.findById(subjectId, conn);
                if (subject == null) {
                    System.out.println("选课失败: 课程不存在，课程ID: " + subjectId);
                    return false;
                }
                // 新增：验证课程时间信息完整性
                if (!isSubjectTimeValid(subject)) {
                    System.out.println("选课失败: 课程时间信息不完整，无法选课，课程ID: " + subjectId);
                    return false;
                }

                // 3. 检查是否已选该课程
                Choose existingChoose = chooseDao.findByStudentAndSubject(studentId, subjectId, conn);
                if (existingChoose != null) {
                    System.out.println("选课失败: 学生已选该课程，学号: " + studentId + ", 课程ID: " + subjectId);
                    return false;
                }

                // 4. 并发容量控制
                if (!subjectDao.decreaseSlotIfAvailable(subjectId, conn)) {
                    System.out.println("选课失败: 课程已满，课程ID: " + subjectId);
                    return false;
                }

                // 5. 有效期（按参数）
                if (!ignoreCourseSelectionPeriod && !isWithinCourseSelectionPeriod(subject)) {
                    System.out.println("选课失败: 已超过选课时间范围，课程ID: " + subjectId);
                    return false;
                }

                // 6. 检查时间冲突（复用当前连接）
                if (!ignoreTimeConflict && hasTimeConflictWithinConn(studentId, subject, conn)) {
                    System.out.println("选课失败: 选课时间冲突，课程ID: " + subjectId);
                    // 回滚名额
                    subjectDao.increaseSlot(subjectId, conn);
                    return false;
                }
                
                // 7. 创建选课记录
                Choose choose = new Choose();
                choose.setSelectid(generateSelectId());
                choose.setStudentId(studentId);
                choose.setSubjectId(subjectId);

                // 8. 插入选课记录
                boolean success = chooseDao.insert(choose, conn);
                if (!success) {
                    // 插入失败，回滚名额
                    subjectDao.increaseSlot(subjectId, conn);
                    return false;
                }
                System.out.println("选课成功，选课ID: " + choose.getSelectid());
                return true;
            });
        } catch (Exception e) {
            handleException("选课操作失败", e);
            return false;
        }
    }

    @Override
    public boolean dropSubject(String selectid) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 1. 验证选课记录存在
                Choose choose = chooseDao.findById(selectid, conn);
                if (choose == null) {
                    System.out.println("退选失败: 选课记录不存在，选课ID: " + selectid);
                    return false;
                }

                // 2. 删除选课记录并回补课程名额
                boolean deleted = chooseDao.delete(selectid, conn);
                if (!deleted) {
                    System.out.println("退选失败: 删除记录失败，选课ID: " + selectid);
                    return false;
                }
                // 回补名额（忽略失败不应发生，但仍返回 false 以便上层提示）
                boolean slotOk = subjectDao.increaseSlot(choose.getSubjectId(), conn);
                if (!slotOk) {
                    System.err.println("退选异常: 回补名额失败，课程ID: " + choose.getSubjectId());
                    return false;
                }
                System.out.println("退选成功并已回补名额，选课ID: " + selectid);
                return true;
            });
        } catch (Exception e) {
            handleException("退选操作失败", e);
            return false;
        }
    }

    @Override
    public List<Subject> getStudentSubjects(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 获取学生的所有选课记录
                List<Choose> chooses = chooseDao.findByStudentId(studentId, conn);
                
                // 获取对应的课程信息
                return chooses.stream()
                    .map(choose -> {
                        try {
                            return subjectDao.findById(choose.getSubjectId(), conn);
                        } catch (Exception e) {
                            System.err.println("获取课程信息失败: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(subject -> subject != null)
                    .toList();
            });
        } catch (Exception e) {
            handleException("获取学生已选课程失败", e);
            return List.of();
        }
    }

    @Override
    public List<Choose> getSubjectChooses(String subjectId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                chooseDao.findBySubjectId(subjectId, conn)
            );
        } catch (Exception e) {
            handleException("获取课程选课记录失败", e);
            return List.of();
        }
    }

    @Override
    public boolean isSubjectChosen(String studentId, String subjectId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Choose choose = chooseDao.findByStudentAndSubject(studentId, subjectId, conn);
                return choose != null;
            });
        } catch (Exception e) {
            handleException("检查选课状态失败", e);
            return false;
        }
    }

    // 新增的接口方法实现
    @Override
    public Choose findByStudentAndSubject(String studentId, String subjectId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                chooseDao.findByStudentAndSubject(studentId, subjectId, conn)
            );
        } catch (Exception e) {
            handleException("根据学生和课程ID查找选课记录失败", e);
            return null;
        }
    }
    
    @Override
    public Choose getChooseDetail(String selectid) {
        return getBySelfId(selectid);
    }

    @Override
    public boolean validateChoose(Choose choose) {
        if (choose.getSelectid() == null || choose.getSelectid().trim().isEmpty()) {
            System.out.println("选课ID为空");
            return false;
        }
        if (choose.getStudentId() == null || choose.getStudentId().trim().isEmpty()) {
            System.out.println("学生ID为空");
            return false;
        }
        if (choose.getSubjectId() == null || choose.getSubjectId().trim().isEmpty()) {
            System.out.println("课程ID为空");
            return false;
        }
        return true;
    }

    private String generateSelectId() {
        return "SELECT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private boolean isSubjectTimeValid(Subject subject) {
        return subject.getWeekRange() != null && !subject.getWeekRange().trim().isEmpty()
                && subject.getWeekType() != null && !subject.getWeekType().trim().isEmpty()
                && subject.getClassTime() != null && !subject.getClassTime().trim().isEmpty()
                && subject.getClassroom() != null && !subject.getClassroom().trim().isEmpty();
    }

    private boolean hasTimeConflictWithinConn(String studentId, Subject newSubject, Connection conn) throws Exception {
        List<Subject> selectedSubjects = getStudentSubjectsWithinConn(studentId, conn);
        String newClassTime = newSubject.getClassTime();
        String newWeekRange = newSubject.getWeekRange();
        String newWeekType = newSubject.getWeekType();
        for (Subject existing : selectedSubjects) {
            if (newSubject.getSubjectId().equals(existing.getSubjectId())) {
                continue;
            }
            if (isTimeConflict(newClassTime, existing.getClassTime())
                    && isWeekRangeOverlap(newWeekRange, existing.getWeekRange())
                    && isWeekTypeConflict(newWeekType, existing.getWeekType())) {
                return true;
            }
        }
        return false;
    }

    private List<Subject> getStudentSubjectsWithinConn(String studentId, Connection conn) throws Exception {
        List<Choose> chooses = chooseDao.findByStudentId(studentId, conn);
        java.util.List<Subject> list = new java.util.ArrayList<>();
        for (Choose c : chooses) {
            try {
                Subject s = subjectDao.findById(c.getSubjectId(), conn);
                if (s != null) list.add(s);
            } catch (Exception e) {
                System.err.println("获取课程信息失败: " + e.getMessage());
            }
        }
        return list;
    }

    private boolean isTimeConflict(String time1, String time2) {
        String segmentPattern = "(周[一二三四五六日])第(\\d+)-(\\d+)节";
        Pattern p = Pattern.compile(segmentPattern);
        Matcher m1 = p.matcher(time1);
        while (m1.find()) {
            String day1 = m1.group(1);
            int start1 = Integer.parseInt(m1.group(2));
            int end1 = Integer.parseInt(m1.group(3));
            Matcher m2 = p.matcher(time2);
            while (m2.find()) {
                String day2 = m2.group(1);
                int start2 = Integer.parseInt(m2.group(2));
                int end2 = Integer.parseInt(m2.group(3));
                if (day1.equals(day2)) {
                    if (!(end1 < start2 || end2 < start1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isWeekRangeOverlap(String range1, String range2) {
        String[] parts1 = range1.split("-");
        String[] parts2 = range2.split("-");
        try {
            int start1 = Integer.parseInt(parts1[0]);
            int end1 = Integer.parseInt(parts1[1]);
            int start2 = Integer.parseInt(parts2[0]);
            int end2 = Integer.parseInt(parts2[1]);
            return !(end1 < start2 || end2 < start1);
        } catch (NumberFormatException e) {
            System.err.println("周次范围格式错误，无法解析: " + e.getMessage());
            return false; 
        }
    }

    private boolean isWeekTypeConflict(String type1, String type2) {
        if ("ALL".equals(type1) || "ALL".equals(type2)) {
            return true;
        }
        return type1.equals(type2);
    }

    private boolean isWithinCourseSelectionPeriod(Subject subject) {
        try {
            Date now = new Date(System.currentTimeMillis());
            Date startDate = subject.getSubjectDate();
            if (startDate == null) {
                System.err.println("警告: 课程开课日期为空，无法检查选课有效期。");
                return false;
            }
            long sevenDaysBefore = startDate.getTime() - (7 * 24 * 60 * 60 * 1000L);
            return now.after(new Date(sevenDaysBefore)) && now.before(startDate);
        } catch (Exception e) {
            System.err.println("检查选课有效期失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isWithinDropPeriod(Subject subject) {
        Date now = new Date(System.currentTimeMillis());
        Date startDate = subject.getSubjectDate();
        if (startDate == null) {
            System.err.println("警告: 课程开课日期为空，无法检查退选有效期。");
            return false;
        }
        return now.before(startDate);
    }
}