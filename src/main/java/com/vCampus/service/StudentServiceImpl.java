package com.vCampus.service;

import com.vCampus.dao.IStudentDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.StudentDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
public class StudentServiceImpl
        extends AbstractBaseServiceImpl<Student, String> implements IStudentService {

    private final IStudentDao studentDao = new StudentDaoImpl();
    private final IUserDao userDao = new UserDaoImpl();

    // 实现抽象方法（现在有Connection参数）
    @Override
    protected Student doGetBySelfId(String studentId, Connection conn) throws Exception {
        return studentDao.findByStudentId(studentId, conn);
    }

    @Override
    protected List<Student> doGetAll(Connection conn) throws Exception {
        return studentDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Student student, Connection conn) throws Exception {
        return studentDao.insert(student, conn);
    }

    @Override
    protected boolean doUpdate(Student student, Connection conn) throws Exception {
        // 更新学生信息时，需要确保 userId 已设置，且用户表中存在对应的 userId
        // 如果 Student 继承了 User，且在创建 Student 时 User 信息也一并创建了，
        // 那么这里更新 Student 时，可能还需要更新 User 表中的部分信息 (如密码、用户名等)
        // 根据 IBaseService 的 update(T entity) 注释 "也更新tbl_user"
        // 我们应该同时更新 User 表。
        // 但这里的 doUpdate 仅调用 studentDao.update(student, conn)
        // 这意味着只更新 tbl_student，不更新 tbl_user。
        // 鉴于 IStudentService 定义了 updateStudentOnly() 和 IBaseService 的 update()
        // 这里的 doUpdate 应该对应 IBaseService 的 update()，即更新两张表。
        // 我将按照 "也更新tbl_user" 的注释来修改这里的逻辑。
        boolean studentUpdated = studentDao.update(student, conn);
        if (!studentUpdated) return false;

        // 获取完整的User信息来更新，因为Student对象可能只包含部分User信息
        var user = userDao.findById(student.getUserId(), conn);
        if (user != null) {
            user.setUsername(student.getUsername());
            user.setPassword(student.getPassword());
            user.setRole(student.getRole()); // 角色可能也需要更新
            return userDao.update(user, conn);
        }
        return false; // 如果User不存在，更新也失败
    }

    @Override
    protected boolean doDelete(String studentId, Connection conn) throws Exception {
        Student student = studentDao.findByStudentId(studentId, conn);
        if (student == null) return false;

        boolean studentDeleted = studentDao.delete(studentId, conn);
        if (!studentDeleted) return false;

        return userDao.delete(student.getUserId(), conn);
    }

    @Override
    protected boolean doExists(String studentId, Connection conn) throws Exception {
        return studentDao.findByStudentId(studentId, conn) != null;
    }

    // 实现特定方法
    @Override
    public Student getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn ->
  studentDao.findByUserId(userId, conn)
            );
        } catch (Exception e) {
            handleException("根据用户ID获取学生失败", e);
            return null;
        }
    }

    @Override
    public Student getStudentFull(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Student student = studentDao.findByStudentId(studentId, conn);
                if (student != null) {
  var user = userDao.findById(student.getUserId(), conn);
  if (user != null) {
  student.setUsername(user.getUsername());
  student.setPassword(user.getPassword());
  student.setRole(user.getRole());
  }
                }
                return student;
            });
        } catch (Exception e) {
            handleException("获取完整学生信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateStudentOnly(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn ->
  studentDao.update(student, conn)
            );
        } catch (Exception e) {
            handleException("更新学生信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteStudentOnly(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn ->
  studentDao.delete(studentId, conn)
            );
        } catch (Exception e) {
            handleException("删除学生信息失败", e);
            return false;
        }
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        try {
            // 修复：直接调用 DAO 层的方法进行数据库过滤，提高性能
            return TransactionManager.executeInTransaction(conn ->
  studentDao.findStudentsByClass(className, conn)
            );
        } catch (Exception e) {
            handleException("根据班级获取学生失败", e);
            return List.of();
        }
    }

    //--
    /**
     * 按学籍状态查询学生
     */
    public List<Student> getStudentsByStatus(String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("状态不能为空");
            }

            return TransactionManager.executeInTransaction(conn ->
  studentDao.findStudentsByStatus(status, conn)
            );
        } catch (Exception e) {
            handleException("按状态查询失败:", e);
            return List.of();
        }
    }

    @Override
    public boolean validateStudentStatus(String studentId, String requiredStatus) throws Exception {
        try {
            Student student = TransactionManager.executeInTransaction(conn ->
  studentDao.findByStudentId(studentId, conn)
            );
            if (student == null) {
                throw new Exception("学生不存在: " + studentId);
            }
            return requiredStatus.equals(student.getStatus());
        } catch (Exception e) {
            throw new Exception("验证学生状态失败: " + e.getMessage());
        }
    }

    @Override
    public String getStudentStatus(String studentId) throws Exception {
        try {
            Student student = TransactionManager.executeInTransaction(conn ->
  studentDao.findByStudentId(studentId, conn)
            );
            if (student == null) {
                throw new Exception("学生不存在: " + studentId);
            }
            return student.getStatus();
        } catch (Exception e) {
            throw new Exception("获取学生状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isStudentActive(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                return false;
            }
            return TransactionManager.executeInTransaction(conn ->
  studentDao.isStudentActive(studentId, conn)
            );
        } catch (Exception e) {
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
            return TransactionManager.executeInTransaction(conn ->
  studentDao.updateStudentStatus(studentId, status, conn)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //--------------

    @Override
    public int getStudentOrderCount(String studentId) {
        try {
            if (studentId == null || studentId.trim().isEmpty()) {
                return 0;
            }
            return TransactionManager.executeInTransaction(conn ->
  studentDao.getStudentOrderCount(studentId, conn)
            );
        } catch (Exception e) {
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
            return TransactionManager.executeInTransaction(conn ->
  studentDao.getStudentTotalSpending(studentId, conn)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}