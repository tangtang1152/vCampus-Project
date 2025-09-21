package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Admin;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 管理员服务实现类（客户端CS）：HTTP调用服务端
 */
public class AdminServiceImpl 
    extends AbstractBaseServiceImpl<Admin, String> implements IAdminService {

    @Override
    protected Admin doGetBySelfId(String adminId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/admins/" + adminId);
        return HttpClientUtil.parseObject(data, Admin.class);
    }

    @Override
    protected List<Admin> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/admins");
        return HttpClientUtil.parseList(data, Admin.class);
    }

    @Override
    protected boolean doAdd(Admin admin, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPost("/admins", admin);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(Admin admin, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPut("/admins/" + admin.getAdminId(), admin);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String adminId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/admins/" + adminId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String adminId, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/admins/exists/" + adminId);
        return data.asBoolean(false);
    }

    // 实现特定方法
    @Override
    public Admin getByUserId(Integer userId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/admins/by-user/" + userId);
                return HttpClientUtil.parseObject(data, Admin.class);
            });
        } catch (Exception e) {
            handleException("根据用户ID获取管理员失败", e);
            return null;
        }
    }

    @Override
    public Admin getAdminFull(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendGet("/admins/full/" + adminId);
                return HttpClientUtil.parseObject(data, Admin.class);
            });
        } catch (Exception e) {
            handleException("获取完整管理员信息失败", e);
            return null;
        }
    }

    @Override
    public boolean updateAdminOnly(Admin admin) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendPut("/admins/only/" + admin.getAdminId(), admin);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("更新管理员信息失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteAdminOnly(String adminId) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                JsonNode data = HttpClientUtil.sendDelete("/admins/only/" + adminId);
                return data.asBoolean(false);
            });
        } catch (Exception e) {
            handleException("删除管理员信息失败", e);
            return false;
        }
    }
}