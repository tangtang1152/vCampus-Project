package com.vCampus.net;

import com.vCampus.net.dto.SocketRequest;
import com.vCampus.net.dto.SocketResponse;
import com.vCampus.net.dto.ShopDtos;
import com.vCampus.service.IChooseService;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.service.ServiceResult;

/**
 * 将 SocketRequest 调度到具体业务服务并生成 SocketResponse。
 * 由服务器端复用一个实例处理所有请求。
 */
public class CourseRequestDispatcher {

    public interface ClientClosedCallback {
        void onClientClosed(String clientKey);
    }

    private final ClientClosedCallback clientClosedCallback;

    public CourseRequestDispatcher(ClientClosedCallback clientClosedCallback) {
        this.clientClosedCallback = clientClosedCallback;
    }

    public SocketResponse handle(SocketRequest req) {
        String action = req.getAction();
        if (action == null) {
            return new SocketResponse(false, "缺少 action");
        }
        switch (action.toUpperCase()) {
            case "USER_LIST":
                return handleUserList(req);
            case "USER_DELETE":
                return handleUserDelete(req);
            case "USER_ADD":
                return handleUserAdd(req);
            case "USER_UPDATE":
                return handleUserUpdate(req);
            case "STUDENT_LIST":
                return handleStudentList(req);
            case "STUDENT_ADD":
                return handleStudentAdd(req);
            case "STUDENT_UPDATE":
                return handleStudentUpdate(req);
            case "STUDENT_DELETE":
                return handleStudentDelete(req);
            case "TEACHER_LIST":
                return handleTeacherList(req);
            case "TEACHER_ADD":
                return handleTeacherAdd(req);
            case "TEACHER_UPDATE":
                return handleTeacherUpdate(req);
            case "TEACHER_DELETE":
                return handleTeacherDelete(req);
            case "ADMIN_LIST":
                return handleAdminList(req);
            case "ADMIN_ADD":
                return handleAdminAdd(req);
            case "ADMIN_UPDATE":
                return handleAdminUpdate(req);
            case "ADMIN_DELETE":
                return handleAdminDelete(req);
            case "PING":
                return new SocketResponse(true, "PONG");
            case "CHOOSE":
                return handleChoose(req);
            case "BORROW":
                return handleBorrow(req);
            case "RENEW":
                return handleRenew(req);
            case "RETURN":
                return handleReturn(req);
            case "RESERVE":
                return handleReserve(req);
            // ===== Shop over Socket =====
            case "SHOP_LIST":
                return handleShopList(req);
            case "SHOP_CREATE_ORDER":
                return handleShopCreateOrder(req);
            case "SHOP_PAY":
                return handleShopPay(req);
            case "SHOP_ADD":
                return handleShopAdd(req);
            case "SHOP_UPDATE":
                return handleShopUpdate(req);
            case "SHOP_DELETE":
                return handleShopDelete(req);
            case "SUBJECT_LIST":
                return handleSubjectList(req);
            case "MY_SUBJECTS":
                return handleMySubjects(req);
            case "LIB_LIST":
                return handleLibList(req);
            case "LIB_MY_BORROWS":
                return handleLibMyBorrows(req);
            case "LIB_ADD":
                return handleLibAdd(req);
            case "LIB_UPDATE":
                return handleLibUpdate(req);
            case "LIB_DELETE":
                return handleLibDelete(req);
            case "LIB_SET_STATUS":
                return handleLibSetStatus(req);
            case "LIB_PAY_FINE":
                return handleLibPayFine(req);
            case "LIB_PAY_FINE_FOR_RECORD":
                return handleLibPayFineForRecord(req);
            case "USER_LOGIN":
                return handleUserLogin(req);
            case "USER_REGISTER":
                return handleUserRegister(req);
            default:
                return new SocketResponse(false, "未知指令: " + action);
        }
    }

    private SocketResponse handleUserLogin(SocketRequest req) {
        String username = req.getParam("username");
        String password = req.getParam("password");
        if (isBlank(username) || isBlank(password)) return new SocketResponse(false, "参数不足");
        var u = ServiceFactory.getUserService().login(username, password);
        if (u == null) return new SocketResponse(false, "用户名或密码错误");
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("userId", u.getUserId());
        data.put("username", u.getUsername());
        java.util.Set<String> roles = u.getRoleSet();
        data.put("roles", roles);
        data.put("activeRole", u.getPrimaryRole());
        try {
            if (roles != null && roles.contains("STUDENT")) {
                var stu = ServiceFactory.getStudentService().getByUserId(u.getUserId());
                if (stu != null) {
                    data.put("studentId", stu.getStudentId());
                    data.put("studentName", stu.getStudentName());
                    data.put("className", stu.getClassName());
                }
            }
        } catch (Exception ignored) {}
        return new SocketResponse(true, "OK", (java.io.Serializable) data);
    }

    private SocketResponse handleUserRegister(SocketRequest req) {
        String username = req.getParam("username");
        String password = req.getParam("password");
        String role = req.getParam("role"); // STUDENT/TEACHER/ADMIN
        String studentId = req.getParam("studentId");
        String studentName = req.getParam("studentName");
        String className = req.getParam("className");
        if (isBlank(username) || isBlank(password) || isBlank(role)) return new SocketResponse(false, "参数不足");
        var svc = ServiceFactory.getUserService();
        if (svc.isUsernameExists(username)) return new SocketResponse(false, "用户名已存在");
        com.vCampus.entity.User user = new com.vCampus.entity.User();
        user.setUsername(username); user.setPassword(password); user.setRole(role);
        var res = svc.register(user);
        if (res != com.vCampus.service.IUserService.RegisterResult.SUCCESS) return new SocketResponse(false, res.getMessage());
        int newUserId = svc.getByUsername(username).getUserId();
        // 若是学生且携带学号，必须提供班级；否则不允许注册
        if ("STUDENT".equalsIgnoreCase(role) && studentId != null && !studentId.isBlank()) {
            try {
                var stuSvc = ServiceFactory.getStudentService();
                com.vCampus.entity.Student s = new com.vCampus.entity.Student();
                s.setStudentId(studentId.trim());
                s.setStudentName((studentName == null || studentName.isBlank()) ? username : studentName.trim());
                if (className == null || className.isBlank()) {
                    return new SocketResponse(false, "班级不能为空");
                }
                s.setClassName(className.trim());
                s.setUserId(svc.getByUsername(username).getUserId());
                // IStudentService 继承了 IBaseService，新增学生使用 add()
                stuSvc.add(s);
            } catch (Exception e) {
                return new SocketResponse(false, "注册成功但创建学生档案失败: " + e.getMessage());
            }
        }
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("userId", newUserId);
        return new SocketResponse(true, "注册成功", (java.io.Serializable) data);
    }

    // ===== User management over socket =====
    private SocketResponse handleUserList(SocketRequest req) {
        var userSvc = ServiceFactory.getUserService();
        var stuSvc = ServiceFactory.getStudentService();
        var tchSvc = ServiceFactory.getTeacherService();
        var admSvc = ServiceFactory.getAdminService();
        java.util.List<com.vCampus.entity.User> list = userSvc.getAll();
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var u : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("userId", u.getUserId());
            m.put("username", u.getUsername());
            m.put("roles", new java.util.ArrayList<>(u.getRoleSet()));
            // 附带 realName/typeInfo
            String realName = ""; String typeInfo = "";
            try { var s = stuSvc.getByUserId(u.getUserId()); if (s != null) { realName = s.getStudentName()==null?"":s.getStudentName(); typeInfo = "班级:" + (s.getClassName()==null?"":s.getClassName()); } } catch (Exception ignored) {}
            try { var t = tchSvc.getByUserId(u.getUserId()); if (t != null) { realName = t.getTeacherName()==null?realName:t.getTeacherName(); if (!typeInfo.isEmpty()) typeInfo += " "; typeInfo += "部门:" + (t.getDepartmentId()==null?"":t.getDepartmentId()); } } catch (Exception ignored) {}
            try { var a = admSvc.getByUserId(u.getUserId()); if (a != null) { realName = a.getAdminName()==null?realName:a.getAdminName(); if (!typeInfo.isEmpty()) typeInfo += " "; typeInfo += "工号:" + (a.getAdminId()==null?"":a.getAdminId()); } } catch (Exception ignored) {}
            m.put("realName", realName);
            m.put("typeInfo", typeInfo);
            rows.add(m);
        }
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) data);
    }

    private SocketResponse handleUserDelete(SocketRequest req) {
        Integer userId = parseInt(req.getParam("userId"));
        if (userId == null) return new SocketResponse(false, "参数不足");
        try {
            var userSvc = ServiceFactory.getUserService();
            var stuSvc = ServiceFactory.getStudentService();
            var tchSvc = ServiceFactory.getTeacherService();
            var admSvc = ServiceFactory.getAdminService();
            var u = userSvc.getBySelfId(userId);
            if (u == null) return new SocketResponse(false, "用户不存在");
            java.util.Set<String> rs = u.getRoleSet();
            if (rs.contains("STUDENT")) { var s = stuSvc.getByUserId(userId); if (s != null) stuSvc.deleteStudentOnly(s.getStudentId()); }
            if (rs.contains("TEACHER")) { var t = tchSvc.getByUserId(userId); if (t != null) tchSvc.deleteTeacherOnly(t.getTeacherId()); }
            if (rs.contains("ADMIN")) { var a = admSvc.getByUserId(userId); if (a != null) admSvc.deleteAdminOnly(a.getAdminId()); }
            boolean ok = userSvc.delete(userId);
            return new SocketResponse(ok, ok?"删除成功":"删除失败");
        } catch (Exception e) {
            return new SocketResponse(false, e.getMessage());
        }
    }

    private SocketResponse handleUserAdd(SocketRequest req) {
        String username = req.getParam("username");
        String password = req.getParam("password");
        String role = req.getParam("role"); // STUDENT/TEACHER/ADMIN
        if (isBlank(username) || isBlank(role)) return new SocketResponse(false, "参数不足");
        var svc = ServiceFactory.getUserService();
        if (svc.isUsernameExists(username)) return new SocketResponse(false, "用户名已存在");
        com.vCampus.entity.User u = new com.vCampus.entity.User();
        u.setUsername(username); u.setPassword(password==null?"123456":password); u.setRole(role);
        var res = svc.register(u);
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        if (res == com.vCampus.service.IUserService.RegisterResult.SUCCESS) {
            data.put("userId", svc.getByUsername(username).getUserId());
            return new SocketResponse(true, "新增成功", (java.io.Serializable) data);
        }
        return new SocketResponse(false, res.getMessage());
    }

    private SocketResponse handleUserUpdate(SocketRequest req) {
        Integer userId = parseInt(req.getParam("userId"));
        if (userId == null) return new SocketResponse(false, "参数不足");
        var svc = ServiceFactory.getUserService();
        var u = svc.getBySelfId(userId);
        if (u == null) return new SocketResponse(false, "用户不存在");
        if (req.getParam("username") != null) u.setUsername(req.getParam("username"));
        if (req.getParam("password") != null && !req.getParam("password").isBlank()) u.setPassword(req.getParam("password"));
        if (req.getParam("roles") != null) {
            java.util.LinkedHashSet<String> rs = new java.util.LinkedHashSet<>();
            for (String p : req.getParam("roles").split(",")) rs.add(p.trim());
            u.setRoleSet(rs);
        }
        boolean ok = svc.update(u);
        return new SocketResponse(ok, ok?"保存成功":"保存失败");
    }

    // ===== Student management over socket =====
    private SocketResponse handleStudentList(SocketRequest req) {
        var svc = ServiceFactory.getStudentService();
        java.util.List<com.vCampus.entity.Student> list = svc.getAll();
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var s : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("studentId", s.getStudentId());
            m.put("studentName", s.getStudentName());
            m.put("className", s.getClassName());
            m.put("sex", s.getSex());
            m.put("enrollDate", s.getEnrollDate());
            m.put("email", s.getEmail());
            m.put("idCard", s.getIdCard());
            m.put("status", s.getStatus());
            rows.add(m);
        }
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) data);
    }

    private SocketResponse handleStudentAdd(SocketRequest req) {
        var svc = ServiceFactory.getStudentService();
        com.vCampus.entity.Student s = new com.vCampus.entity.Student();
        s.setStudentId(req.getParam("studentId"));
        s.setStudentName(req.getParam("studentName"));
        s.setClassName(req.getParam("className"));
        s.setSex(req.getParam("sex"));
        s.setEmail(req.getParam("email"));
        s.setIdCard(req.getParam("idCard"));
        s.setStatus(req.getParam("status"));
        try { String uid = req.getParam("userId"); if (uid != null && !uid.isBlank()) s.setUserId(Integer.parseInt(uid)); } catch (Exception ignored) {}
        boolean ok = svc.add(s);
        return new SocketResponse(ok, ok?"新增成功":"新增失败");
    }

    private SocketResponse handleStudentUpdate(SocketRequest req) {
        var svc = ServiceFactory.getStudentService();
        com.vCampus.entity.Student s = svc.getBySelfId(req.getParam("studentId"));
        if (s == null) return new SocketResponse(false, "学生不存在");
        if (req.getParam("studentName") != null) s.setStudentName(req.getParam("studentName"));
        if (req.getParam("className") != null) s.setClassName(req.getParam("className"));
        if (req.getParam("sex") != null) s.setSex(req.getParam("sex"));
        if (req.getParam("email") != null) s.setEmail(req.getParam("email"));
        if (req.getParam("idCard") != null) s.setIdCard(req.getParam("idCard"));
        if (req.getParam("status") != null) s.setStatus(req.getParam("status"));
        boolean ok = ServiceFactory.getStudentService().updateStudentOnly(s);
        return new SocketResponse(ok, ok?"保存成功":"保存失败");
    }

    private SocketResponse handleStudentDelete(SocketRequest req) {
        String sid = req.getParam("studentId");
        if (sid == null || sid.isBlank()) return new SocketResponse(false, "参数不足");
        boolean ok = ServiceFactory.getStudentService().deleteStudentOnly(sid);
        return new SocketResponse(ok, ok?"删除成功":"删除失败");
    }

    // ===== Teacher management over socket =====
    private SocketResponse handleTeacherList(SocketRequest req) {
        var svc = ServiceFactory.getTeacherService();
        java.util.List<com.vCampus.entity.Teacher> list = svc.getAll();
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var t : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("teacherId", t.getTeacherId());
            m.put("teacherName", t.getTeacherName());
            m.put("sex", t.getSex());
            m.put("technical", t.getTechnical());
            m.put("departmentId", t.getDepartmentId());
            rows.add(m);
        }
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) data);
    }

    private SocketResponse handleTeacherAdd(SocketRequest req) {
        var svc = ServiceFactory.getTeacherService();
        com.vCampus.entity.Teacher t = new com.vCampus.entity.Teacher();
        t.setTeacherId(req.getParam("teacherId"));
        t.setTeacherName(req.getParam("teacherName"));
        t.setSex(req.getParam("sex"));
        t.setTechnical(req.getParam("technical"));
        t.setDepartmentId(req.getParam("departmentId"));
        try { String uid = req.getParam("userId"); if (uid != null && !uid.isBlank()) t.setUserId(Integer.parseInt(uid)); } catch (Exception ignored) {}
        boolean ok = svc.add(t);
        return new SocketResponse(ok, ok?"新增成功":"新增失败");
    }

    private SocketResponse handleTeacherUpdate(SocketRequest req) {
        var svc = ServiceFactory.getTeacherService();
        com.vCampus.entity.Teacher t = svc.getBySelfId(req.getParam("teacherId"));
        if (t == null) return new SocketResponse(false, "教师不存在");
        if (req.getParam("teacherName") != null) t.setTeacherName(req.getParam("teacherName"));
        if (req.getParam("sex") != null) t.setSex(req.getParam("sex"));
        if (req.getParam("technical") != null) t.setTechnical(req.getParam("technical"));
        if (req.getParam("departmentId") != null) t.setDepartmentId(req.getParam("departmentId"));
        boolean ok = ServiceFactory.getTeacherService().updateTeacherOnly(t);
        return new SocketResponse(ok, ok?"保存成功":"保存失败");
    }

    private SocketResponse handleTeacherDelete(SocketRequest req) {
        String tid = req.getParam("teacherId");
        if (tid == null || tid.isBlank()) return new SocketResponse(false, "参数不足");
        boolean ok = ServiceFactory.getTeacherService().deleteTeacherOnly(tid);
        return new SocketResponse(ok, ok?"删除成功":"删除失败");
    }

    // ===== Admin management over socket =====
    private SocketResponse handleAdminList(SocketRequest req) {
        var svc = ServiceFactory.getAdminService();
        java.util.List<com.vCampus.entity.Admin> list = svc.getAll();
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var a : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("adminId", a.getAdminId());
            m.put("adminName", a.getAdminName());
            rows.add(m);
        }
        java.util.Map<String,Object> data = new java.util.HashMap<>();
        data.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) data);
    }

    private SocketResponse handleAdminAdd(SocketRequest req) {
        var svc = ServiceFactory.getAdminService();
        com.vCampus.entity.Admin a = new com.vCampus.entity.Admin();
        a.setAdminId(req.getParam("adminId"));
        a.setAdminName(req.getParam("adminName"));
        try { String uid = req.getParam("userId"); if (uid != null && !uid.isBlank()) a.setUserId(Integer.parseInt(uid)); } catch (Exception ignored) {}
        boolean ok = svc.add(a);
        return new SocketResponse(ok, ok?"新增成功":"新增失败");
    }

    private SocketResponse handleAdminUpdate(SocketRequest req) {
        var svc = ServiceFactory.getAdminService();
        com.vCampus.entity.Admin a = svc.getBySelfId(req.getParam("adminId"));
        if (a == null) return new SocketResponse(false, "管理员不存在");
        if (req.getParam("adminName") != null) a.setAdminName(req.getParam("adminName"));
        boolean ok = ServiceFactory.getAdminService().updateAdminOnly(a);
        return new SocketResponse(ok, ok?"保存成功":"保存失败");
    }

    private SocketResponse handleAdminDelete(SocketRequest req) {
        String aid = req.getParam("adminId");
        if (aid == null || aid.isBlank()) return new SocketResponse(false, "参数不足");
        boolean ok = ServiceFactory.getAdminService().deleteAdminOnly(aid);
        return new SocketResponse(ok, ok?"删除成功":"删除失败");
    }

    public void onClientClosed(String clientKey) {
        if (clientClosedCallback != null) {
            clientClosedCallback.onClientClosed(clientKey);
        }
    }

    private SocketResponse handleChoose(SocketRequest req) {
        String studentId = req.getParam("studentId");
        String subjectId = req.getParam("subjectId");
        if (isBlank(studentId) || isBlank(subjectId)) {
            return new SocketResponse(false, "参数不足");
        }
        IChooseService chooseService = ServiceFactory.getChooseService();
        // 若实现类支持带原因的接口，则优先走它
        if (chooseService instanceof com.vCampus.service.ChooseServiceImpl impl) {
            var res = impl.chooseSubjectWithReason(studentId, subjectId);
            return new SocketResponse(res.isSuccess(), res.getMessage());
        }
        boolean ok = chooseService.chooseSubject(studentId, subjectId);
        return new SocketResponse(ok, ok ? "选课成功" : "选课失败：可能已满或已选过");
    }

    private SocketResponse handleBorrow(SocketRequest req) {
        String userId = req.getParam("userId");
        Integer bookId = parseInt(req.getParam("bookId"));
        int days = parseIntOrDefault(req.getParam("days"), 30);
        if (isBlank(userId) || bookId == null) {
            return new SocketResponse(false, "参数不足");
        }
        LibraryService lib = ServiceFactory.getLibraryService();
        ServiceResult res = lib.borrowBookWithReason(userId, bookId, days);
        com.vCampus.util.AuditLogger.log("BORROW", String.format("user=%s bookId=%s result=%s msg=%s", userId, String.valueOf(bookId), String.valueOf(res==null?false:res.isSuccess()), String.valueOf(res==null?"":res.getMessage())));
        return new SocketResponse(res != null && res.isSuccess(), res == null ? "操作失败" : res.getMessage());
    }

    private SocketResponse handleRenew(SocketRequest req) {
        String userId = req.getParam("userId");
        Integer recordId = parseInt(req.getParam("recordId"));
        int days = parseIntOrDefault(req.getParam("days"), 30);
        if (isBlank(userId) || recordId == null) {
            return new SocketResponse(false, "参数不足");
        }
        LibraryService lib = ServiceFactory.getLibraryService();
        ServiceResult res = lib.renewBorrowWithReason(userId, recordId, days, 1);
        com.vCampus.util.AuditLogger.log("RENEW", String.format("user=%s recordId=%s days=%d result=%s msg=%s", userId, String.valueOf(recordId), days, String.valueOf(res==null?false:res.isSuccess()), String.valueOf(res==null?"":res.getMessage())));
        return new SocketResponse(res != null && res.isSuccess(), res == null ? "操作失败" : res.getMessage());
    }

    private SocketResponse handleReturn(SocketRequest req) {
        String userId = req.getParam("userId");
        Integer recordId = parseInt(req.getParam("recordId"));
        Integer bookId = parseInt(req.getParam("bookId"));
        if (isBlank(userId) || recordId == null || bookId == null) {
            return new SocketResponse(false, "参数不足");
        }
        LibraryService lib = ServiceFactory.getLibraryService();
        ServiceResult res = lib.returnBookWithReason(userId, recordId, bookId);
        com.vCampus.util.AuditLogger.log("RETURN", String.format("user=%s recordId=%s bookId=%s result=%s msg=%s", userId, String.valueOf(recordId), String.valueOf(bookId), String.valueOf(res==null?false:res.isSuccess()), String.valueOf(res==null?"":res.getMessage())));
        return new SocketResponse(res != null && res.isSuccess(), res == null ? "操作失败" : res.getMessage());
    }

    private SocketResponse handleReserve(SocketRequest req) {
        String userId = req.getParam("userId");
        Integer bookId = parseInt(req.getParam("bookId"));
        if (isBlank(userId) || bookId == null) {
            return new SocketResponse(false, "参数不足");
        }
        LibraryService lib = ServiceFactory.getLibraryService();
        ServiceResult res = lib.reserveBookWithReason(userId, bookId);
        com.vCampus.util.AuditLogger.log("RESERVE", String.format("user=%s bookId=%s result=%s msg=%s", userId, String.valueOf(bookId), String.valueOf(res==null?false:res.isSuccess()), String.valueOf(res==null?"":res.getMessage())));
        return new SocketResponse(res != null && res.isSuccess(), res == null ? "操作失败" : res.getMessage());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Integer parseInt(String s) {
        try { return s == null ? null : Integer.valueOf(s); } catch (Exception e) { return null; }
    }

    private int parseIntOrDefault(String s, int defVal) {
        try { return s == null ? defVal : Integer.parseInt(s); } catch (Exception e) { return defVal; }
    }

    // ================= Shop handlers =================
    private SocketResponse handleShopList(SocketRequest req) {
        String category = req.getParam("category");
        String keyword = req.getParam("keyword");
        var svc = ServiceFactory.getProductService();
        java.util.List<com.vCampus.entity.Product> list;
        if (keyword != null && !keyword.isBlank()) list = svc.searchProductsByName(keyword);
        else if (category != null && !category.isBlank() && !"全部".equals(category)) list = svc.getProductsByCategory(category);
        else list = svc.getAllProducts();
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var p : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("productId", p.getProductId());
            m.put("productName", p.getProductName());
            m.put("price", p.getPrice());
            m.put("stock", p.getStock());
            m.put("category", p.getCategory());
            m.put("description", p.getDescription());
            rows.add(m);
        }
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    private SocketResponse handleShopCreateOrder(SocketRequest req) {
        Object payload = req.getPayload();
        if (!(payload instanceof ShopDtos.CreateOrderReqDTO dto)) return new SocketResponse(false, "非法请求体");
        var svc = ServiceFactory.getShopService();
        java.util.List<com.vCampus.entity.OrderItem> items = new java.util.ArrayList<>();
        for (ShopDtos.OrderItemDTO it : dto.items) {
            com.vCampus.entity.OrderItem oi = new com.vCampus.entity.OrderItem();
            oi.setProductId(it.productId);
            oi.setQuantity(it.quantity);
            items.add(oi);
        }
        String orderId = svc.purchase(dto.studentId, items);
        if (orderId == null) return new SocketResponse(false, "库存不足或商品不存在");
        ShopDtos.CreateOrderRespDTO resp = new ShopDtos.CreateOrderRespDTO();
        resp.orderId = orderId;
        resp.totalAmount = svc.calculateCartTotal(items);
        resp.status = "待支付";
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("order", resp);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    private SocketResponse handleShopPay(SocketRequest req) {
        String orderId = req.getParam("orderId");
        if (orderId == null || orderId.isBlank()) return new SocketResponse(false, "orderId 不能为空");
        boolean ok = ServiceFactory.getShopService().payOrder(orderId);
        return new SocketResponse(ok, ok ? "支付成功" : "订单不存在或状态异常");
    }

    private SocketResponse handleShopAdd(SocketRequest req) {
        var svc = ServiceFactory.getProductService();
        com.vCampus.entity.Product p = new com.vCampus.entity.Product();
        p.setProductId(req.getParam("productId"));
        p.setProductName(req.getParam("productName"));
        try { p.setPrice(Double.parseDouble(req.getParam("price"))); } catch (Exception ignored) {}
        try { p.setStock(Integer.parseInt(req.getParam("stock"))); } catch (Exception ignored) {}
        p.setCategory(req.getParam("category"));
        p.setDescription(req.getParam("description"));
        boolean ok = svc.addProduct(p);
        com.vCampus.util.AuditLogger.log("SHOP_ADD", String.format("productId=%s result=%s", p.getProductId(), ok));
        return new SocketResponse(ok, ok ? "新增成功" : "新增失败");
    }

    private SocketResponse handleShopUpdate(SocketRequest req) {
        var svc = ServiceFactory.getProductService();
        com.vCampus.entity.Product p = new com.vCampus.entity.Product();
        p.setProductId(req.getParam("productId"));
        p.setProductName(req.getParam("productName"));
        try { p.setPrice(Double.parseDouble(req.getParam("price"))); } catch (Exception ignored) {}
        try { p.setStock(Integer.parseInt(req.getParam("stock"))); } catch (Exception ignored) {}
        p.setCategory(req.getParam("category"));
        p.setDescription(req.getParam("description"));
        boolean ok = svc.updateProduct(p);
        com.vCampus.util.AuditLogger.log("SHOP_UPDATE", String.format("productId=%s result=%s", p.getProductId(), ok));
        return new SocketResponse(ok, ok ? "更新成功" : "更新失败");
    }

    private SocketResponse handleShopDelete(SocketRequest req) {
        var svc = ServiceFactory.getProductService();
        String pid = req.getParam("productId");
        boolean ok = svc.deleteProduct(pid);
        com.vCampus.util.AuditLogger.log("SHOP_DELETE", String.format("productId=%s result=%s", pid, ok));
        return new SocketResponse(ok, ok ? "删除成功" : "删除失败");
    }

    // ================= Subject list over socket =================
    private SocketResponse handleSubjectList(SocketRequest req) {
        String keyword = req.getParam("keyword");
        var svc = ServiceFactory.getSubjectService();
        java.util.List<com.vCampus.entity.Subject> list = (keyword == null || keyword.isBlank())
                ? svc.getAllSubjects()
                : svc.getSubjectsByName(keyword);
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        for (var s : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("subjectId", s.getSubjectId());
            m.put("subjectName", s.getSubjectName());
            m.put("subjectDate", s.getSubjectDate());
            m.put("subjectNum", s.getSubjectNum());
            m.put("credit", s.getCredit());
            m.put("teacherId", s.getTeacherId());
            m.put("weekRange", s.getWeekRange());
            m.put("weekType", s.getWeekType());
            m.put("classTime", s.getClassTime());
            m.put("classroom", s.getClassroom());
            rows.add(m);
        }
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    private SocketResponse handleMySubjects(SocketRequest req) {
        String studentId = req.getParam("studentId");
        if (isBlank(studentId)) return new SocketResponse(false, "参数不足");
        var choose = ServiceFactory.getChooseService();
        java.util.List<com.vCampus.entity.Subject> list = choose.getStudentSubjects(studentId);
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var s : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("subjectId", s.getSubjectId());
            m.put("subjectName", s.getSubjectName());
            m.put("subjectDate", s.getSubjectDate());
            m.put("subjectNum", s.getSubjectNum());
            m.put("credit", s.getCredit());
            m.put("teacherId", s.getTeacherId());
            m.put("weekRange", s.getWeekRange());
            m.put("weekType", s.getWeekType());
            m.put("classTime", s.getClassTime());
            m.put("classroom", s.getClassroom());
            rows.add(m);
        }
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    // ================= Library list over socket =================
    private SocketResponse handleLibList(SocketRequest req) {
        String keyword = req.getParam("keyword");
        String status = req.getParam("status");
        String sort = req.getParam("sort");
        int page = parseIntOrDefault(req.getParam("page"), 1);
        int size = parseIntOrDefault(req.getParam("size"), 10);
        var lib = ServiceFactory.getLibraryService();
        java.util.List<com.vCampus.entity.Book> list = lib.searchBooksAdvanced(keyword == null ? "" : keyword,
                status == null ? "全部" : status,
                sort == null ? "默认(最新)" : sort,
                page,
                size);
        int total = lib.countBooksAdvanced(keyword == null ? "" : keyword, status == null ? "全部" : status);
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var b : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("bookId", b.getBookId());
            m.put("title", b.getTitle());
            m.put("author", b.getAuthor());
            m.put("isbn", b.getIsbn());
            m.put("availableCopies", b.getAvailableCopies());
            m.put("status", b.getStatus());
            rows.add(m);
        }
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("rows", rows);
        map.put("total", total);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    private SocketResponse handleLibMyBorrows(SocketRequest req) {
        String userId = req.getParam("userId");
        String status = req.getParam("status");
        if (isBlank(userId)) return new SocketResponse(false, "参数不足");
        var lib = ServiceFactory.getLibraryService();
        java.util.List<com.vCampus.entity.BorrowRecord> list = lib.listMyBorrowsByStatus(userId, status == null ? "借出" : status);
        java.util.List<java.util.Map<String,Object>> rows = new java.util.ArrayList<>();
        for (var r : list) {
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            m.put("recordId", r.getRecordId());
            m.put("bookId", r.getBookId());
            try {
                com.vCampus.entity.Book b = ServiceFactory.getLibraryService().getBookById(r.getBookId());
                if (b != null) m.put("title", b.getTitle());
            } catch (Exception ignored) {}
            m.put("borrowDate", r.getBorrowDate());
            m.put("dueDate", r.getDueDate());
            m.put("returnDate", r.getReturnDate());
            m.put("status", r.getStatus());
            m.put("fine", r.getFine());
            m.put("renewTimes", r.getRenewTimes());
            rows.add(m);
        }
        java.util.Map<String,Object> map = new java.util.HashMap<>();
        map.put("rows", rows);
        return new SocketResponse(true, "OK", (java.io.Serializable) map);
    }

    private SocketResponse handleLibAdd(SocketRequest req) {
        var lib = ServiceFactory.getLibraryService();
        com.vCampus.entity.Book b = new com.vCampus.entity.Book();
        b.setIsbn(req.getParam("isbn"));
        b.setTitle(req.getParam("title"));
        b.setAuthor(req.getParam("author"));
        b.setCategory(req.getParam("category"));
        b.setPublisher(req.getParam("publisher"));
        try { b.setPubDate(java.sql.Date.valueOf(req.getParam("pubDate"))); } catch (Exception ignored) {}
        try { b.setTotalCopies(Integer.valueOf(req.getParam("totalCopies"))); } catch (Exception ignored) {}
        try { b.setAvailableCopies(Integer.valueOf(req.getParam("availableCopies"))); } catch (Exception ignored) {}
        b.setLocation(req.getParam("location"));
        b.setStatus(req.getParam("status"));
        boolean ok = lib.addBook(b);
        com.vCampus.util.AuditLogger.log("LIB_ADD", String.format("isbn=%s title=%s result=%s", b.getIsbn(), b.getTitle(), ok));
        return new SocketResponse(ok, ok ? "新增成功" : "新增失败");
    }

    private SocketResponse handleLibUpdate(SocketRequest req) {
        var lib = ServiceFactory.getLibraryService();
        com.vCampus.entity.Book b = new com.vCampus.entity.Book();
        try { b.setBookId(Integer.valueOf(req.getParam("bookId"))); } catch (Exception ignored) {}
        b.setIsbn(req.getParam("isbn"));
        b.setTitle(req.getParam("title"));
        b.setAuthor(req.getParam("author"));
        b.setCategory(req.getParam("category"));
        b.setPublisher(req.getParam("publisher"));
        try { b.setPubDate(java.sql.Date.valueOf(req.getParam("pubDate"))); } catch (Exception ignored) {}
        try { b.setTotalCopies(Integer.valueOf(req.getParam("totalCopies"))); } catch (Exception ignored) {}
        try { b.setAvailableCopies(Integer.valueOf(req.getParam("availableCopies"))); } catch (Exception ignored) {}
        b.setLocation(req.getParam("location"));
        b.setStatus(req.getParam("status"));
        boolean ok = lib.updateBook(b);
        com.vCampus.util.AuditLogger.log("LIB_UPDATE", String.format("bookId=%s title=%s result=%s", String.valueOf(b.getBookId()), b.getTitle(), ok));
        return new SocketResponse(ok, ok ? "更新成功" : "更新失败");
    }

    private SocketResponse handleLibDelete(SocketRequest req) {
        var lib = ServiceFactory.getLibraryService();
        Integer id = parseInt(req.getParam("bookId"));
        if (id == null) return new SocketResponse(false, "bookId 不能为空");
        boolean ok = lib.deleteBook(id);
        com.vCampus.util.AuditLogger.log("LIB_DELETE", String.format("bookId=%s result=%s", String.valueOf(id), ok));
        return new SocketResponse(ok, ok ? "删除成功" : "删除失败");
    }

    private SocketResponse handleLibSetStatus(SocketRequest req) {
        var lib = ServiceFactory.getLibraryService();
        Integer id = parseInt(req.getParam("bookId"));
        String status = req.getParam("status");
        if (id == null || status == null) return new SocketResponse(false, "参数不足");
        boolean ok = lib.setBookStatus(id, status);
        com.vCampus.util.AuditLogger.log("LIB_SET_STATUS", String.format("bookId=%s status=%s result=%s", String.valueOf(id), status, ok));
        return new SocketResponse(ok, ok ? "状态已更新" : "状态更新失败");
    }

    private SocketResponse handleLibPayFine(SocketRequest req) {
        String userId = req.getParam("userId");
        if (isBlank(userId)) return new SocketResponse(false, "参数不足");
        var res = ServiceFactory.getLibraryService().payAllFines(userId);
        return new SocketResponse(res.isSuccess(), res.getMessage());
    }

    private SocketResponse handleLibPayFineForRecord(SocketRequest req) {
        String userId = req.getParam("userId");
        Integer recordId = parseInt(req.getParam("recordId"));
        if (isBlank(userId) || recordId == null) return new SocketResponse(false, "参数不足");
        var res = ServiceFactory.getLibraryService().payFineForRecord(userId, recordId);
        return new SocketResponse(res.isSuccess(), res.getMessage());
    }
}


