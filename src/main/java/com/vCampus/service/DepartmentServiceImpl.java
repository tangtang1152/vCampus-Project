package com.vCampus.service;

import com.vCampus.dao.IDepartmentDao;
import com.vCampus.dao.DepartmentDaoImpl;
import com.vCampus.entity.Department;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 部门服务实现类
 * 提供对部门数据的业务逻辑操作实现
 */
public class DepartmentServiceImpl 
    extends AbstractBaseServiceImpl<Department, String> implements IDepartmentService {

    private final IDepartmentDao departmentDao;

    public DepartmentServiceImpl() {
        this.departmentDao = new DepartmentDaoImpl();
    }

    // 实现抽象方法
    @Override
    protected Department doGetBySelfId(String departmentId, Connection conn) throws Exception {
        return departmentDao.findById(departmentId, conn);
    }

    @Override
    protected List<Department> doGetAll(Connection conn) throws Exception {
        return departmentDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Department department, Connection conn) throws Exception {
        // 验证部门信息
        if (!validateDepartment(department)) {
            System.out.println("部门信息验证失败");
            return false;
        }
        return departmentDao.insert(department, conn);
    }

    @Override
    protected boolean doUpdate(Department department, Connection conn) throws Exception {
        // 验证部门信息
        if (!validateDepartment(department)) {
            System.out.println("部门信息验证失败");
            return false;
        }
        return departmentDao.update(department, conn);
    }

    @Override
    protected boolean doDelete(String departmentId, Connection conn) throws Exception {
        // 检查部门是否存在
        Department department = departmentDao.findById(departmentId, conn);
        if (department == null) {
            System.out.println("部门不存在，部门ID: " + departmentId);
            return false;
        }

        // TODO: 可以添加额外的业务逻辑检查，如该部门下是否有员工等
        return departmentDao.delete(departmentId, conn);
    }

    @Override
    protected boolean doExists(String departmentId, Connection conn) throws Exception {
        return departmentDao.findById(departmentId, conn) != null;
    }

    /**
     * 根据部门ID获取部门信息 - IDepartmentService接口方法
     */
    @Override
    public Department getDepartmentById(String departmentId) {
        return getBySelfId(departmentId);
    }

    /**
     * 根据部门名称获取部门信息 - IDepartmentService接口方法
     */
    @Override
    public Department getDepartmentByName(String departmentName) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                departmentDao.findByDepartmentName(departmentName, conn)
            );
        } catch (Exception e) {
            handleException("获取部门信息失败", e);
            return null;
        }
    }

    /**
     * 获取所有部门信息 - IDepartmentService接口方法
     */
    @Override
    public List<Department> getAllDepartments() {
        return getAll();
    }

    /**
     * 添加新部门 - IDepartmentService接口方法
     */
    @Override
    public boolean addDepartment(Department department) {
        return add(department);
    }

    /**
     * 更新部门信息 - IDepartmentService接口方法
     */
    @Override
    public boolean updateDepartment(Department department) {
        return update(department);
    }

    /**
     * 删除部门 - IDepartmentService接口方法
     */
    @Override
    public boolean deleteDepartment(String departmentId) {
        return delete(departmentId);
    }

    /**
     * 验证部门信息是否完整有效 - IDepartmentService接口方法
     */
    @Override
    public boolean validateDepartment(Department department) {
        // 检查部门ID是否为空
        if (department.getDepartmentId() == null || department.getDepartmentId().trim().isEmpty()) {
            System.out.println("部门ID为空");
            return false;
        }

        // 检查部门名称是否为空
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            System.out.println("部门名称为空");
            return false;
        }

        // 检查部门ID格式（可根据实际需求调整）
        if (!department.getDepartmentId().matches("[A-Za-z0-9]+")) {
            System.out.println("部门ID格式不正确: " + department.getDepartmentId());
            return false;
        }

        return true;
    }

    /**
     * 检查部门是否存在 - IDepartmentService接口方法
     */
    @Override
    public boolean departmentExists(String departmentId) {
        return exists(departmentId);
    }

    /**
     * 检查部门名称是否已存在 - IDepartmentService接口方法
     */
    @Override
    public boolean departmentNameExists(String departmentName) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Department department = departmentDao.findByDepartmentName(departmentName, conn);
                return department != null;
            });
        } catch (Exception e) {
            handleException("检查部门名称存在性失败", e);
            return false;
        }
    }
}