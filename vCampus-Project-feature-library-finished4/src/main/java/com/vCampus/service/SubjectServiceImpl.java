package com.vCampus.service;

import com.vCampus.dao.ISubjectDao;
import com.vCampus.dao.SubjectDaoImpl;
import com.vCampus.entity.Subject;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 课程服务实现类
 */
public class SubjectServiceImpl extends AbstractBaseServiceImpl<Subject, String> implements ISubjectService {

    private static final ISubjectDao subjectDao = new SubjectDaoImpl();
    // 周次范围正则（如 "1-8"、"3-16"）
    private static final Pattern WEEK_RANGE_PATTERN = Pattern.compile("^\\d+-\\d+$");
    // 单双周规则允许的值
    private static final String[] VALID_WEEK_TYPES = {"ALL", "ODD", "EVEN"};

    // 实现抽象方法
    @Override
    protected Subject doGetBySelfId(String subjectId, Connection conn) throws Exception {
        return subjectDao.findById(subjectId, conn);
    }

    @Override
    protected List<Subject> doGetAll(Connection conn) throws Exception {
        return subjectDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Subject subject, Connection conn) throws Exception {
        // 验证课程信息（包含新增字段校验）
        if (!validateSubject(subject)) {
            System.out.println("课程信息验证失败");
            return false;
        }
        
        return subjectDao.insert(subject, conn);
    }

    @Override
    protected boolean doUpdate(Subject subject, Connection conn) throws Exception {
        // 验证课程信息（包含新增字段校验）
        if (!validateSubject(subject)) {
            System.out.println("课程信息验证失败");
            return false;
        }
        
        return subjectDao.update(subject, conn);
    }

    @Override
    protected boolean doDelete(String subjectId, Connection conn) throws Exception {
        return subjectDao.delete(subjectId, conn);
    }

    @Override
    protected boolean doExists(String subjectId, Connection conn) throws Exception {
        return subjectDao.findById(subjectId, conn) != null;
    }

    /**
     * 根据课程ID获取课程信息 - ISubjectService接口方法
     */
    @Override
    public Subject getSubjectById(String subjectId) {
        return getBySelfId(subjectId);
    }

    /**
     * 获取所有课程信息 - ISubjectService接口方法
     */
    @Override
    public List<Subject> getAllSubjects() {
        return getAll();
    }

    /**
     * 添加新课程 - ISubjectService接口方法
     */
    @Override
    public boolean addSubject(Subject subject) {
        return add(subject);
    }

    /**
     * 更新课程信息 - ISubjectService接口方法
     */
    @Override
    public boolean updateSubject(Subject subject) {
        return update(subject);
    }

    /**
     * 删除课程 - ISubjectService接口方法
     */
    @Override
    public boolean deleteSubject(String subjectId) {
        return delete(subjectId);
    }

    /**
     * 根据教师ID获取课程列表 - ISubjectService接口方法
     */
    @Override
    public List<Subject> getSubjectsByTeacherId(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                subjectDao.findByTeacherId(teacherId, conn)
            );
        } catch (Exception e) {
            handleException("根据教师ID获取课程失败", e);
            return List.of();
        }
    }

    /**
     * 根据课程名称模糊查询课程 - ISubjectService接口方法
     */
    @Override
    public List<Subject> getSubjectsByName(String subjectName) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                subjectDao.findBySubjectName(subjectName, conn)
            );
        } catch (Exception e) {
            handleException("根据课程名称查询课程失败", e);
            return List.of();
        }
    }

    /**
     * 验证课程信息是否完整有效 - 新增对课程时间相关字段的校验
     */
    @Override
    public boolean validateSubject(Subject subject) {
        // 原有字段校验
        if (subject.getSubjectId() == null || subject.getSubjectId().trim().isEmpty()) {
            System.out.println("课程ID为空");
            return false;
        }

        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            System.out.println("课程名称为空");
            return false;
        }

        if (subject.getSubjectDate() == null) {
            System.out.println("开课日期为空");
            return false;
        }

        if (subject.getSubjectNum() == null || subject.getSubjectNum() <= 0) {
            System.out.println("学时数无效: " + subject.getSubjectNum());
            return false;
        }

        if (subject.getCredit() == null || subject.getCredit() <= 0) {
            System.out.println("学分无效: " + subject.getCredit());
            return false;
        }

        if (subject.getTeacherId() == null || subject.getTeacherId().trim().isEmpty()) {
            System.out.println("教师ID为空");
            return false;
        }

        
        
        
        // 新增字段校验
        // 1. 周次范围校验（格式如 "1-8"）
        if (subject.getWeekRange() == null || !WEEK_RANGE_PATTERN.matcher(subject.getWeekRange()).matches()) {
            System.out.println("周次范围格式无效（应为\"起始周-结束周\"，如\"1-8\"）: " + subject.getWeekRange());
            return false;
        }
        // 解析周次范围并校验起始周 <= 结束周
        String[] weekParts = subject.getWeekRange().split("-");
        try {
            int startWeek = Integer.parseInt(weekParts[0]);
            int endWeek = Integer.parseInt(weekParts[1]);
            if (startWeek < 1 || endWeek < startWeek || endWeek > 20) { // 假设最大周数为20
                System.out.println("周次范围数值无效（起始周<=结束周，且在1-20范围内）: " + subject.getWeekRange());
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("周次范围数值不是有效数字: " + subject.getWeekRange());
            return false;
        }

        // 2. 单双周规则校验（必须是 ALL/ODD/EVEN）
        if (subject.getWeekType() == null) {
            System.out.println("单双周规则为空");
            return false;
        }
        boolean isValidWeekType = false;
        for (String type : VALID_WEEK_TYPES) {
            if (type.equals(subject.getWeekType())) {
                isValidWeekType = true;
                break;
            }
        }
        if (!isValidWeekType) {
            System.out.println("单双周规则无效（必须是ALL/ODD/EVEN）: " + subject.getWeekType());
            return false;
        }

        // 3. 上课时间校验（不为空且有基本格式）
        if (subject.getClassTime() == null || subject.getClassTime().trim().isEmpty()) {
            System.out.println("上课时间为空");
            return false;
        }
        // 修正后的正则表达式：匹配 "周一第1-2节" 或 "周一第1-2节,周三第3-4节"
        // 注意：\s* 允许逗号后有可选的空格
        String classTimePattern = "^周[一二三四五六日]第\\d+-\\d+节(,\\s*周[一二三四五六日]第\\d+-\\d+节)*$";
        if (!subject.getClassTime().matches(classTimePattern)) {
        	System.out.println("上课时间格式无效（应为\"周一第1-2节,周三第3-4节\"）: " + subject.getClassTime());
        	return false;
        }

        // 4. 教室位置校验（不为空）
        if (subject.getClassroom() == null || subject.getClassroom().trim().isEmpty()) {
            System.out.println("教室位置为空");
            return false;
        }
        // 教室格式简单校验（如 "教101"、"实202"）
        if (!subject.getClassroom().matches("^[\\u4e00-\\u9fa5a-zA-Z0-9]+$")) {
            System.out.println("教室位置包含无效字符: " + subject.getClassroom());
            return false;
        }

        return true;
    }
}