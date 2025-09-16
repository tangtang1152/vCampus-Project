package com.vCampus.service;

import com.vCampus.entity.Admin;

public interface IAdminService extends IBaseService<Admin, String> {
    
    Admin getByUserId(Integer userId);
    
    // 也输出tbl_user对应记录
    Admin getAdminFull(String adminId);
    
    // 只更新tbl_admin
    boolean updateAdminOnly(Admin admin);
    
    // 只删tbl_admin
    boolean deleteAdminOnly(String adminId);

}