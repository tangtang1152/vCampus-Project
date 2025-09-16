package com.vCampus.service;

import com.vCampus.entity.Admin;
import java.util.List;

/**
 * 管理员服务接口
 * 提供管理员相关的业务逻辑操作
 */
public interface IAdminService {
    
    /**
     * 根据管理员工号获取管理员信息
     * @param adminId 管理员工号
     * @return 管理员对象，如果不存在返回null
     */
    Admin getAdminById(String adminId);
    
    /**
     * 根据用户ID获取管理员信息
     * @param userId 用户ID
     * @return 管理员对象，如果不存在返回null
     */
    Admin getAdminByUserId(Integer userId);
    
    /**
     * 获取所有管理员
     * @return 管理员列表
     */
    List<Admin> getAllAdmins();
    
    /**
     * 添加管理员
     * @param admin 管理员对象
     * @return 添加成功返回true，失败返回false
     */
    boolean addAdmin(Admin admin);
    
    /**
     * 更新管理员信息
     * @param admin 管理员对象
     * @return 更新成功返回true，失败返回false
     */
    boolean updateAdmin(Admin admin);
    
    boolean updateFullAdmin(Admin admin);
    
    /**
     * 删除管理员
     * @param adminId 管理员工号
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteAdmin(String adminId);
    
    boolean deleteAdminInfoOnly(String adminId);

    
    /**
     * 验证管理员工号是否已存在
     * @param adminId 管理员工号
     * @return 存在返回true，不存在返回false
     */
    boolean isAdminIdExists(String adminId);
}