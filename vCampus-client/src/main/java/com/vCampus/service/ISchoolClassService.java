package com.vCampus.service;

import com.vCampus.entity.SchoolClass;
import java.util.List;

/**
 * 班级服务接口
 * 定义班级相关的业务操作
 */
public interface ISchoolClassService {

    /**
     * 根据班级ID获取班级信息
     */
    SchoolClass getClassById(String classId);

    /**
     * 获取所有班级信息
     */
    List<SchoolClass> getAllClasses();

    /**
     * 根据部门ID获取班级列表
     */
    List<SchoolClass> getClassesByDepartmentId(String departmentId);

    /**
     * 根据班级名称模糊查询班级
     */
    List<SchoolClass> getClassesByClassName(String className);

    /**
     * 添加新班级
     */
    boolean addClass(SchoolClass schoolClass);

    /**
     * 更新班级信息
     */
    boolean updateClass(SchoolClass schoolClass);

    /**
     * 删除班级
     */
    boolean deleteClass(String classId);

    /**
     * 验证班级信息是否完整有效
     */
    boolean validateClass(SchoolClass schoolClass);

    /**
     * 检查班级是否存在
     */
    boolean classExists(String classId);

    /**
     * 检查班级名称是否已存在
     */
    boolean classNameExists(String className);
}