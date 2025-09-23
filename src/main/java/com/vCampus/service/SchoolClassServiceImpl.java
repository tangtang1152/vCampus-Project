package com.vCampus.service;

import com.vCampus.dao.ISchoolClassDao;
import com.vCampus.dao.SchoolClassDaoImpl;
import com.vCampus.entity.SchoolClass;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 班级服务实现类
 * 提供对班级数据的业务逻辑操作实现
 */
public class SchoolClassServiceImpl extends AbstractBaseServiceImpl<SchoolClass, String> implements ISchoolClassService {

    private final ISchoolClassDao schoolClassDao;

    public SchoolClassServiceImpl() {
        this.schoolClassDao = new SchoolClassDaoImpl();
    }

    // 实现抽象方法
    @Override
    protected SchoolClass doGetBySelfId(String classId, Connection conn) throws Exception {
        return schoolClassDao.findById(classId, conn);
    }

    @Override
    protected List<SchoolClass> doGetAll(Connection conn) throws Exception {
        return schoolClassDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(SchoolClass schoolClass, Connection conn) throws Exception {
        // 验证班级信息
        if (!validateClass(schoolClass)) {
            System.out.println("班级信息验证失败");
            return false;
        }

        // 检查班级是否已存在
        if (schoolClassDao.findById(schoolClass.getClassId(), conn) != null) {
            System.out.println("班级已存在，班级ID: " + schoolClass.getClassId());
            return false;
        }

        return schoolClassDao.insert(schoolClass, conn);
    }

    @Override
    protected boolean doUpdate(SchoolClass schoolClass, Connection conn) throws Exception {
        // 验证班级信息
        if (!validateClass(schoolClass)) {
            System.out.println("班级信息验证失败");
            return false;
        }

        // 检查班级是否存在
        if (schoolClassDao.findById(schoolClass.getClassId(), conn) == null) {
            System.out.println("班级不存在，班级ID: " + schoolClass.getClassId());
            return false;
        }

        return schoolClassDao.update(schoolClass, conn);
    }

    @Override
    protected boolean doDelete(String classId, Connection conn) throws Exception {
        // 检查班级是否存在
        if (schoolClassDao.findById(classId, conn) == null) {
            System.out.println("班级不存在，班级ID: " + classId);
            return false;
        }

        // TODO: 可以添加额外的业务逻辑检查，如该班级下是否有学生等
        return schoolClassDao.delete(classId, conn);
    }

    @Override
    protected boolean doExists(String classId, Connection conn) throws Exception {
        return schoolClassDao.findById(classId, conn) != null;
    }

    /**
     * 根据班级ID获取班级信息 - ISchoolClassService接口方法
     */
    @Override
    public SchoolClass getClassById(String classId) {
        return getBySelfId(classId);
    }

    /**
     * 获取所有班级信息 - ISchoolClassService接口方法
     */
    @Override
    public List<SchoolClass> getAllClasses() {
        return getAll();
    }

    /**
     * 根据部门ID获取班级列表 - ISchoolClassService接口方法
     */
    @Override
    public List<SchoolClass> getClassesByDepartmentId(String departmentId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                schoolClassDao.findByDepartmentId(departmentId, conn)
            );
        } catch (Exception e) {
            handleException("根据部门ID获取班级失败", e);
            return List.of();
        }
    }

    /**
     * 根据班级名称模糊查询班级 - ISchoolClassService接口方法
     */
    @Override
    public List<SchoolClass> getClassesByClassName(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                schoolClassDao.findByClassName(className, conn)
            );
        } catch (Exception e) {
            handleException("根据班级名称查询班级失败", e);
            return List.of();
        }
    }

    /**
     * 添加新班级 - ISchoolClassService接口方法
     */
    @Override
    public boolean addClass(SchoolClass schoolClass) {
        return add(schoolClass);
    }

    /**
     * 更新班级信息 - ISchoolClassService接口方法
     */
    @Override
    public boolean updateClass(SchoolClass schoolClass) {
        return update(schoolClass);
    }

    /**
     * 删除班级 - ISchoolClassService接口方法
     */
    @Override
    public boolean deleteClass(String classId) {
        return delete(classId);
    }

    /**
     * 验证班级信息是否完整有效 - ISchoolClassService接口方法
     */
    @Override
    public boolean validateClass(SchoolClass schoolClass) {
        // 检查班级ID是否为空
        if (schoolClass.getClassId() == null || schoolClass.getClassId().trim().isEmpty()) {
            System.out.println("班级ID为空");
            return false;
        }

        // 检查班级名称是否为空
        if (schoolClass.getClassName() == null || schoolClass.getClassName().trim().isEmpty()) {
            System.out.println("班级名称为空");
            return false;
        }

        // 检查班级ID格式（可根据实际需求调整）
        if (!schoolClass.getClassId().matches("[A-Za-z0-9]+")) {
            System.out.println("班级ID格式不正确: " + schoolClass.getClassId());
            return false;
        }

        // 检查班级名称长度（可根据实际需求调整）
        if (schoolClass.getClassName().length() > 50) {
            System.out.println("班级名称过长: " + schoolClass.getClassName());
            return false;
        }

        return true;
    }

    /**
     * 检查班级是否存在 - ISchoolClassService接口方法
     */
    @Override
    public boolean classExists(String classId) {
        return exists(classId);
    }

    /**
     * 检查班级名称是否已存在 - ISchoolClassService接口方法
     */
    @Override
    public boolean classNameExists(String className) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                List<SchoolClass> classes = schoolClassDao.findByClassName(className, conn);
                return classes != null && !classes.isEmpty();
            });
        } catch (Exception e) {
            handleException("检查班级名称存在性失败", e);
            return false;
        }
    }
}