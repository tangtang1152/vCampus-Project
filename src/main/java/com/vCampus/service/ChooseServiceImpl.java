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

    /**
     * 新增的管理员代选方法
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @param ignoreTimeConflict 是否无视时间冲突
     * @return 代选是否成功
     */
    @Override
    public boolean adminAssistChooseSubject(String studentId, String subjectId, boolean ignoreTimeConflict) {
        // 管理员代选：是否忽略时间冲突由参数控制；一律忽略选课有效期
        return doChooseSubject(studentId, subjectId, ignoreTimeConflict, true);
    }

    /**
     * 核心选课逻辑的私有方法，包含是否忽略时间冲突和选课有效期的参数
     * @param studentId 学生ID
     * @param subjectId 课程ID
     * @param ignoreTimeConflict true表示忽略时间冲突，false表示进行时间冲突检查
     * @param ignoreCourseSelectionPeriod true表示忽略选课有效期，false表示进行选课有效期检查
     * @return 选课是否成功
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
                // 新增：验证课程时间信息完整性（这个通常不应该跳过，因为课程信息本身就应该完整）
                if (!isSubjectTimeValid(subject)) {
  System.out.println("选课失败: 课程时间信息不完整，无法选课，课程ID: " + subjectId);
  return false;
                }

                // 3. 检查是否已选该课程 (这个通常不应该跳过)
                Choose existingChoose = chooseDao.findByStudentAndSubject(studentId, subjectId, conn);
                if (existingChoose != null) {
  System.out.println("选课失败: 学生已选该课程，学号: " + studentId + ", 课程ID: " + subjectId);
  return false;
                }

                // 4. 检查课程容量 (这个通常不应该跳过)
                List<Choose> subjectChooses = chooseDao.findBySubjectId(subjectId, conn);
                if (subjectChooses.size() >= subject.getSubjectNum()) {
  System.out.println("选课失败: 课程已满，课程ID: " + subjectId);
  return false;
                }

                // 5. 检查选课时间有效期 (根据参数决定是否跳过)
                if (!ignoreCourseSelectionPeriod && !isWithinCourseSelectionPeriod(subject)) {
  System.out.println("选课失败: 已超过选课时间范围，课程ID: " + subjectId);
  return false;
                }

                // 6. 检查时间冲突 (根据参数决定是否跳过)
                if (!ignoreTimeConflict && hasTimeConflict(studentId, subject, conn)) {
  System.out.println("选课失败: 选课时间冲突，课程ID: " + subjectId);
  return false;
                }
                
                // 7. 创建选课记录
                Choose choose = new Choose();
                choose.setSelectid(generateSelectId());
                choose.setStudentId(studentId);
                choose.setSubjectId(subjectId);

                // 8. 插入选课记录
                boolean success = chooseDao.insert(choose, conn);
                if (success) {
  System.out.println("选课成功，选课ID: " + choose.getSelectid());
                }
                return success;
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

                // 2. 退选有效期校验（已按需求关闭，允许任何时间退课）
                // Subject subject = subjectDao.findById(choose.getSubjectId(), conn);
                // if (subject != null && !isWithinDropPeriod(subject)) {
                //     System.out.println("退选失败: 已超过退选时间，无法退选，选课ID: " + selectid);
                //     return false;
                // }

                // 3. 删除选课记录
                boolean success = chooseDao.delete(selectid, conn);
                if (success) {
  System.out.println("退选成功，选课ID: " + selectid);
                }
                return success;
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
        // 检查选课ID是否有效
        if (choose.getSelectid() == null || choose.getSelectid().trim().isEmpty()) {
            System.out.println("选课ID为空");
            return false;
        }

        // 检查学生ID是否有效
        if (choose.getStudentId() == null || choose.getStudentId().trim().isEmpty()) {
            System.out.println("学生ID为空");
            return false;
        }

        // 检查课程ID是否有效
        if (choose.getSubjectId() == null || choose.getSubjectId().trim().isEmpty()) {
            System.out.println("课程ID为空");
            return false;
        }

        return true;
    }

    /**
     * 生成唯一的选课ID
     * @return 选课ID
     */
    private String generateSelectId() {
        return "SELECT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 检查课程时间信息是否完整有效
     */
    private boolean isSubjectTimeValid(Subject subject) {
        return subject.getWeekRange() != null && !subject.getWeekRange().trim().isEmpty()
                && subject.getWeekType() != null && !subject.getWeekType().trim().isEmpty()
                && subject.getClassTime() != null && !subject.getClassTime().trim().isEmpty()
                && subject.getClassroom() != null && !subject.getClassroom().trim().isEmpty();
    }

    /**
     * 检查与已选课程是否存在时间冲突
     */
    private boolean hasTimeConflict(String studentId, Subject newSubject, Connection conn) throws Exception {
        // 获取学生已选课程
        List<Subject> selectedSubjects = getStudentSubjects(studentId);
        
        // 解析新课程时间
        String newClassTime = newSubject.getClassTime();
        String newWeekRange = newSubject.getWeekRange();
        String newWeekType = newSubject.getWeekType();
        
        // 检查每门已选课程的时间冲突
        for (Subject existing : selectedSubjects) {
            // 排除同一门课程自己和自己比较的情况
            if (newSubject.getSubjectId().equals(existing.getSubjectId())) {
                continue;
            }

            if (isTimeConflict(newClassTime, existing.getClassTime())
  && isWeekRangeOverlap(newWeekRange, existing.getWeekRange())
  && isWeekTypeConflict(newWeekType, existing.getWeekType())) {
                return true; // 存在时间冲突
            }
        }
        return false; // 没有发现时间冲突
    }

    /**
     * 检查上课时间是否冲突（如周一第1-2节与周一第2-3节冲突）
     * 改进：使用正则表达式解析具体的星期和节次，进行精确比较。
     */
    private boolean isTimeConflict(String time1, String time2) {
        String segmentPattern = "(周[一二三四五六日])第(\\d+)-(\\d+)节"; // 更正正则表达式，避免双反斜杠问题
        Pattern p = Pattern.compile(segmentPattern);

        Matcher m1 = p.matcher(time1);

        // 遍历 time1 中的所有上课时间段
        while (m1.find()) {
            String day1 = m1.group(1); // 例如 "周一"
            int start1 = Integer.parseInt(m1.group(2)); // 例如 1
            int end1 = Integer.parseInt(m1.group(3));   // 例如 2

            // 为 time2 创建一个新的 Matcher，以便每次都能从头开始匹配
            Matcher m2 = p.matcher(time2);
            // 遍历 time2 中的所有上课时间段
            while (m2.find()) {
                String day2 = m2.group(1); // 例如 "周一"
                int start2 = Integer.parseInt(m2.group(2)); // 例如 2
                int end2 = Integer.parseInt(m2.group(3));   // 例如 3

                // 如果星期相同，则检查节次是否冲突
                if (day1.equals(day2)) {
  // 检查时间段是否有重叠
  // 如果 (end1 < start2) 或者 (end2 < start1) 成立，则表示没有重叠
  // 取反即为有重叠
  if (!(end1 < start2 || end2 < start1)) {
  return true; // 存在时间冲突
  }
                }
            }
        }
        return false; // 没有发现时间冲突
    }


    /**
     * 检查周次范围是否重叠
     */
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

    /**
     * 检查单双周规则是否冲突
     * ALL与任何类型都冲突，ODD和EVEN不冲突 (ODD vs ODD 冲突, EVEN vs EVEN 冲突)
     */
    private boolean isWeekTypeConflict(String type1, String type2) {
        if ("ALL".equals(type1) || "ALL".equals(type2)) {
            return true; // 任何一方是ALL，则与任何其他类型都可能冲突
        }
        return type1.equals(type2); // 如果都不是ALL，则只有当类型完全相同时才冲突（ODD vs ODD 或 EVEN vs EVEN）
    }

    /**
     * 检查是否在选课有效期内（简化版：开课日期前7天内可选课）
     */
    private boolean isWithinCourseSelectionPeriod(Subject subject) {
        try {
            Date now = new Date(System.currentTimeMillis()); // 使用当前时间
            Date startDate = subject.getSubjectDate();
            if (startDate == null) {
                System.err.println("警告: 课程开课日期为空，无法检查选课有效期。");
                return false; // 如果开课日期为空，则认为不在有效期内
            }
            // 计算开课日期前7天
            long sevenDaysBefore = startDate.getTime() - (7 * 24 * 60 * 60 * 1000L);
            return now.after(new Date(sevenDaysBefore)) && now.before(startDate);
        } catch (Exception e) {
            System.err.println("检查选课有效期失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查是否在退选有效期内（简化版：开课前可退选）
     */
    private boolean isWithinDropPeriod(Subject subject) {
        Date now = new Date(System.currentTimeMillis()); // 使用当前时间
        Date startDate = subject.getSubjectDate();
        if (startDate == null) {
            System.err.println("警告: 课程开课日期为空，无法检查退选有效期。");
            return false; // 如果开课日期为空，则认为不在有效期内
        }
        // 允许在开课日期之前退选
        return now.before(startDate);
    }
}