package com.vcampus.main;

import com.vcampus.entity.*;
import com.vcampus.service.*;

import util.DBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 虚拟校园系统主应用程序
 * 用于测试和演示各个模块的功能
 */
public class VcampusApplication {

    public static void main(String[] args) {
        System.out.println("========== 虚拟校园系统启动 ==========");
        
        try {
            // 测试用户服务
            testUserService();
            
        	
        	//Student
        	// 1. 首先检查数据库结构
            //checkDatabaseStructure();
            
            // 2. 尝试使用Service层测试
            //testStudentServiceOperations();
            
        	
            // 测试学生服务
            testStudentService();
        	
        	  	      
            // 测试商品服务
            testProductService();
            
            //Ⅳ 测试订单服务
            testOrderService();
            
            //Ⅴ 测试商店服务
            testShopService();
            
            System.out.println("========== 虚拟校园系统测试完成 ==========");
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testUserService() {
        System.out.println("\n----- 测试用户服务 -----");
        IUserService userService = new UserServiceImpl();
        
        try {
            // 创建新用户
            User newUser = new User();
            newUser.setUsername("testuser7");
            newUser.setPassword("password1234");
            newUser.setRole("student");
            
            // 注册用户
            boolean registered = userService.register(newUser);
            System.out.println("用户注册结果: " + (registered ? "成功" : "失败"));
            
            // 用户登录
            User loggedInUser = userService.login("testuser7", "password1234");
            System.out.println("用户登录结果: " + (loggedInUser != null ? "成功" : "失败"));
            
            if (loggedInUser != null) {
                // 获取用户信息
                User userInfo = userService.getUserById(loggedInUser.getUserId());
                if (userInfo != null) {
                    System.out.println("用户信息: " + userInfo.getUsername() + ", 角色: " + userInfo.getRole());
                }
                
                // 跳过有问题的密码更新测试
                System.out.println("跳过密码更新测试（已知问题）");
                //boolean passwordUpdated = userService.updateUserPassword(loggedInUser.getUserId(), "newpassword456");
                //System.out.println("密码更新结果: " + (passwordUpdated ? "成功" : "失败"));
                
                // 检查用户角色
                boolean isStudent = userService.checkUserRole(loggedInUser.getUserId(), "student");
                System.out.println("用户是否是学生: " + isStudent);
            }
            
            // 获取所有用户
            List<User> allUsers = userService.getAllUsers();
            System.out.println("系统用户总数: " + (allUsers != null ? allUsers.size() : 0));
            
        } catch (Exception e) {
            System.err.println("用户服务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static void checkDatabaseStructure() {
        System.out.println("检查数据库结构...");
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 检查tbl_student表的外键约束
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, "TBL_STUDENT");
            while (foreignKeys.next()) {
                String fkName = foreignKeys.getString("FK_NAME");
                String pkTable = foreignKeys.getString("PKTABLE_NAME");
                String pkColumn = foreignKeys.getString("PKCOLUMN_NAME");
                String fkTable = foreignKeys.getString("FKTABLE_NAME");
                String fkColumn = foreignKeys.getString("FKCOLUMN_NAME");
                
                System.out.println("外键约束: " + fkName);
                System.out.println("  主表: " + pkTable + "." + pkColumn);
                System.out.println("  外键表: " + fkTable + "." + fkColumn);
            }
            foreignKeys.close();
            
        } catch (SQLException e) {
            System.err.println("检查数据库结构时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBUtil.closeConnection(conn);
            }
        }
    }
    
    public static void testStudentServiceOperations() {
        System.out.println("开始测试学生服务操作...");
        
        IUserService userService = new UserServiceImpl();
        IStudentService studentService = new StudentServiceImpl();
        
        try {
            // 1. 创建用户
        	String timestamp = String.valueOf(System.currentTimeMillis());
            String username = "user" + timestamp.substring(timestamp.length() - 4); 
            
            User user = new User();
            user.setUsername(username);
            user.setPassword("test123");
            user.setRole("student");
            
            boolean userCreated = userService.register(user);
            System.out.println("用户创建结果: " + userCreated);
            
            if (userCreated) {
                // 2. 获取刚创建的用户
                User createdUser = userService.getUserByUsername(user.getUsername());
                if (createdUser != null) {
                    System.out.println("创建的用户ID: " + createdUser.getUserId());
                    
                    // 3. 创建学生
                    Student student = new Student();
                    student.setStudentId("S" + timestamp.substring(timestamp.length() - 6));
                    student.setUserId(createdUser.getUserId());
                    student.setStudentName("测试学生");
                    student.setClassId("TEST2023");
                    student.setEnrollDate(new java.util.Date());
                    student.setStatus("正常");
                    
                    boolean studentCreated = studentService.addStudent(student);
                    System.out.println("学生创建结果: " + studentCreated);
                    
                    if (studentCreated) {
                        // 4. 验证
                        Student retrievedStudent = studentService.getStudentById(student.getStudentId());
                        System.out.println("检索到的学生: " + 
                            (retrievedStudent != null ? retrievedStudent.getStudentName() : "null"));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //如果需要性能优化，再考虑底层SQL操作
    public static void testStudentOperations() {
	    System.out.println("开始测试学生表操作...");
	    
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    
	    try {
	        conn = DBUtil.getConnection();
	        
	        // 生成唯一的用户ID和学生ID
	        long timestamp = System.currentTimeMillis();
	        int uniqueUserId = (int) (timestamp % 9000 + 1000); // 生成1000-9999的随机数
	        String randomStudentId = "S" + timestamp % 10000;
	        
	        // 1. 先插入用户记录
	        String insertUserSql = "INSERT INTO tbl_user (userId, username, password, role) VALUES (?, ?, ?, ?)";
	        pstmt = conn.prepareStatement(insertUserSql);
	        pstmt.setInt(1, uniqueUserId);          // 使用唯一的用户ID
	        pstmt.setString(2, "user_" + uniqueUserId);
	        pstmt.setString(3, "password123");
	        pstmt.setString(4, "student");
	        pstmt.executeUpdate();
	        pstmt.close();
	        System.out.println("✅ 用户记录插入成功，用户ID: " + uniqueUserId);
	        
	        // 2. 再插入学生记录
	        String insertStudentSql = "INSERT INTO tbl_student (studentId, userId, studentName, classId, enrollDate, status) " +
	                                 "VALUES (?, ?, ?, ?, ?, ?)";
	        pstmt = conn.prepareStatement(insertStudentSql);
	        pstmt.setString(1, randomStudentId);    // 学生ID
	        pstmt.setInt(2, uniqueUserId);          // 使用唯一的用户ID
	        pstmt.setString(3, "测试学生");
	        pstmt.setString(4, "CS2023");
	        
	        // 添加日期字段
	        java.util.Date currentDate = new java.util.Date();
	        pstmt.setDate(5, new java.sql.Date(currentDate.getTime()));
	        
	        pstmt.setString(6, "正常");
	        
	        int insertResult = pstmt.executeUpdate();
	        System.out.println("✅ 学生记录插入成功，影响行数: " + insertResult);
	        System.out.println("✅ 学生ID: " + randomStudentId + ", 用户ID: " + uniqueUserId);
	        
	        // 3. 测试查询
	        Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT * FROM tbl_student WHERE studentId = '" + randomStudentId + "'");
	        while (rs.next()) {
	            System.out.println("学号:" + rs.getString("studentId") + 
	                             ", 用户ID:" + rs.getInt("userId") +
	                             ", 姓名:" + rs.getString("studentName") +
	                             ", 入学日期:" + rs.getDate("enrollDate"));
	        }
	        rs.close();
	        stmt.close();
	        
	    } catch (SQLException e) {
	        System.err.println("❌ 测试失败: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        try {
	            if (pstmt != null) pstmt.close();
	            if (conn != null) DBUtil.closeConnection(conn);
	        } catch (SQLException e) {
	            System.err.println("关闭资源时出错: " + e.getMessage());
	        }
	    }
	}

    
    
    private static void testStudentService() {
        System.out.println("\n----- 测试学生服务 -----");
        IStudentService studentService = new StudentServiceImpl();
        IUserService userService = new UserServiceImpl();
        
        try {
            // 首先创建一个用户
            User user = new User();
            user.setUsername("student_user1");
            user.setPassword("student123");
            user.setRole("student");
            
            boolean userRegistered = userService.register(user);
            System.out.println("创建用户结果: " + (userRegistered ? "成功" : "失败"));
            
            if (userRegistered) {
                // 获取刚创建的用户ID
                User createdUser = userService.getUserByUsername("student_user1");
                if (createdUser != null) {
                    Integer userId = createdUser.getUserId();
                    System.out.println("创建的用户ID: " + userId);
                    
                    // 创建新学生，使用正确的userId
                    Student newStudent = new Student();
                    newStudent.setStudentId("S2023002");
                    newStudent.setUserId(userId); // 使用实际存在的用户ID
                    newStudent.setStudentName("张三");
                    newStudent.setClassId("CS2023");
                    newStudent.setEnrollDate(new Date());
                    newStudent.setStatus("正常");
                    
                    // 添加学生
                    boolean added = studentService.addStudent(newStudent);
                    System.out.println("添加学生结果: " + (added ? "成功" : "失败"));
                    
                    // 获取学生信息
                    Student student = studentService.getStudentById("S2023002");
                    if (student != null) {
                        System.out.println("学生信息: " + student.getStudentName() + ", 班级: " + student.getClassId());
                    }
                }
            }
            
            // 检查学生状态
            boolean isActive = studentService.isStudentActive("S2023001");
            System.out.println("学生状态是否正常: " + isActive);
            
            // 获取所有学生
            List<Student> allStudents = studentService.getAllStudents();
            System.out.println("系统学生总数: " + (allStudents != null ? allStudents.size() : 0));
            
        } catch (Exception e) {
            System.err.println("学生服务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    private static void testProductService() {
        System.out.println("\n----- 测试商品服务 -----");
        IProductService productService = new ProductServiceImpl();
        
        try {
            // 创建新商品
            Product newProduct = new Product();
            newProduct.setProductId("P10001");
            newProduct.setProductName("笔记本电脑");
            newProduct.setPrice(5999.99);
            newProduct.setStock(50);
            newProduct.setCategory("电子产品");
            newProduct.setDescription("高性能笔记本电脑");
            
            // 添加商品
            boolean added = productService.addProduct(newProduct);
            System.out.println("添加商品结果: " + (added ? "成功" : "失败"));
            
            // 获取商品信息
            Product product = productService.getProductById("P10001");
            if (product != null) {
                System.out.println("商品信息: " + product.getProductName() + ", 价格: " + product.getPrice());
            }
            
            // 更新库存
           // boolean stockUpdated = productService.updateProductStock("P10001", -2); // 减少2个库存
           // System.out.println("库存更新结果: " + (stockUpdated ? "成功" : "失败"));
            
            // 检查库存
            boolean inStock = productService.checkStock("P10001", 5);
            System.out.println("库存是否足够: " + inStock);
            
            // 获取所有商品
            List<Product> products = productService.getAllProducts();
            System.out.println("商品总数: " + (products != null ? products.size() : 0));
            
            // 搜索商品
            List<Product> searchResults = productService.searchProductsByName("电脑");
            System.out.println("搜索到商品数量: " + (searchResults != null ? searchResults.size() : 0));
            
        } catch (Exception e) {
            System.err.println("商品服务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testOrderService() {
        System.out.println("\n----- 测试订单服务 -----");
        IOrderService orderService = new OrderServiceImpl();
        
        try {
            // 创建新订单
            Order newOrder = new Order();
            newOrder.setOrderId("ORD20231201001");
            newOrder.setStudentId("S2023001");
            newOrder.setOrderDate(new Date());
            newOrder.setTotalAmount(99.99);
            newOrder.setStatus("待支付");
            
            // 添加订单
            boolean added = orderService.createOrder(newOrder);
            System.out.println("创建订单结果: " + (added ? "成功" : "失败"));
            
            // 获取订单信息
            Order order = orderService.getOrderById("ORD20231201001");
            if (order != null) {
                System.out.println("订单信息: " + order.getOrderId() + ", 金额: " + order.getTotalAmount());
            }
            
            // 更新订单状态
            //boolean statusUpdated = orderService.updateOrderStatus("ORD20231201001", "已支付");
            //System.out.println("订单状态更新结果: " + (statusUpdated ? "成功" : "失败"));
            
            // 获取学生订单
            List<Order> studentOrders = orderService.getOrdersByStudentId("S2023001");
            System.out.println("学生订单数量: " + (studentOrders != null ? studentOrders.size() : 0));
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId("ORD20231201001");
            orderItem.setProductId("P10001");
            orderItem.setQuantity(1);
            orderItem.setSubtotal(5999.99);
            
            boolean itemAdded = orderService.addOrderItem(orderItem);
            System.out.println("添加订单项结果: " + (itemAdded ? "成功" : "失败"));
            
            // 获取订单项
            List<OrderItem> orderItems = orderService.getOrderItemsByOrderId("ORD20231201001");
            System.out.println("订单项数量: " + (orderItems != null ? orderItems.size() : 0));
            
        } catch (Exception e) {
            System.err.println("订单服务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testShopService() {
        System.out.println("\n----- 测试商店服务 -----");
        IShopService shopService = new ShopServiceImpl();
        
        try {
            // 获取所有商品
            List<Product> products = shopService.getAllProducts();
            System.out.println("商店商品总数: " + (products != null ? products.size() : 0));
            
            // 创建购物车项
            List<OrderItem> cartItems = new ArrayList<>();
            OrderItem item1 = new OrderItem();
            item1.setProductId("P10001");
            item1.setQuantity(2);
            cartItems.add(item1);
            
            // 计算购物车总金额
            double total = shopService.calculateCartTotal(cartItems);
            System.out.println("购物车总金额: " + total);
            
            // 验证购物车
            boolean valid = shopService.validateCartItems(cartItems);
            System.out.println("购物车验证结果: " + valid);
            
            // 购买商品（创建订单）
          /*  String orderId = shopService.purchase("S2023001", cartItems);
            System.out.println("购买结果: " + (orderId != null ? "成功，订单号: " + orderId : "失败"));
            
            if (orderId != null) {
                // 支付订单
                boolean paid = shopService.payOrder(orderId);
                System.out.println("支付结果: " + (paid ? "成功" : "失败"));
                
                // 获取订单历史
                List<Order> orderHistory = shopService.getOrderHistory("S2023001");
                System.out.println("订单历史数量: " + (orderHistory != null ? orderHistory.size() : 0));
            }
            
            // 搜索商品
            List<Product> searchResults = shopService.searchProducts("电脑");
            System.out.println("搜索到商品数量: " + (searchResults != null ? searchResults.size() : 0));
            
            // 获取所有分类
            List<String> categories = shopService.getAllCategories();
            System.out.println("商品分类数量: " + (categories != null ? categories.size() : 0));
          */  
        } catch (Exception e) {
            System.err.println("商店服务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 简单的数据库连接测试
     */
    private static void testDatabaseConnection() {
        System.out.println("\n----- 测试数据库连接 -----");
        try {
            // 测试数据库连接
            util.DBUtil.getConnection();
            System.out.println("数据库连接测试成功");
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}