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
        data.put("roles", u.getRoleSet());
        data.put("activeRole", u.getPrimaryRole());
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
        return new SocketResponse(true, "注册成功");
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


