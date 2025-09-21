package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Department;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 部门服务实现类（客户端CS）：HTTP调用服务端
 */
public class DepartmentServiceImpl 
    extends AbstractBaseServiceImpl<Department, String> implements IDepartmentService {

    @Override
    protected Department doGetBySelfId(String departmentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/departments/" + departmentId);
        return HttpClientUtil.parseObject(data, Department.class);
    }

    @Override
    protected List<Department> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/departments");
        return HttpClientUtil.parseList(data, Department.class);
    }

    @Override
    protected boolean doAdd(Department department, Connection conn) throws Exception {
        if (!validateDepartment(department)) return false;
        JsonNode data = HttpClientUtil.sendPost("/departments", department);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(Department department, Connection conn) throws Exception {
        if (!validateDepartment(department)) return false;
        JsonNode data = HttpClientUtil.sendPut("/departments/" + department.getDepartmentId(), department);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String departmentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/departments/" + departmentId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String departmentId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/departments/exists/" + departmentId);
        return data.asBoolean(false);
    }

    @Override
    public Department getDepartmentById(String departmentId) {
        return getBySelfId(departmentId);
    }

    @Override
    public Department getDepartmentByName(String departmentName) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/departments/by-name/" + departmentName);
                return HttpClientUtil.parseObject(data, Department.class);
            });
        } catch (Exception e) {
            handleException("获取部门信息失败", e);
            return null;
        }
    }

    @Override
    public List<Department> getAllDepartments() { return getAll(); }

    @Override
    public boolean addDepartment(Department department) { return add(department); }

    @Override
    public boolean updateDepartment(Department department) { return update(department); }

    @Override
    public boolean deleteDepartment(String departmentId) { return delete(departmentId); }

    @Override
    public boolean validateDepartment(Department department) {
        if (department.getDepartmentId() == null || department.getDepartmentId().trim().isEmpty()) return false;
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) return false;
        if (!department.getDepartmentId().matches("[A-Za-z0-9]+")) return false;
        return true;
    }

    @Override
    public boolean departmentExists(String departmentId) { return exists(departmentId); }

    @Override
    public boolean departmentNameExists(String departmentName) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/departments/exists/by-name/" + departmentName);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("检查部门名称存在性失败", e);
            return false;
        }
    }
}