package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.*;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;
import com.vCampus.util.ValidationService;

import java.util.List;

/**
 * 用户服务（客户端CS架构）：通过HTTP调用服务端REST接口
 */
public class UserServiceImpl extends AbstractBaseServiceImpl<User, Integer> implements IUserService {

    @Override
    protected User doGetBySelfId(Integer userId, java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/users/by-id/" + userId);
        return HttpClientUtil.parseObject(data, User.class);
    }

    @Override
    protected List<User> doGetAll(java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/users");
        return HttpClientUtil.parseList(data, User.class);
    }

    @Override
    protected boolean doAdd(User user, java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPost("/users", user);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(User user, java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPut("/users/" + user.getUserId(), user);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(Integer userId, java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/users/" + userId);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(Integer userId, java.sql.Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/users/exists/" + userId);
        return data.asBoolean(false);
    }

    @Override
    public User login(String username, String password) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/users/login?username=" + username + "&password=" + password, null);
            return HttpClientUtil.parseObject(data, User.class);
        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User getByUsername(String username) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/users/by-username/" + username);
            return HttpClientUtil.parseObject(data, User.class);
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateUser(String username, String password) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/users/validate", new User(){ { setUsername(username); setPassword(password);} });
            return data.asBoolean(false);
        } catch (Exception e) {
            handleException("验证用户失败", e);
            return false;
        }
    }

    @Override
    public boolean isUsernameExists(String username) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/users/exists/by-username/" + username);
            return data.asBoolean(false);
        } catch (Exception e) {
            System.err.println("检查用户名是否存在失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("oldPassword", oldPassword);
            payload.put("newPassword", newPassword);
            JsonNode data = HttpClientUtil.sendPost("/users/" + userId + "/change-password", payload);
            return data.asBoolean(false);
        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resetPassword(Integer userId, String newPassword) {
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("newPassword", newPassword);
            JsonNode data = HttpClientUtil.sendPost("/users/" + userId + "/reset-password", payload);
            return data.asBoolean(false);
        } catch (Exception e) {
            System.err.println("重置密码失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public RegisterResult register(User user) {
        try {
            if (user instanceof Student && !ValidationService.validateStudent((Student) user)) return RegisterResult.VALIDATION_FAILED;
            if (user instanceof Teacher && !ValidationService.validateTeacher((Teacher) user)) return RegisterResult.VALIDATION_FAILED;
            if (user instanceof Admin && !ValidationService.validateAdmin((Admin) user)) return RegisterResult.VALIDATION_FAILED;
            if (!(user instanceof Student) && !(user instanceof Teacher) && !(user instanceof Admin) && !ValidationService.validateUser(user)) return RegisterResult.VALIDATION_FAILED;

            JsonNode data = HttpClientUtil.sendPost("/users/register", user);
            String result = data.asText("DATABASE_ERROR");
            try { return RegisterResult.valueOf(result); } catch (Exception ignore) { return RegisterResult.DATABASE_ERROR; }
        } catch (Exception e) {
            handleException("注册失败", e);
            return RegisterResult.DATABASE_ERROR;
        }
    }
}
