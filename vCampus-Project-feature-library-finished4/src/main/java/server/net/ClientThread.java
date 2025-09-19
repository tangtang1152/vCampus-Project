package server.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vCampus.service.IUserService;
import com.vCampus.service.ISubjectService;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ITeacherService;
import com.vCampus.service.IAdminService;
import com.vCampus.service.IChooseService;
import com.vCampus.service.IShopService;
import com.vCampus.service.IProductService;
import com.vCampus.service.IOrderService;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceFactory;

import com.vCampus.entity.User;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Subject;
import com.vCampus.entity.Book;
import com.vCampus.entity.BorrowRecord;
import com.vCampus.entity.Product;
import com.vCampus.entity.Order;
import com.vCampus.entity.OrderItem;
import com.vCampus.entity.Choose;

import java.util.List;

public class ClientThread extends Thread {
    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true)) {

            // 1. 读取客户端请求
            String request = in.readLine();
            System.out.println("[SERVER] 收到请求: " + request);

            // 2. 处理请求
            String response = processRequest(request);//下面有这个函数

            // 3. 返回响应
            out.println(response);
            System.out.println("[SERVER] 返回响应: " + response);

        } catch (IOException e) {
            System.err.println("[SERVER] 客户端连接异常: " + e.getMessage());
        }
    }

    /**
     * 处理客户端请求
     * @param request 客户端请求字符串
     * @return 响应字符串
     */
    private String processRequest(String request) {
        if (request == null || request.trim().isEmpty()) {
            return "ERROR: 请求为空";
        }

        try {
            // 解析请求格式: "ACTION:参数1:参数2:..."
            String[] parts = request.split(":");
            if (parts.length < 1) {
                return "ERROR: 请求格式错误";
            }

            String action = parts[0].toUpperCase();
            
            switch (action) {
                case "LOGIN":
                    return handleLogin(parts);
                case "REGISTER":
                    return handleRegister(parts);
                case "LOGOUT":
                    return handleLogout(parts);
                case "GET_SUBJECTS":
                    return handleGetSubjects(parts);
                case "ADD_SUBJECT":
                    return handleAddSubject(parts);
                case "UPDATE_SUBJECT":
                    return handleUpdateSubject(parts);
                case "DELETE_SUBJECT":
                    return handleDeleteSubject(parts);
                case "CHECK_STUDENT":
                    return handleCheckStudent(parts);
                case "ADMIN_ASSIST_CHOOSE":
                    return handleAdminAssistChoose(parts);
                // 图书馆相关操作
                case "SEARCH_BOOKS":
                    return handleSearchBooks(parts);
                case "BORROW_BOOK":
                    return handleBorrowBook(parts);
                case "RETURN_BOOK":
                    return handleReturnBook(parts);
                case "RENEW_BOOK":
                    return handleRenewBook(parts);
                case "RESERVE_BOOK":
                    return handleReserveBook(parts);
                case "GET_MY_BORROWS":
                    return handleGetMyBorrows(parts);
                case "GET_MY_RESERVATIONS":
                    return handleGetMyReservations(parts);
                // 商店相关操作
                case "GET_ALL_PRODUCTS":
                    return handleGetAllProducts(parts);
                case "GET_PRODUCTS_BY_CATEGORY":
                    return handleGetProductsByCategory(parts);
                case "SEARCH_PRODUCTS":
                    return handleSearchProducts(parts);
                case "GET_PRODUCT_BY_ID":
                    return handleGetProductById(parts);
                case "GET_ALL_CATEGORIES":
                    return handleGetAllCategories(parts);
                case "PURCHASE":
                    return handlePurchase(parts);
                case "CREATE_ORDER":
                    return handleCreateOrder(parts);
                case "PAY_ORDER":
                    return handlePayOrder(parts);
                case "CANCEL_ORDER":
                    return handleCancelOrder(parts);
                case "GET_ORDER_HISTORY":
                    return handleGetOrderHistory(parts);
                case "GET_ORDER_DETAILS":
                    return handleGetOrderDetails(parts);
                case "CHECK_STOCK":
                    return handleCheckStock(parts);
                // 课程管理相关操作
                case "GET_SUBJECTS_BY_NAME":
                    return handleGetSubjectsByName(parts);
                case "GET_SUBJECT_CHOOSES":
                    return handleGetSubjectChooses(parts);
                case "VALIDATE_SUBJECT":
                    return handleValidateSubject(parts);
                case "GET_STUDENT_BY_ID":
                    return handleGetStudentById(parts);
                case "GET_STUDENT_BY_USER_ID":
                    return handleGetStudentByUserId(parts);
                case "GET_STUDENT_FULL":
                    return handleGetStudentFull(parts);
                case "GET_MY_SUBJECTS":
                    return handleGetMySubjects(parts);
                case "CHOOSE_SUBJECT":
                    return handleChooseSubject(parts);
                case "DROP_SUBJECT":
                    return handleDropSubject(parts);
                case "ADMIN_DROP_SUBJECT":
                    return handleAdminDropSubject(parts);
                // 图书管理相关操作
                case "ADD_BOOK":
                    return handleAddBook(parts);
                case "UPDATE_BOOK":
                    return handleUpdateBook(parts);
                case "DELETE_BOOK":
                    return handleDeleteBook(parts);
                case "INCREASE_STOCK":
                    return handleIncreaseStock(parts);
                case "DECREASE_STOCK":
                    return handleDecreaseStock(parts);
                case "SET_BOOK_STATUS":
                    return handleSetBookStatus(parts);
                // 商品管理相关操作
                case "ADD_PRODUCT":
                    return handleAddProduct(parts);
                case "UPDATE_PRODUCT":
                    return handleUpdateProduct(parts);
                case "DELETE_PRODUCT":
                    return handleDeleteProduct(parts);
                // 用户管理相关操作
                case "GET_ALL_USERS":
                    return handleGetAllUsers(parts);
                case "GET_ALL_STUDENTS":
                    return handleGetAllStudents(parts);
                case "GET_ALL_TEACHERS":
                    return handleGetAllTeachers(parts);
                case "GET_ALL_ADMINS":
                    return handleGetAllAdmins(parts);
                case "UPDATE_STUDENT":
                    return handleUpdateStudent(parts);
                default:
                    return "ERROR: 未知的操作类型: " + action;
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 处理请求时发生异常: " + e.getMessage());
            return "ERROR: 服务器内部错误";
        }
    }

    /**
     * 处理登录请求
     * 格式: LOGIN:用户名:密码
     */
    private String handleLogin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR: 登录请求参数错误";
        }

        String username = parts[1];
        String password = parts[2];

        try {
            IUserService userService = ServiceFactory.getUserService();
            User user = userService.login(username, password);
            
            if (user != null) {
                String selfId = getSelfIdFromUser(user);
                return "SUCCESS:LOGIN:" + user.getRole() + ":" + selfId;
            } else {
                return "ERROR: 用户名或密码错误";
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 登录处理异常: " + e.getMessage());
            return "ERROR: 登录失败";
        }
    }

    /**
     * 处理注册请求
     * 格式: REGISTER:用户名:密码:角色:其他信息
     */
    private String handleRegister(String[] parts) {
        if (parts.length < 4) {
            return "ERROR: 注册请求参数不足";
        }

        String username = parts[1];
        String password = parts[2];
        String role = parts[3];

        try {
            IUserService userService = ServiceFactory.getUserService();
            
            // 根据角色创建相应的用户对象
            User user = null;
            switch (role.toUpperCase()) {
                case "STUDENT":
                    if (parts.length >= 7) {
                        Student student = new Student();
                        student.setUsername(username);
                        student.setPassword(password);
                        student.setRole("STUDENT");
                        student.setStudentName(parts[4]);
                        student.setClassName(parts[5]);
                        student.setStudentId(parts[6]);
                        user = student;
                    } else {
                        return "ERROR: 学生注册信息不完整";
                    }
                    break;
                case "TEACHER":
                    if (parts.length >= 8) {
                        Teacher teacher = new Teacher();
                        teacher.setUsername(username);
                        teacher.setPassword(password);
                        teacher.setRole("TEACHER");
                        teacher.setTeacherId(parts[4]);
                        teacher.setTeacherName(parts[5]);
                        teacher.setSex(parts[6]);
                        teacher.setTechnical(parts[7]);
                        teacher.setDepartmentId(parts.length > 8 ? parts[8] : "");
                        user = teacher;
                    } else {
                        return "ERROR: 教师注册信息不完整";
                    }
                    break;
                case "ADMIN":
                    if (parts.length >= 6) {
                        Admin admin = new Admin();
                        admin.setUsername(username);
                        admin.setPassword(password);
                        admin.setRole("ADMIN");
                        admin.setAdminId(parts[4]);
                        admin.setAdminName(parts[5]);
                        user = admin;
                    } else {
                        return "ERROR: 管理员注册信息不完整";
                    }
                    break;
                default:
                    return "ERROR: 无效的角色类型";
            }

            IUserService.RegisterResult result = userService.register(user);
            
            switch (result) {
                case SUCCESS:
                    return "SUCCESS:REGISTER:注册成功";
                case USERNAME_EXISTS:
                    return "ERROR: 用户名已存在";
                case STUDENT_ID_EXISTS:
                    return "ERROR: 学号已存在";
                case TEACHER_ID_EXISTS:
                    return "ERROR: 教师编号已存在";
                case ADMIN_ID_EXISTS:
                    return "ERROR: 管理员工号已存在";
                case VALIDATION_FAILED:
                    return "ERROR: 数据验证失败";
                case DATABASE_ERROR:
                default:
                    return "ERROR: 注册失败";
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 注册处理异常: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: 注册失败";
        }
    }

    /**
     * 处理登出请求
     * 格式: LOGOUT:用户名
     */
    private String handleLogout(String[] parts) {
        if (parts.length != 2) {
            return "ERROR: 登出请求参数错误";
        }

        String username = parts[1];
        // 这里可以添加登出逻辑，比如清理会话等
        return "SUCCESS:LOGOUT:登出成功";
    }

    /**
     * 处理获取课程列表请求
     * 格式: GET_SUBJECTS:关键词
     */
    private String handleGetSubjects(String[] parts) {
        try {
            String keyword = parts.length > 1 ? parts[1] : "";
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            List<Subject> subjects = subjectService.getSubjectsByName(keyword);
            
            StringBuilder response = new StringBuilder("SUCCESS:SUBJECTS:");
            for (int i = 0; i < subjects.size(); i++) {
                if (i > 0) response.append("|");
                Subject subject = subjects.get(i);
                response.append(subject.getSubjectId()).append(",");
                response.append(subject.getSubjectName()).append(",");
                response.append(subject.getSubjectDate() != null ? subject.getSubjectDate().toString() : "").append(",");
                response.append(subject.getSubjectNum() != null ? subject.getSubjectNum() : 0).append(",");
                response.append(subject.getCredit() != null ? subject.getCredit() : 0.0).append(",");
                response.append(subject.getTeacherId()).append(",");
                response.append(subject.getWeekRange()).append(",");
                response.append(subject.getWeekType()).append(",");
                response.append(subject.getClassTime()).append(",");
                response.append(subject.getClassroom());
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取课程列表异常: " + e.getMessage());
            return "ERROR: 获取课程列表失败";
        }
    }

    /**
     * 处理添加课程请求
     * 格式: ADD_SUBJECT:课程数据
     */
    private String handleAddSubject(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 添加课程请求参数不足";
            }
            
            Subject subject = parseSubjectFromData(parts[1]);
            if (subject == null) {
                return "ERROR: 课程数据格式错误";
            }
            
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            boolean success = subjectService.addSubject(subject);
            return success ? "SUCCESS:ADD_SUBJECT:添加成功" : "ERROR: 添加课程失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 添加课程异常: " + e.getMessage());
            return "ERROR: 添加课程失败";
        }
    }

    /**
     * 处理更新课程请求
     * 格式: UPDATE_SUBJECT:课程数据
     */
    private String handleUpdateSubject(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 更新课程请求参数不足";
            }
            
            Subject subject = parseSubjectFromData(parts[1]);
            if (subject == null) {
                return "ERROR: 课程数据格式错误";
            }
            
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            boolean success = subjectService.updateSubject(subject);
            return success ? "SUCCESS:UPDATE_SUBJECT:更新成功" : "ERROR: 更新课程失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 更新课程异常: " + e.getMessage());
            return "ERROR: 更新课程失败";
        }
    }

    /**
     * 处理删除课程请求
     * 格式: DELETE_SUBJECT:课程ID
     */
    private String handleDeleteSubject(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 删除课程请求参数不足";
            }
            
            String subjectId = parts[1];
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            boolean success = subjectService.deleteSubject(subjectId);
            return success ? "SUCCESS:DELETE_SUBJECT:删除成功" : "ERROR: 删除课程失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 删除课程异常: " + e.getMessage());
            return "ERROR: 删除课程失败";
        }
    }

    /**
     * 处理检查学生请求
     * 格式: CHECK_STUDENT:学号
     */
    private String handleCheckStudent(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 检查学生请求参数不足";
            }
            
            String studentId = parts[1];
            IStudentService studentService = ServiceFactory.getStudentService();
            Student student = studentService.getBySelfId(studentId);
            return student != null ? "SUCCESS:CHECK_STUDENT:学生存在" : "ERROR: 学生不存在";
        } catch (Exception e) {
            System.err.println("[SERVER] 检查学生异常: " + e.getMessage());
            return "ERROR: 检查学生失败";
        }
    }

    /**
     * 处理管理员代选请求
     * 格式: ADMIN_ASSIST_CHOOSE:学号:课程ID:是否忽略时间冲突
     */
    private String handleAdminAssistChoose(String[] parts) {
        try {
            if (parts.length < 4) {
                return "ERROR: 管理员代选请求参数不足";
            }
            
            String studentId = parts[1];
            String subjectId = parts[2];
            boolean ignoreTimeConflict = Boolean.parseBoolean(parts[3]);
            
            IChooseService chooseService = ServiceFactory.getChooseService();
            boolean success = chooseService.adminAssistChooseSubject(studentId, subjectId, ignoreTimeConflict);
            return success ? "SUCCESS:ADMIN_ASSIST_CHOOSE:代选成功" : "ERROR: 代选失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 管理员代选异常: " + e.getMessage());
            return "ERROR: 代选失败";
        }
    }

    /**
     * 从数据字符串解析课程对象
     */
    private Subject parseSubjectFromData(String data) {
        try {
            String[] fields = data.split(",");
            if (fields.length >= 10) {
                Subject subject = new Subject();
                subject.setSubjectId(fields[0]);
                subject.setSubjectName(fields[1]);
                if (!fields[2].isEmpty()) {
                    subject.setSubjectDate(java.sql.Date.valueOf(fields[2]));
                }
                subject.setSubjectNum(Integer.parseInt(fields[3]));
                subject.setCredit(Double.parseDouble(fields[4]));
                subject.setTeacherId(fields[5]);
                subject.setWeekRange(fields[6]);
                subject.setWeekType(fields[7]);
                subject.setClassTime(fields[8]);
                subject.setClassroom(fields[9]);
                return subject;
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 解析课程数据异常: " + e.getMessage());
        }
        return null;
    }

    // ================= 图书馆相关处理方法 =================

    /**
     * 处理搜索图书请求
     * 格式: SEARCH_BOOKS:关键词:状态:排序:页码:页大小
     */
    private String handleSearchBooks(String[] parts) {
        try {
            String keyword = parts.length > 1 ? parts[1] : "";
            String status = parts.length > 2 ? parts[2] : "全部";
            String sort = parts.length > 3 ? parts[3] : "默认(最新)";
            int page = parts.length > 4 ? Integer.parseInt(parts[4]) : 1;
            int pageSize = parts.length > 5 ? Integer.parseInt(parts[5]) : 10;
            
            LibraryService libraryService = new LibraryService();
            List<Book> books = libraryService.searchBooksAdvanced(keyword, status, sort, page, pageSize);
            
            StringBuilder response = new StringBuilder("SUCCESS:BOOKS:");
            for (int i = 0; i < books.size(); i++) {
                if (i > 0) response.append("|");
                Book book = books.get(i);
                response.append(book.getBookId()).append(",");
                response.append(book.getTitle()).append(",");
                response.append(book.getAuthor()).append(",");
                response.append(book.getIsbn()).append(",");
                response.append(book.getAvailableCopies() != null ? book.getAvailableCopies() : 0).append(",");
                response.append(book.getTotalCopies() != null ? book.getTotalCopies() : 0).append(",");
                response.append(book.getStatus() != null ? book.getStatus() : "正常");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 搜索图书异常: " + e.getMessage());
            return "ERROR: 搜索图书失败";
        }
    }

    /**
     * 处理借书请求
     * 格式: BORROW_BOOK:用户ID:图书ID:借阅天数
     */
    private String handleBorrowBook(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 借书请求参数不足";
            }
            
            String userId = parts[1];
            Integer bookId = Integer.parseInt(parts[2]);
            int days = parts.length > 3 ? Integer.parseInt(parts[3]) : 30;
            
            LibraryService libraryService = new LibraryService();
            com.vCampus.service.ServiceResult result = libraryService.borrowBookWithReason(userId, bookId, days);
            
            return result.isSuccess() ? "SUCCESS:BORROW:" + result.getMessage() : "ERROR:" + result.getMessage();
        } catch (Exception e) {
            System.err.println("[SERVER] 借书异常: " + e.getMessage());
            return "ERROR: 借书失败";
        }
    }

    /**
     * 处理还书请求
     * 格式: RETURN_BOOK:用户ID:记录ID:图书ID
     */
    private String handleReturnBook(String[] parts) {
        try {
            if (parts.length < 4) {
                return "ERROR: 还书请求参数不足";
            }
            
            String userId = parts[1];
            Integer recordId = Integer.parseInt(parts[2]);
            Integer bookId = Integer.parseInt(parts[3]);
            
            LibraryService libraryService = new LibraryService();
            com.vCampus.service.ServiceResult result = libraryService.returnBookWithReason(userId, recordId, bookId);
            
            return result.isSuccess() ? "SUCCESS:RETURN:" + result.getMessage() : "ERROR:" + result.getMessage();
        } catch (Exception e) {
            System.err.println("[SERVER] 还书异常: " + e.getMessage());
            return "ERROR: 还书失败";
        }
    }

    /**
     * 处理续借请求
     * 格式: RENEW_BOOK:用户ID:记录ID:续借天数
     */
    private String handleRenewBook(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 续借请求参数不足";
            }
            
            String userId = parts[1];
            Integer recordId = Integer.parseInt(parts[2]);
            int days = parts.length > 3 ? Integer.parseInt(parts[3]) : 30;
            
            LibraryService libraryService = new LibraryService();
            com.vCampus.service.ServiceResult result = libraryService.renewBorrowWithReason(userId, recordId, days, 1);
            
            return result.isSuccess() ? "SUCCESS:RENEW:" + result.getMessage() : "ERROR:" + result.getMessage();
        } catch (Exception e) {
            System.err.println("[SERVER] 续借异常: " + e.getMessage());
            return "ERROR: 续借失败";
        }
    }

    /**
     * 处理预约图书请求
     * 格式: RESERVE_BOOK:用户ID:图书ID
     */
    private String handleReserveBook(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 预约请求参数不足";
            }
            
            String userId = parts[1];
            Integer bookId = Integer.parseInt(parts[2]);
            
            LibraryService libraryService = new LibraryService();
            com.vCampus.service.ServiceResult result = libraryService.reserveBookWithReason(userId, bookId);
            
            return result.isSuccess() ? "SUCCESS:RESERVE:" + result.getMessage() : "ERROR:" + result.getMessage();
        } catch (Exception e) {
            System.err.println("[SERVER] 预约图书异常: " + e.getMessage());
            return "ERROR: 预约失败";
        }
    }

    /**
     * 处理获取我的借阅记录请求
     * 格式: GET_MY_BORROWS:用户ID:状态
     */
    private String handleGetMyBorrows(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取借阅记录请求参数不足";
            }
            
            String userId = parts[1];
            String status = parts.length > 2 ? parts[2] : "借出";
            
            LibraryService libraryService = new LibraryService();
            List<BorrowRecord> borrows = libraryService.listMyBorrowsByStatus(userId, status);
            
            StringBuilder response = new StringBuilder("SUCCESS:BORROWS:");
            for (int i = 0; i < borrows.size(); i++) {
                if (i > 0) response.append("|");
                BorrowRecord record = borrows.get(i);
                response.append(record.getRecordId()).append(",");
                response.append(record.getBookId()).append(",");
                response.append(record.getBorrowDate() != null ? record.getBorrowDate().toString() : "").append(",");
                response.append(record.getDueDate() != null ? record.getDueDate().toString() : "").append(",");
                response.append(record.getReturnDate() != null ? record.getReturnDate().toString() : "").append(",");
                response.append(record.getStatus() != null ? record.getStatus() : "").append(",");
                response.append(record.getFine() != null ? record.getFine() : 0.0).append(",");
                response.append(record.getRenewTimes() != null ? record.getRenewTimes() : 0);
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取借阅记录异常: " + e.getMessage());
            return "ERROR: 获取借阅记录失败";
        }
    }

    /**
     * 处理获取我的预约记录请求
     * 格式: GET_MY_RESERVATIONS:用户ID
     */
    private String handleGetMyReservations(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取预约记录请求参数不足";
            }
            
            String userId = parts[1];
            LibraryService libraryService = new LibraryService();
            List<com.vCampus.entity.Reservation> reservations = libraryService.listMyReservations(userId);
            
            StringBuilder response = new StringBuilder("SUCCESS:RESERVATIONS:");
            for (int i = 0; i < reservations.size(); i++) {
                if (i > 0) response.append("|");
                com.vCampus.entity.Reservation reservation = reservations.get(i);
                response.append(reservation.getReservationId()).append(",");
                response.append(reservation.getBookId()).append(",");
                response.append(reservation.getReservedAt() != null ? reservation.getReservedAt().toString() : "").append(",");
                response.append(reservation.getExpiresAt() != null ? reservation.getExpiresAt().toString() : "").append(",");
                response.append(reservation.getQueueOrder()).append(",");
                response.append(reservation.getStatus() != null ? reservation.getStatus() : "");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取预约记录异常: " + e.getMessage());
            return "ERROR: 获取预约记录失败";
        }
    }

    // ================= 商店相关处理方法 =================

    /**
     * 处理获取所有商品请求
     * 格式: GET_ALL_PRODUCTS
     */
    private String handleGetAllProducts(String[] parts) {
        try {
            IShopService shopService = ServiceFactory.getShopService();
            List<Product> products = shopService.getAllProducts();
            
            StringBuilder response = new StringBuilder("SUCCESS:PRODUCTS:");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) response.append("|");
                Product product = products.get(i);
                response.append(product.getProductId()).append(",");
                response.append(product.getProductName()).append(",");
                response.append(product.getPrice()).append(",");
                response.append(product.getStock()).append(",");
                response.append(product.getCategory() != null ? product.getCategory() : "").append(",");
                response.append(product.getDescription() != null ? product.getDescription() : "");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取所有商品异常: " + e.getMessage());
            return "ERROR: 获取商品失败";
        }
    }

    /**
     * 处理根据分类获取商品请求
     * 格式: GET_PRODUCTS_BY_CATEGORY:分类
     */
    private String handleGetProductsByCategory(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取分类商品请求参数不足";
            }
            
            String category = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            List<Product> products = shopService.getProductsByCategory(category);
            
            StringBuilder response = new StringBuilder("SUCCESS:PRODUCTS:");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) response.append("|");
                Product product = products.get(i);
                response.append(product.getProductId()).append(",");
                response.append(product.getProductName()).append(",");
                response.append(product.getPrice()).append(",");
                response.append(product.getStock()).append(",");
                response.append(product.getCategory() != null ? product.getCategory() : "").append(",");
                response.append(product.getDescription() != null ? product.getDescription() : "");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取分类商品异常: " + e.getMessage());
            return "ERROR: 获取分类商品失败";
        }
    }

    /**
     * 处理搜索商品请求
     * 格式: SEARCH_PRODUCTS:关键词
     */
    private String handleSearchProducts(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 搜索商品请求参数不足";
            }
            
            String keyword = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            List<Product> products = shopService.searchProducts(keyword);
            
            StringBuilder response = new StringBuilder("SUCCESS:PRODUCTS:");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) response.append("|");
                Product product = products.get(i);
                response.append(product.getProductId()).append(",");
                response.append(product.getProductName()).append(",");
                response.append(product.getPrice()).append(",");
                response.append(product.getStock()).append(",");
                response.append(product.getCategory() != null ? product.getCategory() : "").append(",");
                response.append(product.getDescription() != null ? product.getDescription() : "");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 搜索商品异常: " + e.getMessage());
            return "ERROR: 搜索商品失败";
        }
    }

    /**
     * 处理根据ID获取商品请求
     * 格式: GET_PRODUCT_BY_ID:商品ID
     */
    private String handleGetProductById(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取商品请求参数不足";
            }
            
            String productId = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            Product product = shopService.getProductById(productId);
            
            if (product != null) {
                return "SUCCESS:PRODUCT:" + product.getProductId() + "," + 
                       product.getProductName() + "," + product.getPrice() + "," + 
                       product.getStock() + "," + (product.getCategory() != null ? product.getCategory() : "") + "," +
                       (product.getDescription() != null ? product.getDescription() : "");
            } else {
                return "ERROR: 商品不存在";
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 获取商品异常: " + e.getMessage());
            return "ERROR: 获取商品失败";
        }
    }

    /**
     * 处理获取所有分类请求
     * 格式: GET_ALL_CATEGORIES
     */
    private String handleGetAllCategories(String[] parts) {
        try {
            IShopService shopService = ServiceFactory.getShopService();
            List<String> categories = shopService.getAllCategories();
            
            StringBuilder response = new StringBuilder("SUCCESS:CATEGORIES:");
            for (int i = 0; i < categories.size(); i++) {
                if (i > 0) response.append(",");
                response.append(categories.get(i));
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取分类异常: " + e.getMessage());
            return "ERROR: 获取分类失败";
        }
    }

    /**
     * 处理购买请求
     * 格式: PURCHASE:学生ID:订单项数据
     */
    private String handlePurchase(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 购买请求参数不足";
            }
            
            String studentId = parts[1];
            String orderItemsData = parts[2];
            
            // 解析订单项数据
            List<OrderItem> items = parseOrderItemsFromData(orderItemsData);
            if (items == null || items.isEmpty()) {
                return "ERROR: 订单项数据格式错误";
            }
            
            IShopService shopService = ServiceFactory.getShopService();
            String orderId = shopService.purchase(studentId, items);
            
            return orderId != null ? "SUCCESS:PURCHASE:" + orderId : "ERROR: 购买失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 购买异常: " + e.getMessage());
            return "ERROR: 购买失败";
        }
    }

    /**
     * 处理支付订单请求
     * 格式: PAY_ORDER:订单ID
     */
    private String handlePayOrder(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 支付订单请求参数不足";
            }
            
            String orderId = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            boolean success = shopService.payOrder(orderId);
            
            return success ? "SUCCESS:PAY:支付成功" : "ERROR: 支付失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 支付订单异常: " + e.getMessage());
            return "ERROR: 支付失败";
        }
    }

    /**
     * 处理取消订单请求
     * 格式: CANCEL_ORDER:订单ID
     */
    private String handleCancelOrder(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 取消订单请求参数不足";
            }
            
            String orderId = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            boolean success = shopService.cancelOrder(orderId);
            
            return success ? "SUCCESS:CANCEL:取消成功" : "ERROR: 取消失败";
        } catch (Exception e) {
            System.err.println("[SERVER] 取消订单异常: " + e.getMessage());
            return "ERROR: 取消失败";
        }
    }

    /**
     * 处理获取订单历史请求
     * 格式: GET_ORDER_HISTORY:学生ID
     */
    private String handleGetOrderHistory(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取订单历史请求参数不足";
            }
            
            String studentId = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            List<Order> orders = shopService.getOrderHistory(studentId);
            
            StringBuilder response = new StringBuilder("SUCCESS:ORDERS:");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) response.append("|");
                Order order = orders.get(i);
                response.append(order.getOrderId()).append(",");
                response.append(order.getStudentId()).append(",");
                response.append(order.getOrderDate() != null ? order.getOrderDate().toString() : "").append(",");
                response.append(order.getTotalAmount()).append(",");
                response.append(order.getStatus() != null ? order.getStatus() : "");
            }
            return response.toString();
        } catch (Exception e) {
            System.err.println("[SERVER] 获取订单历史异常: " + e.getMessage());
            return "ERROR: 获取订单历史失败";
        }
    }

    /**
     * 处理获取订单详情请求
     * 格式: GET_ORDER_DETAILS:订单ID
     */
    private String handleGetOrderDetails(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取订单详情请求参数不足";
            }
            
            String orderId = parts[1];
            IShopService shopService = ServiceFactory.getShopService();
            Order order = shopService.getOrderDetails(orderId);
            
            if (order != null) {
                return "SUCCESS:ORDER:" + order.getOrderId() + "," + 
                       order.getStudentId() + "," + 
                       (order.getOrderDate() != null ? order.getOrderDate().toString() : "") + "," +
                       order.getTotalAmount() + "," + 
                       (order.getStatus() != null ? order.getStatus() : "");
            } else {
                return "ERROR: 订单不存在";
            }
        } catch (Exception e) {
            System.err.println("[SERVER] 获取订单详情异常: " + e.getMessage());
            return "ERROR: 获取订单详情失败";
        }
    }

    /**
     * 处理检查库存请求
     * 格式: CHECK_STOCK:商品ID:需要数量
     */
    private String handleCheckStock(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 检查库存请求参数不足";
            }
            
            String productId = parts[1];
            int requiredQuantity = Integer.parseInt(parts[2]);
            
            IShopService shopService = ServiceFactory.getShopService();
            boolean available = shopService.checkStock(productId, requiredQuantity);
            
            return available ? "SUCCESS:STOCK:库存充足" : "ERROR: 库存不足";
        } catch (Exception e) {
            System.err.println("[SERVER] 检查库存异常: " + e.getMessage());
            return "ERROR: 检查库存失败";
        }
    }

    /**
     * 从数据字符串解析订单项列表
     */
    private List<OrderItem> parseOrderItemsFromData(String data) {
        try {
            List<OrderItem> items = new java.util.ArrayList<>();
            String[] itemStrings = data.split("\\|");
            
            for (String itemString : itemStrings) {
                String[] fields = itemString.split(",");
                if (fields.length >= 3) {
                    OrderItem item = new OrderItem();
                    item.setProductId(fields[0]);
                    item.setQuantity(Integer.parseInt(fields[1]));
                    item.setSubtotal(Double.parseDouble(fields[2]));
                    items.add(item);
                }
            }
            return items;
        } catch (Exception e) {
            System.err.println("[SERVER] 解析订单项数据异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从用户对象获取相应的ID
     */
    private String getSelfIdFromUser(User user) {
        if (user instanceof Student) {
            return ((Student) user).getStudentId();
        } else if (user instanceof Teacher) {
            return ((Teacher) user).getTeacherId();
        } else if (user instanceof Admin) {
            return ((Admin) user).getAdminId();
        } else {
            return String.valueOf(user.getUserId());
        }
    }

    // ==================== 课程管理相关处理方法 ====================

    /**
     * 处理按名称搜索课程请求
     * 格式: GET_SUBJECTS_BY_NAME:关键词
     */
    private String handleGetSubjectsByName(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 搜索课程请求参数不足";
            }
            
            String keyword = parts[1];
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            List<Subject> subjects = subjectService.getSubjectsByName(keyword);
            
            StringBuilder response = new StringBuilder("SUCCESS:SUBJECTS:");
            for (Subject subject : subjects) {
                response.append(subject.getSubjectId()).append(",")
                       .append(subject.getSubjectName()).append(",")
                       .append(subject.getSubjectNum()).append(",")
                       .append(subject.getCredit()).append(",")
                       .append(subject.getTeacherId()).append(",")
                       .append(subject.getWeekRange()).append(",")
                       .append(subject.getWeekType()).append(",")
                       .append(subject.getClassTime()).append(",")
                       .append(subject.getClassroom()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 搜索课程失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取课程选课记录请求
     * 格式: GET_SUBJECT_CHOOSES:课程ID
     */
    private String handleGetSubjectChooses(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取选课记录请求参数不足";
            }
            
            String subjectId = parts[1];
            IChooseService chooseService = ServiceFactory.getChooseService();
            List<Choose> chooses = chooseService.getSubjectChooses(subjectId);
            
            StringBuilder response = new StringBuilder("SUCCESS:CHOOSES:");
            for (Choose choose : chooses) {
                response.append(choose.getSelectid()).append(",")
                       .append(choose.getStudentId()).append(",")
                       .append(choose.getSubjectId()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取选课记录失败: " + e.getMessage();
        }
    }

    /**
     * 处理验证课程信息请求
     * 格式: VALIDATE_SUBJECT:课程ID,课程名称,教师ID,周次范围,单双周,上课时间,教室
     */
    private String handleValidateSubject(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 验证课程请求参数不足";
            }
            
            String[] fields = parts[1].split(",");
            if (fields.length < 7) {
                return "ERROR: 课程信息不完整";
            }
            
            Subject subject = new Subject();
            subject.setSubjectId(fields[0]);
            subject.setSubjectName(fields[1]);
            subject.setTeacherId(fields[2]);
            subject.setWeekRange(fields[3]);
            subject.setWeekType(fields[4]);
            subject.setClassTime(fields[5]);
            subject.setClassroom(fields[6]);
            
            ISubjectService subjectService = ServiceFactory.getSubjectService();
            boolean valid = subjectService.validateSubject(subject);
            
            return valid ? "SUCCESS:VALIDATE:课程信息验证通过" : "ERROR: 课程信息验证失败";
        } catch (Exception e) {
            return "ERROR: 验证课程失败: " + e.getMessage();
        }
    }

    /**
     * 处理按学号获取学生信息请求
     * 格式: GET_STUDENT_BY_ID:学号
     */
    private String handleGetStudentById(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取学生信息请求参数不足";
            }
            
            String studentId = parts[1];
            IStudentService studentService = ServiceFactory.getStudentService();
            Student student = studentService.getBySelfId(studentId);
            
            if (student == null) {
                return "ERROR: 学生不存在";
            }
            
            return "SUCCESS:STUDENT:" + student.getStudentId() + "," + 
                   student.getStudentName() + "," + student.getUserId() + "," + 
                   student.getClassName() + "," + student.getSex() + "," + student.getEmail();
        } catch (Exception e) {
            return "ERROR: 获取学生信息失败: " + e.getMessage();
        }
    }

    /**
     * 处理按用户ID获取学生信息请求
     * 格式: GET_STUDENT_BY_USER_ID:用户ID
     */
    private String handleGetStudentByUserId(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取学生信息请求参数不足";
            }
            
            int userId = Integer.parseInt(parts[1]);
            IStudentService studentService = ServiceFactory.getStudentService();
            Student student = studentService.getByUserId(userId);
            
            if (student == null) {
                return "ERROR: 学生不存在";
            }
            
            return "SUCCESS:STUDENT:" + student.getStudentId() + "," + 
                   student.getStudentName() + "," + student.getUserId() + "," + 
                   student.getClassName() + "," + student.getSex() + "," + student.getEmail();
        } catch (Exception e) {
            return "ERROR: 获取学生信息失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取完整学生信息请求
     * 格式: GET_STUDENT_FULL:学号
     */
    private String handleGetStudentFull(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取学生信息请求参数不足";
            }
            
            String studentId = parts[1];
            IStudentService studentService = ServiceFactory.getStudentService();
            Student student = studentService.getStudentFull(studentId);
            
            if (student == null) {
                return "ERROR: 学生不存在";
            }
            
            return "SUCCESS:STUDENT:" + student.getStudentId() + "," + 
                   student.getStudentName() + "," + student.getUserId() + "," + 
                   student.getClassName() + "," + student.getSex() + "," + student.getEmail();
        } catch (Exception e) {
            return "ERROR: 获取学生信息失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取我的课程请求
     * 格式: GET_MY_SUBJECTS:学号
     */
    private String handleGetMySubjects(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 获取我的课程请求参数不足";
            }
            
            String studentId = parts[1];
            IChooseService chooseService = ServiceFactory.getChooseService();
            List<Subject> subjects = chooseService.getStudentSubjects(studentId);
            
            StringBuilder response = new StringBuilder("SUCCESS:SUBJECTS:");
            for (Subject subject : subjects) {
                response.append(subject.getSubjectId()).append(",")
                       .append(subject.getSubjectName()).append(",")
                       .append(subject.getSubjectNum()).append(",")
                       .append(subject.getCredit()).append(",")
                       .append(subject.getTeacherId()).append(",")
                       .append(subject.getWeekRange()).append(",")
                       .append(subject.getWeekType()).append(",")
                       .append(subject.getClassTime()).append(",")
                       .append(subject.getClassroom()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取我的课程失败: " + e.getMessage();
        }
    }

    /**
     * 处理选课请求
     * 格式: CHOOSE_SUBJECT:学号:课程ID
     */
    private String handleChooseSubject(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 选课请求参数不足";
            }
            
            String studentId = parts[1];
            String subjectId = parts[2];
            IChooseService chooseService = ServiceFactory.getChooseService();
            boolean success = chooseService.chooseSubject(studentId, subjectId);
            
            return success ? "SUCCESS:CHOOSE:选课成功" : "ERROR: 选课失败";
        } catch (Exception e) {
            return "ERROR: 选课失败: " + e.getMessage();
        }
    }

    /**
     * 处理退课请求
     * 格式: DROP_SUBJECT:学号:课程ID
     */
    private String handleDropSubject(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 退课请求参数不足";
            }
            
            String studentId = parts[1];
            String subjectId = parts[2];
            IChooseService chooseService = ServiceFactory.getChooseService();
            
            // 先找到选课记录
            List<Choose> chooses = chooseService.getSubjectChooses(subjectId);
            Choose myRecord = chooses.stream()
                    .filter(c -> c.getStudentId().equals(studentId))
                    .findFirst()
                    .orElse(null);
            
            if (myRecord == null) {
                return "ERROR: 未找到选课记录";
            }
            
            boolean success = chooseService.dropSubject(myRecord.getSelectid());
            return success ? "SUCCESS:DROP:退课成功" : "ERROR: 退课失败";
        } catch (Exception e) {
            return "ERROR: 退课失败: " + e.getMessage();
        }
    }

    /**
     * 处理管理员退课请求
     * 格式: ADMIN_DROP_SUBJECT:学号:课程ID
     */
    private String handleAdminDropSubject(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 管理员退课请求参数不足";
            }
            
            String studentId = parts[1];
            String subjectId = parts[2];
            IChooseService chooseService = ServiceFactory.getChooseService();
            
            // 先找到选课记录
            List<Choose> chooses = chooseService.getSubjectChooses(subjectId);
            Choose myRecord = chooses.stream()
                    .filter(c -> c.getStudentId().equals(studentId))
                    .findFirst()
                    .orElse(null);
            
            if (myRecord == null) {
                return "ERROR: 未找到选课记录";
            }
            
            boolean success = chooseService.dropSubject(myRecord.getSelectid());
            return success ? "SUCCESS:DROP:管理员退课成功" : "ERROR: 管理员退课失败";
        } catch (Exception e) {
            return "ERROR: 管理员退课失败: " + e.getMessage();
        }
    }

    // ==================== 图书管理相关处理方法 ====================

    /**
     * 处理添加图书请求
     * 格式: ADD_BOOK:ISBN,书名,作者,分类,出版社,出版日期,总数量,可借数量,位置,状态
     */
    private String handleAddBook(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 添加图书请求参数不足";
            }
            
            String[] fields = parts[1].split(",");
            if (fields.length < 10) {
                return "ERROR: 图书信息不完整";
            }
            
            Book book = new Book();
            book.setIsbn(fields[0]);
            book.setTitle(fields[1]);
            book.setAuthor(fields[2]);
            book.setCategory(fields[3]);
            book.setPublisher(fields[4]);
            book.setPubDate(java.sql.Date.valueOf(fields[5]));
            book.setTotalCopies(Integer.parseInt(fields[6]));
            book.setAvailableCopies(Integer.parseInt(fields[7]));
            book.setLocation(fields[8]);
            book.setStatus(fields[9]);
            
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.addBook(book);
            
            return success ? "SUCCESS:ADD:图书添加成功" : "ERROR: 图书添加失败";
        } catch (Exception e) {
            return "ERROR: 图书添加失败: " + e.getMessage();
        }
    }

    /**
     * 处理更新图书请求
     * 格式: UPDATE_BOOK:图书ID,ISBN,书名,作者,分类,出版社,出版日期,总数量,可借数量,位置,状态
     */
    private String handleUpdateBook(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 更新图书请求参数不足";
            }
            
            String[] fields = parts[1].split(",");
            if (fields.length < 11) {
                return "ERROR: 图书信息不完整";
            }
            
            Book book = new Book();
            book.setBookId(Integer.parseInt(fields[0]));
            book.setIsbn(fields[1]);
            book.setTitle(fields[2]);
            book.setAuthor(fields[3]);
            book.setCategory(fields[4]);
            book.setPublisher(fields[5]);
            book.setPubDate(java.sql.Date.valueOf(fields[6]));
            book.setTotalCopies(Integer.parseInt(fields[7]));
            book.setAvailableCopies(Integer.parseInt(fields[8]));
            book.setLocation(fields[9]);
            book.setStatus(fields[10]);
            
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.updateBook(book);
            
            return success ? "SUCCESS:UPDATE:图书更新成功" : "ERROR: 图书更新失败";
        } catch (Exception e) {
            return "ERROR: 图书更新失败: " + e.getMessage();
        }
    }

    /**
     * 处理删除图书请求
     * 格式: DELETE_BOOK:图书ID
     */
    private String handleDeleteBook(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 删除图书请求参数不足";
            }
            
            int bookId = Integer.parseInt(parts[1]);
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.deleteBook(bookId);
            
            return success ? "SUCCESS:DELETE:图书删除成功" : "ERROR: 图书删除失败";
        } catch (Exception e) {
            return "ERROR: 图书删除失败: " + e.getMessage();
        }
    }

    /**
     * 处理增加库存请求
     * 格式: INCREASE_STOCK:图书ID:数量
     */
    private String handleIncreaseStock(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 增加库存请求参数不足";
            }
            
            int bookId = Integer.parseInt(parts[1]);
            int quantity = Integer.parseInt(parts[2]);
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.increaseStock(bookId, quantity);
            
            return success ? "SUCCESS:STOCK:库存增加成功" : "ERROR: 库存增加失败";
        } catch (Exception e) {
            return "ERROR: 库存增加失败: " + e.getMessage();
        }
    }

    /**
     * 处理减少库存请求
     * 格式: DECREASE_STOCK:图书ID:数量
     */
    private String handleDecreaseStock(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 减少库存请求参数不足";
            }
            
            int bookId = Integer.parseInt(parts[1]);
            int quantity = Integer.parseInt(parts[2]);
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.decreaseStock(bookId, quantity);
            
            return success ? "SUCCESS:STOCK:库存减少成功" : "ERROR: 库存减少失败";
        } catch (Exception e) {
            return "ERROR: 库存减少失败: " + e.getMessage();
        }
    }

    /**
     * 处理设置图书状态请求
     * 格式: SET_BOOK_STATUS:图书ID:状态
     */
    private String handleSetBookStatus(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 设置图书状态请求参数不足";
            }
            
            int bookId = Integer.parseInt(parts[1]);
            String status = parts[2];
            LibraryService libraryService = new LibraryService();
            boolean success = libraryService.setBookStatus(bookId, status);
            
            return success ? "SUCCESS:STATUS:图书状态设置成功" : "ERROR: 图书状态设置失败";
        } catch (Exception e) {
            return "ERROR: 图书状态设置失败: " + e.getMessage();
        }
    }

    // ==================== 商品管理相关处理方法 ====================

    /**
     * 处理添加商品请求
     * 格式: ADD_PRODUCT:商品名称,价格,库存,分类,描述
     */
    private String handleAddProduct(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 添加商品请求参数不足";
            }
            
            String[] fields = parts[1].split(",");
            if (fields.length < 5) {
                return "ERROR: 商品信息不完整";
            }
            
            Product product = new Product();
            product.setProductName(fields[0]);
            product.setPrice(Double.parseDouble(fields[1]));
            product.setStock(Integer.parseInt(fields[2]));
            product.setCategory(fields[3]);
            product.setDescription(fields[4]);
            
            IProductService productService = ServiceFactory.getProductService();
            boolean success = productService.addProduct(product);
            
            return success ? "SUCCESS:ADD:商品添加成功" : "ERROR: 商品添加失败";
        } catch (Exception e) {
            return "ERROR: 商品添加失败: " + e.getMessage();
        }
    }

    /**
     * 处理更新商品请求
     * 格式: UPDATE_PRODUCT:商品ID,商品名称,价格,库存,分类,描述
     */
    private String handleUpdateProduct(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 更新商品请求参数不足";
            }
            
            String[] fields = parts[1].split(",");
            if (fields.length < 6) {
                return "ERROR: 商品信息不完整";
            }
            
            Product product = new Product();
            product.setProductId(fields[0]);
            product.setProductName(fields[1]);
            product.setPrice(Double.parseDouble(fields[2]));
            product.setStock(Integer.parseInt(fields[3]));
            product.setCategory(fields[4]);
            product.setDescription(fields[5]);
            
            IProductService productService = ServiceFactory.getProductService();
            boolean success = productService.updateProduct(product);
            
            return success ? "SUCCESS:UPDATE:商品更新成功" : "ERROR: 商品更新失败";
        } catch (Exception e) {
            return "ERROR: 商品更新失败: " + e.getMessage();
        }
    }

    /**
     * 处理删除商品请求
     * 格式: DELETE_PRODUCT:商品ID
     */
    private String handleDeleteProduct(String[] parts) {
        try {
            if (parts.length < 2) {
                return "ERROR: 删除商品请求参数不足";
            }
            
            String productId = parts[1];
            IProductService productService = ServiceFactory.getProductService();
            boolean success = productService.deleteProduct(productId);
            
            return success ? "SUCCESS:DELETE:商品删除成功" : "ERROR: 商品删除失败";
        } catch (Exception e) {
            return "ERROR: 商品删除失败: " + e.getMessage();
        }
    }

    // ==================== 用户管理相关处理方法 ====================

    /**
     * 处理获取所有用户请求
     * 格式: GET_ALL_USERS
     */
    private String handleGetAllUsers(String[] parts) {
        try {
            IUserService userService = ServiceFactory.getUserService();
            List<User> users = userService.getAll();
            
            StringBuilder response = new StringBuilder("SUCCESS:USERS:");
            for (User user : users) {
                response.append(user.getUserId()).append(",")
                       .append(user.getUsername()).append(",")
                       .append(user.getRole()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取用户列表失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取所有学生请求
     * 格式: GET_ALL_STUDENTS
     */
    private String handleGetAllStudents(String[] parts) {
        try {
            IStudentService studentService = ServiceFactory.getStudentService();
            List<Student> students = studentService.getAll();
            
            StringBuilder response = new StringBuilder("SUCCESS:STUDENTS:");
            for (Student student : students) {
                response.append(student.getStudentId()).append(",")
                       .append(student.getStudentName()).append(",")
                       .append(student.getUserId()).append(",")
                       .append(student.getClassName()).append(",")
                       .append(student.getSex()).append(",")
                       .append(student.getEmail()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取学生列表失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取所有教师请求
     * 格式: GET_ALL_TEACHERS
     */
    private String handleGetAllTeachers(String[] parts) {
        try {
            ITeacherService teacherService = ServiceFactory.getTeacherService();
            List<Teacher> teachers = teacherService.getAll();
            
            StringBuilder response = new StringBuilder("SUCCESS:TEACHERS:");
            for (Teacher teacher : teachers) {
                response.append(teacher.getTeacherId()).append(",")
                       .append(teacher.getTeacherName()).append(",")
                       .append(teacher.getUserId()).append(",")
                       .append(teacher.getDepartmentId()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取教师列表失败: " + e.getMessage();
        }
    }

    /**
     * 处理获取所有管理员请求
     * 格式: GET_ALL_ADMINS
     */
    private String handleGetAllAdmins(String[] parts) {
        try {
            IAdminService adminService = ServiceFactory.getAdminService();
            List<Admin> admins = adminService.getAll();
            
            StringBuilder response = new StringBuilder("SUCCESS:ADMINS:");
            for (Admin admin : admins) {
                response.append(admin.getAdminId()).append(",")
                       .append(admin.getAdminName()).append(",")
                       .append(admin.getUserId()).append("|");
            }
            
            return response.toString();
        } catch (Exception e) {
            return "ERROR: 获取管理员列表失败: " + e.getMessage();
        }
    }

    /**
     * 处理创建订单请求
     * 格式: CREATE_ORDER:学号:商品ID1,数量1,小计1|商品ID2,数量2,小计2|...
     */
    private String handleCreateOrder(String[] parts) {
        try {
            if (parts.length < 3) {
                return "ERROR: 创建订单请求参数不足";
            }
            
            String studentId = parts[1];
            String itemsStr = parts[2];
            
            // 解析订单项
            List<OrderItem> items = new java.util.ArrayList<>();
            String[] itemStrings = itemsStr.split("\\|");
            for (String itemString : itemStrings) {
                String[] fields = itemString.split(",");
                if (fields.length >= 3) {
                    OrderItem item = new OrderItem();
                    item.setProductId(fields[0]);
                    item.setQuantity(Integer.parseInt(fields[1]));
                    item.setSubtotal(Double.parseDouble(fields[2]));
                    items.add(item);
                }
            }
            
            IShopService shopService = ServiceFactory.getShopService();
            String orderId = shopService.purchase(studentId, items);
            
            if (orderId != null) {
                return "SUCCESS:ORDER:" + orderId;
            } else {
                return "ERROR: 订单创建失败";
            }
        } catch (Exception e) {
            return "ERROR: 创建订单失败: " + e.getMessage();
        }
    }

    /**
     * 处理更新学生信息请求
     * 格式: UPDATE_STUDENT:学号:姓名:班级:性别:邮箱:身份证:状态
     */
    private String handleUpdateStudent(String[] parts) {
        try {
            if (parts.length < 8) {
                return "ERROR: 更新学生信息请求参数不足";
            }
            
            String studentId = parts[1];
            String studentName = parts[2];
            String className = parts[3];
            String sex = parts[4];
            String email = parts[5];
            String idCard = parts[6];
            String status = parts[7];
            
            IStudentService studentService = ServiceFactory.getStudentService();
            
            // 先获取现有学生信息
            Student existingStudent = studentService.getBySelfId(studentId);
            if (existingStudent == null) {
                return "ERROR: 学生不存在";
            }
            
            // 更新学生信息
            existingStudent.setStudentName(studentName);
            existingStudent.setClassName(className);
            existingStudent.setSex(sex);
            existingStudent.setEmail(email);
            existingStudent.setIdCard(idCard);
            existingStudent.setStatus(status);
            
            boolean success = studentService.updateStudentOnly(existingStudent);
            
            if (success) {
                return "SUCCESS:UPDATE:学生信息更新成功";
            } else {
                return "ERROR: 学生信息更新失败";
            }
        } catch (Exception e) {
            return "ERROR: 更新学生信息失败: " + e.getMessage();
        }
    }
}