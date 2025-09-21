package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Teacher;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 教师服务实现类（客户端CS）：HTTP调用服务端
 */
public class TeacherServiceImpl 
    extends AbstractBaseServiceImpl<Teacher, String> implements ITeacherService {

    @Override
    protected Teacher doGetBySelfId(String teacherId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/teachers/" + teacherId);
        return HttpClientUtil.parseObject(data, Teacher.class);
    }

    @Override
    protected List<Teacher> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/teachers");
        return HttpClientUtil.parseList(data, Teacher.class);
    }

    @Override
    protected boolean doAdd(Teacher teacher, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPost("/teachers", teacher);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(Teacher teacher, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPut("/teachers/" + teacher.getTeacherId(), teacher);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String teacherId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/teachers/" + teacherId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String teacherId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/teachers/exists/" + teacherId);
        return data.asBoolean(false);
    }

    @Override
    public Teacher getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(c -> {
                JsonNode data = HttpClientUtil.sendGet("/teachers/by-user/" + userId);
                return HttpClientUtil.parseObject(data, Teacher.class);
            });
        } catch (Exception e) {
            handleException("根据用户ID获取教师失败", e);
            return null;
        }
    }

    @Override
    public Teacher getTeacherFull(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(c -> {
                JsonNode data = HttpClientUtil.sendGet("/teachers/full/" + teacherId);
                return HttpClientUtil.parseObject(data, Teacher.class);
            });
        } catch (Exception e) {
            handleException("获取完整教师信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateTeacherOnly(Teacher teacher) {
        try {
            return TransactionManager.executeInTransaction(c -> {
                JsonNode data = HttpClientUtil.sendPut("/teachers/only/" + teacher.getTeacherId(), teacher);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("更新教师信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteTeacherOnly(String teacherId) {
        try {
            return TransactionManager.executeInTransaction(c -> {
                JsonNode data = HttpClientUtil.sendDelete("/teachers/only/" + teacherId);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("删除教师信息失败", e);
            return false;
        }
    }

    @Override
    public List<Teacher> getTeachersByDepartment(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(c -> {
                JsonNode data = HttpClientUtil.sendGet("/teachers/by-dept/" + departmentId);
                return HttpClientUtil.parseList(data, Teacher.class);
            });
        } catch (Exception e) {
            handleException("根据班级获取教师失败", e);
            return List.of();
        }
    }
}