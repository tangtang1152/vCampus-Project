package com.vCampus.service;

import com.vCampus.dao.ITeacherDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.TeacherDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Teacher;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师服务实现类
 */
public class TeacherServiceImpl 
    extends AbstractBaseServiceImpl<Teacher, String> implements ITeacherService {

    private final ITeacherDao teacherDao = new TeacherDaoImpl();
    private final IUserDao userDao = new UserDaoImpl();
    
    // 实现抽象方法（现在有Connection参数）
    @Override
    protected Teacher doGetBySelfId(String teacherId, Connection conn) throws Exception {
        return teacherDao.findByTeacherId(teacherId, conn);
    }

    @Override
    protected List<Teacher> doGetAll(Connection conn) throws Exception {
        return teacherDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Teacher teacher, Connection conn) throws Exception {
        return teacherDao.insert(teacher, conn);
    }

    @Override
    protected boolean doUpdate(Teacher teacher, Connection conn) throws Exception {
        return teacherDao.update(teacher, conn);
    }

    @Override
    protected boolean doDelete(String teacherId, Connection conn) throws Exception {
        Teacher teacher = teacherDao.findByTeacherId(teacherId, conn);
        if (teacher == null) return false;
        
        boolean teacherDeleted = teacherDao.delete(teacherId, conn);
        if (!teacherDeleted) return false;
        
        return userDao.delete(teacher.getUserId(), conn);
    }

    @Override
    protected boolean doExists(String teacherId, Connection conn) throws Exception {
        return teacherDao.findByTeacherId(teacherId, conn) != null;
    }

    // 实现特定方法
    @Override
    public Teacher getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.findByUserId(userId, conn)
            );
        } catch (Exception e) {
            handleException("根据用户ID获取教师失败", e);
            return null;
        }
    }

    @Override
    public Teacher getTeacherFull(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Teacher teacher = teacherDao.findByTeacherId(teacherId, conn);
                if (teacher != null) {
                    var user = userDao.findById(teacher.getUserId(), conn);
                    if (user != null) {
                        teacher.setUsername(user.getUsername());
                        teacher.setPassword(user.getPassword());
                        teacher.setRole(user.getRole());
                    }
                }
                return teacher;
            });
        } catch (Exception e) {
            handleException("获取完整教师信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateTeacherOnly(Teacher teacher) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                teacherDao.update(teacher, conn)
            );
        } catch (Exception e) {
            handleException("更新教师信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteTeacherOnly(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(conn ->
                teacherDao.delete(teacherId, conn)
            );
        } catch (Exception e) {
            handleException("删除教师信息失败", e);
            return false;
        }
    }

    @Override
    public List<Teacher> getTeachersByDepartment(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<Teacher> allTeachers = teacherDao.findAll(conn);
                return allTeachers.stream()
                    .filter(teacher -> departmentId.equals(teacher.getDepartmentId()))
                    .collect(Collectors.toList());
            });
        } catch (Exception e) {
            handleException("根据班级获取教师失败", e);
            return List.of();
        }
    }
}