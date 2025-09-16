package com.vCampus.service;

import com.vCampus.dao.IAdminDao;
import com.vCampus.dao.IUserDao;
import com.vCampus.dao.UserDaoImpl;
import com.vCampus.dao.AdminDaoImpl;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.util.TransactionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员服务实现类
 */
public class AdminServiceImpl implements IAdminService {
    
    private final IAdminDao adminDao = new AdminDaoImpl();
    private static final IUserDao userDao = new UserDaoImpl();
    
    @Override
    public Admin getAdminById(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.findByAdminId(adminId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取管理员信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Admin getAdminByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.findByUserId(userId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取管理员信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Admin> getAllAdmins() {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.findAll(conn)
            );
        } catch (RuntimeException e) {
            System.err.println("获取所有管理员失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addAdmin(Admin admin) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.insert(admin, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("添加管理员失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.update(admin, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("更新管理员信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateFullAdmin(Admin admin) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 更新用户基本信息
                boolean userUpdated = userDao.update(admin, conn);
                if (!userUpdated) {
                    System.out.println("更新用户信息失败");
                    return false;
                }
                
                // 更新管理员特定信息
                boolean adminUpdated = adminDao.update(admin, conn);
                if (!adminUpdated) {
                    System.out.println("更新管理员信息失败");
                    // 如果管理员信息更新失败，可以回滚用户更新
                    throw new RuntimeException("更新管理员信息失败，事务已回滚");
                }
                
                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("完整更新管理员信息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
       

    @Override
    public boolean deleteAdminInfoOnly(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.delete(adminId, conn)
            );
        } catch (RuntimeException e) {
            System.err.println("删除管理员失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteAdmin(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 首先获取管理员信息
                Admin admin = adminDao.findByAdminId(adminId, conn);
                if (admin == null) {
                    System.out.println("管理员不存在，管理员工号: " + adminId);
                    return false;
                }

                // 先删除管理员信息
                boolean adminDeleted = adminDao.delete(adminId, conn);
                if (!adminDeleted) {
                    System.out.println("删除管理员信息失败");
                    return false;
                }

                // 再删除用户账户
                boolean userDeleted = userDao.delete(admin.getUserId(), conn);
                if (!userDeleted) {
                    System.out.println("删除用户信息失败");
                    // 如果用户删除失败，回滚管理员删除操作
                    throw new RuntimeException("删除用户信息失败，事务已回滚");
                }

                return true;
            });
        } catch (RuntimeException e) {
            System.err.println("删除管理员失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isAdminIdExists(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> 
                adminDao.findByAdminId(adminId, conn) != null
            );
        } catch (RuntimeException e) {
            System.err.println("检查管理员工号是否存在失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}