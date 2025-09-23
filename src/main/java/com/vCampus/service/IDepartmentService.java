package com.vCampus.service;

import com.vCampus.entity.Department;
import java.util.List;

/**
 * 部门服务接口
 * 定义部门相关的业务操作
 */
public interface IDepartmentService {

    /**
     * 根据部门ID获取部门信息
     */
    Department getDepartmentById(String departmentId);

    /**
     * 根据部门名称获取部门信息
     */
    Department getDepartmentByName(String departmentName);

    /**
     * 获取所有部门信息
     */
    List<Department> getAllDepartments();

    /**
     * 添加新部门
     */
    boolean addDepartment(Department department);

    /**
     * 更新部门信息
     */
    boolean updateDepartment(Department department);

    /**
     * 删除部门
     */
    boolean deleteDepartment(String departmentId);

    /**
     * 验证部门信息是否完整有效
     */
    boolean validateDepartment(Department department);

    /**
     * 检查部门是否存在
     */
    boolean departmentExists(String departmentId);

    /**
     * 检查部门名称是否已存在
     */
    boolean departmentNameExists(String departmentName);
}