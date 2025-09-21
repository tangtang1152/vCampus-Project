package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Choose;
import com.vCampus.entity.Subject;
import com.vCampus.net.HttpClientUtil;
import com.vCampus.util.TransactionManager;

import java.sql.Connection;
import java.util.List;

/**
 * 选课服务实现类（客户端CS）：HTTP调用服务端
 */
public class ChooseServiceImpl extends AbstractBaseServiceImpl<Choose, String> implements IChooseService {

    // 实现抽象方法
    @Override
    protected Choose doGetBySelfId(String selectid, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/chooses/" + selectid);
        return HttpClientUtil.parseObject(data, Choose.class);
    }

    @Override
    protected java.util.List<Choose> doGetAll(Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/chooses");
        return HttpClientUtil.parseList(data, Choose.class);
    }

    @Override
    protected boolean doAdd(Choose choose, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPost("/chooses", choose);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doUpdate(Choose choose, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendPut("/chooses/" + choose.getSelectid(), choose);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doDelete(String selectid, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendDelete("/chooses/" + selectid);
        return data.asBoolean(false);
    }

    @Override
    protected boolean doExists(String selectid, Connection conn) throws Exception {
        JsonNode data = HttpClientUtil.sendGet("/chooses/" + selectid);
        return !data.isNull();
    }

    @Override
    public boolean chooseSubject(String studentId, String subjectId) {
        return TransactionManager.executeInTransaction(c -> {
            var body = new java.util.HashMap<String, String>();
            body.put("studentId", studentId);
            body.put("subjectId", subjectId);
            try {
                // HttpClientUtil.sendPost 已经从服务器的 ApiResponse 中提取了 "data" 字段。
                // 根据服务端 ChooseController 的定义，/chooses/choose 接口的 "data" 字段直接是一个布尔值。
                JsonNode rootDataNode = HttpClientUtil.sendPost("/chooses/choose", body);

                // *** 核心修改：直接将返回的 JsonNode 解析为 boolean ***
                boolean success = rootDataNode.asBoolean(false); // 使用 asBoolean(defaultValue) 更加健壮

                // 您可以添加调试输出，以确认是否成功获取到布尔值
                System.out.println("选课API响应（data字段解析为布尔值）: " + success);

                return success; // 直接返回布尔结果
            } catch (Exception e) {
                System.err.println("选课失败，客户端捕获异常: " + e.getMessage());
                e.printStackTrace();
                return false; // 发生异常，也返回false
            }
        });
    }
    
    @Override
    public boolean adminAssistChooseSubject(String studentId, String subjectId, boolean ignoreTimeConflict) {
        return TransactionManager.executeInTransaction(c -> {
            var body = new java.util.HashMap<String, Object>();
            body.put("studentId", studentId);
            body.put("subjectId", subjectId);
            body.put("ignoreTimeConflict", ignoreTimeConflict);
            JsonNode data = HttpClientUtil.sendPost("/chooses/admin-choose", body);
            // 根据服务端 ChooseController，/admin-choose 也返回 ApiResponse<Boolean>
            return data.asBoolean(false);
        });
    }

    @Override
    public boolean dropSubject(String selectid) {
        return TransactionManager.executeInTransaction(c -> {
            JsonNode data = HttpClientUtil.sendPost("/chooses/drop/" + selectid, java.util.Map.of());
            // 根据服务端 ChooseController，/drop/{selectid} 也返回 ApiResponse<Boolean>
            return data.asBoolean(false);
        });
    }

    @Override
    public java.util.List<Subject> getStudentSubjects(String studentId) {
        return TransactionManager.executeInTransaction(c -> {
            JsonNode data = HttpClientUtil.sendGet("/chooses/by-student/" + studentId);
            return HttpClientUtil.parseList(data, Subject.class);
        });
    }

    @Override
    public java.util.List<Choose> getSubjectChooses(String subjectId) {
        return TransactionManager.executeInTransaction(c -> {
            JsonNode data = HttpClientUtil.sendGet("/chooses/by-subject/" + subjectId);
            return HttpClientUtil.parseList(data, Choose.class);
        });
    }

    @Override
    public boolean isSubjectChosen(String studentId, String subjectId) {
        return TransactionManager.executeInTransaction(c -> {
            JsonNode data = HttpClientUtil.sendGet("/chooses/is-chosen?studentId=" + studentId + "&subjectId=" + subjectId);
            // 根据服务端 ChooseController，/is-chosen 也返回 ApiResponse<Boolean>
            return data.asBoolean(false);
        });
    }

    @Override
    public Choose findByStudentAndSubject(String studentId, String subjectId) {
        return TransactionManager.executeInTransaction(c -> {
            JsonNode data = HttpClientUtil.sendGet("/chooses/find?studentId=" + studentId + "&subjectId=" + subjectId);
            // 根据服务端 ChooseController，/find 返回 ApiResponse<Choose>，所以这里解析为 Choose 对象是正确的
            return HttpClientUtil.parseObject(data, Choose.class);
        });
    }

    @Override
    public Choose getChooseDetail(String selectid) { return getBySelfId(selectid); }

    @Override
    public boolean validateChoose(Choose choose) {
        if (choose.getSelectid() == null || choose.getSelectid().trim().isEmpty()) return false;
        if (choose.getStudentId() == null || choose.getStudentId().trim().isEmpty()) return false;
        if (choose.getSubjectId() == null || choose.getSubjectId().trim().isEmpty()) return false;
        return true;
    }
}