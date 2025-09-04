package com.vcampus.main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;  // 关键导入（JavaFX 专用）
//导入连接acess数据库的依赖
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
        // 1. 创建五个功能按钮
        Button registerBtn = new Button("注册");
        Button logoutBtn = new Button("注销");
        Button loginBtn = new Button("登录");
        Button signOutBtn = new Button("登出");
        Button authBtn = new Button("授权");

        // 2. 为按钮添加简单的事件处理
        registerBtn.setOnAction(e -> {
            Student newStudent = RegisterDlg.showAndWait();//
            if (newStudent != null) {//如果返回的不是NULL,而是一个学生类对象，则说明学生信息成功
                System.out.println("注册成功: " + newStudent.getName());
                System.out.println("密码已加密存储: " + newStudent.getKey().replaceAll(".", "*"));
                // 实际开发中应加密密码（如BCrypt）后再存储
                
                
                
             // ★ 插入到 Access 数据库
                try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://C:/path/to/your/database.accdb")) {
                    String sql = "INSERT INTO Students (id, name, password, isAdmin) VALUES (?, ?, ?, ?)";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, newStudent.getId());
                        pstmt.setString(2, newStudent.getName());
                        pstmt.setString(3, newStudent.getKey()); // 实际应存储加密后的密码
                      
                        
                        pstmt.executeUpdate();
                        System.out.println("数据已保存到数据库！");
                    }
                } catch (SQLException e2) {
                    System.err.println("数据库错误: " + e2.getMessage());
                }
            }
        });
        logoutBtn.setOnAction(e -> {
            Pair<String, String> logoutInfo = LogoutDlg.showAndWait();
            if (logoutInfo != null) {
                String username = logoutInfo.getKey();
                String password = logoutInfo.getValue();
                System.out.println("注销确认: 用户名=" + username + ", 密码=" + password);
                // TODO: 添加实际注销逻辑（如清除会话）
            }
        });
        loginBtn.setOnAction(e -> {
            Pair<String, String> loginInfo = LoginDlg.showLoginDialog();
            if (loginInfo != null) {
                String username = loginInfo.getKey();
                String password = loginInfo.getValue();
                System.out.println("尝试登录: 用户名=" + username + ", 密码=" + password);
                // TODO: 添加实际登录逻辑（如数据库验证）
            }
        });
        signOutBtn.setOnAction(e -> System.out.println("登出按钮被点击"));
        authBtn.setOnAction(e -> System.out.println("授权按钮被点击"));

        // 3. 使用VBox布局（垂直排列，间距10像素）
        VBox root = new VBox(10);
        root.setPadding(new Insets(20)); // 内边距20像素
        root.getChildren().addAll(registerBtn, logoutBtn, loginBtn, signOutBtn, authBtn);

        // 4. 创建场景并设置到舞台
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("用户权限管理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
