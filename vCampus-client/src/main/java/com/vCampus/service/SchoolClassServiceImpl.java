package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.SchoolClass;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 班级服务实现类（客户端CS）：HTTP调用服务端
 */
public class SchoolClassServiceImpl extends AbstractBaseServiceImpl<SchoolClass, String> implements ISchoolClassService {

    @Override
    protected SchoolClass doGetBySelfId(String classId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/classes/" + classId);
        return HttpClientUtil.parseObject(data, SchoolClass.class);
    }

    @Override
    protected List<SchoolClass> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/classes");
        return HttpClientUtil.parseList(data, SchoolClass.class);
    }

    @Override
    protected boolean doAdd(SchoolClass schoolClass, Connection conn) throws Exception {
        if (!validateClass(schoolClass)) return false;
        JsonNode data = HttpClientUtil.sendPost("/classes", schoolClass);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(SchoolClass schoolClass, Connection conn) throws Exception {
        if (!validateClass(schoolClass)) return false;
        JsonNode data = HttpClientUtil.sendPut("/classes/" + schoolClass.getClassId(), schoolClass);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String classId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/classes/" + classId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String classId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/classes/exists/" + classId);
        return data.asBoolean(false);
    }

    @Override
    public SchoolClass getClassById(String classId) { return getBySelfId(classId); }

    @Override
    public List<SchoolClass> getAllClasses() { return getAll(); }

    @Override
    public List<SchoolClass> getClassesByDepartmentId(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/classes/by-department/" + departmentId);
                return HttpClientUtil.parseList(data, SchoolClass.class);
            });
        } catch (Exception e) {
            handleException("根据部门ID获取班级失败", e);
            return List.of();
        }
    }

    @Override
    public List<SchoolClass> getClassesByClassName(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/classes/by-name/" + className);
                return HttpClientUtil.parseList(data, SchoolClass.class);
            });
        } catch (Exception e) {
            handleException("根据班级名称查询班级失败", e);
            return List.of();
        }
    }

    @Override
    public boolean addClass(SchoolClass schoolClass) { return add(schoolClass); }

    @Override
    public boolean updateClass(SchoolClass schoolClass) { return update(schoolClass); }

    @Override
    public boolean deleteClass(String classId) { return delete(classId); }

    @Override
    public boolean validateClass(SchoolClass schoolClass) {
        if (schoolClass.getClassId() == null || schoolClass.getClassId().trim().isEmpty()) return false;
        if (schoolClass.getClassName() == null || schoolClass.getClassName().trim().isEmpty()) return false;
        if (!schoolClass.getClassId().matches("[A-Za-z0-9]+")) return false;
        if (schoolClass.getClassName().length() > 50) return false;
        return true;
    }

    @Override
    public boolean classExists(String classId) { return exists(classId); }

    @Override
    public boolean classNameExists(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/classes/exists/by-name/" + className);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("检查班级名称存在性失败", e);
            return false;
        }
    }
}