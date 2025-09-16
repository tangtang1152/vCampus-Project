package com.vCampus.service;

import com.vCampus.dao.IAdminDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.AdminDaoImpl;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.entity.Admin;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 管理员服务实现类
 */
public class AdminServiceImpl 
    extends AbstractBaseServiceImpl<Admin, String> implements IAdminService {

    private final IAdminDao adminDao = new AdminDaoImpl();
    private final IUserDao userDao = new UserDaoImpl();
    
    // 实现抽象方法（现在有Connection参数）
    @Override
    protected Admin doGetBySelfId(String adminId, Connection conn) throws Exception {
        return adminDao.findByAdminId(adminId, conn);
    }

    @Override
    protected List<Admin> doGetAll(Connection conn) throws Exception {
        return adminDao.findAll(conn);
    }

    @Override
    protected boolean doAdd(Admin admin, Connection conn) throws Exception {
        return adminDao.insert(admin, conn);
    }

    @Override
    protected boolean doUpdate(Admin admin, Connection conn) throws Exception {
        return adminDao.update(admin, conn);
    }

    @Override
    protected boolean doDelete(String adminId, Connection conn) throws Exception {
        Admin admin = adminDao.findByAdminId(adminId, conn);
        if (admin == null) return false;
        
        boolean adminDeleted = adminDao.delete(adminId, conn);
        if (!adminDeleted) return false;
        
        return userDao.delete(admin.getUserId(), conn);
    }

    @Override
    protected boolean doExists(String adminId, Connection conn) throws Exception {
        return adminDao.findByAdminId(adminId, conn) != null;
    }

    // 实现特定方法
    @Override
    public Admin getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.findByUserId(userId, conn)
            );
        } catch (Exception e) {
            handleException("根据用户ID获取管理员失败", e);
            return null;
        }
    }

    @Override
    public Admin getAdminFull(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                Admin admin = adminDao.findByAdminId(adminId, conn);
                if (admin != null) {
                    var user = userDao.findById(admin.getUserId(), conn);
                    if (user != null) {
                        admin.setUsername(user.getUsername());
                        admin.setPassword(user.getPassword());
                        admin.setRole(user.getRole());
                    }
                }
                return admin;
            });
        } catch (Exception e) {
            handleException("获取完整管理员信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateAdminOnly(Admin admin) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.update(admin, conn)
            );
        } catch (Exception e) {
            handleException("更新管理员信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteAdminOnly(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn ->
                adminDao.delete(adminId, conn)
            );
        } catch (Exception e) {
            handleException("删除管理员信息失败", e);
            return false;
        }
    }
}