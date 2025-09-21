package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Student;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 学生服务实现类（客户端CS）：HTTP调用服务端
 */
public class StudentServiceImpl 
	extends AbstractBaseServiceImpl<Student, String> implements IStudentService {

    @Override
    protected Student doGetBySelfId(String studentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/students/" + studentId);
        return HttpClientUtil.parseObject(data, Student.class);
    }

    @Override
    protected List<Student> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/students");
        return HttpClientUtil.parseList(data, Student.class);
    }

    @Override
    protected boolean doAdd(Student student, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPost("/students", student);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(Student student, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPut("/students/" + student.getStudentId(), student);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String studentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/students/" + studentId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String studentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/students/exists/" + studentId);
        return data.asBoolean(false);
    }

    @Override
    public Student getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/students/by-user/" + userId);
                return HttpClientUtil.parseObject(data, Student.class);
            });
        } catch (Exception e) {
            handleException("根据用户ID获取学生失败", e);
            return null;
        }
    }

    @Override
    public Student getStudentFull(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/students/full/" + studentId);
                return HttpClientUtil.parseObject(data, Student.class);
            });
        } catch (Exception e) {
            handleException("获取完整学生信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateStudentOnly(Student student) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendPut("/students/only/" + student.getStudentId(), student);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("更新学生信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteStudentOnly(String studentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendDelete("/students/only/" + studentId);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("删除学生信息失败", e);
            return false;
        }
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/students/by-class/" + className);
                return HttpClientUtil.parseList(data, Student.class);
            });
        } catch (Exception e) {
            handleException("根据班级获取学生失败", e);
            return List.of();
        }
    }
}