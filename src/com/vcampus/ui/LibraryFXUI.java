package com.vcampus.ui;

import com.vcampus.controller.LibraryController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * 图书馆系统 - JavaFX主界面
 * 使用你现有的LibraryController
 */
public class LibraryFXUI extends Application {
    
    // 重用你现有的控制器！
    private LibraryController controller;
    private String currentUser;
    private Stage primaryStage;
    
    public static void main(String[] args) {
        // 启动JavaFX应用
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.controller = new LibraryController(); // 使用你的控制器
        this.currentUser = null;
        
        setupPrimaryStage();
        showLoginScreen();
    }
    
    /**
     * 设置主窗口属性
     */
    private void setupPrimaryStage() {
        primaryStage.setTitle("🏫 虚拟校园图书馆系统");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
    }
    
    /**
     * 显示登录界面
     */
    private void showLoginScreen() {
        // 创建主布局
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(40));
        loginLayout.setStyle("-fx-background-color: #f8f9fa;");
        
        // 标题
        Label titleLabel = new Label("图书馆管理系统");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // 登录表单
        GridPane loginForm = createLoginForm();
        
        // 状态提示
        Label statusLabel = new Label("请输入学号登录系统");
        statusLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        loginLayout.getChildren().addAll(titleLabel, loginForm, statusLabel);
        
        Scene scene = new Scene(loginLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * 创建登录表单
     */
    private GridPane createLoginForm() {
        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
        
        // 学号标签
        Label studentIdLabel = new Label("学号:");
        studentIdLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 学号输入框
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("请输入你的学号");
        studentIdField.setStyle("-fx-font-size: 14px; -fx-pref-width: 200px;");
        
        // 登录按钮
        Button loginButton = new Button("登录");
        loginButton.setStyle("-fx-font-size: 14px; -fx-background-color: #3498db; -fx-text-fill: white;");
        loginButton.setDefaultButton(true);
        
        // 状态标签
        Label statusLabel = new Label();
        
        // 布局
        form.add(studentIdLabel, 0, 0);
        form.add(studentIdField, 1, 0);
        form.add(loginButton, 1, 1);
        form.add(statusLabel, 1, 2);
        
        // 登录事件 - 调用你的控制器！
        loginButton.setOnAction(event -> {
            String studentId = studentIdField.getText().trim();
            
            if (studentId.isEmpty()) {
                statusLabel.setText("❌ 学号不能为空");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
            
            // 使用你的控制器验证（这里简单直接登录）
            currentUser = studentId;
            statusLabel.setText("✅ 登录成功！欢迎 " + studentId);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            
            // 显示主界面
            showMainInterface();
        });
        
        // 回车键登录
        studentIdField.setOnAction(event -> loginButton.fire());
        
        return form;
    }
    
    /**
     * 显示主界面
     */
    private void showMainInterface() {
        // 创建选项卡界面
        TabPane tabPane = new TabPane();
        
        // 创建各个功能标签页
        Tab borrowTab = new Tab("🤝 借书", createBorrowTab());
        Tab returnTab = new Tab("↩️ 还书", createReturnTab());
        Tab queryTab = new Tab("👤 我的借阅", createQueryTab());
        Tab statsTab = new Tab("📊 统计", createStatsTab());
        
        // 设置标签页不可关闭
        borrowTab.setClosable(false);
        returnTab.setClosable(false);
        queryTab.setClosable(false);
        statsTab.setClosable(false);
        
        tabPane.getTabs().addAll(borrowTab, returnTab, queryTab, statsTab);
        
        Scene scene = new Scene(tabPane);
        primaryStage.setScene(scene);
    }
    
    /**
     * 创建借书标签页
     */
    private VBox createBorrowTab() {
        VBox borrowTab = new VBox(15);
        borrowTab.setPadding(new Insets(20));
        borrowTab.setAlignment(Pos.TOP_CENTER);
        
        Label title = new Label("借书功能");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // 使用你的控制器的借书表单
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        
        Label bookIdLabel = new Label("图书ID:");
        TextField bookIdField = new TextField();
        
        Label daysLabel = new Label("借阅天数:");
        TextField daysField = new TextField();
        
        Button borrowButton = new Button("借书");
        borrowButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        Label resultLabel = new Label();
        
        // 调用你的控制器方法！
        borrowButton.setOnAction(event -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText());
                int days = Integer.parseInt(daysField.getText());
                
                // 这里调用你写好的控制器方法！
                boolean success = controller.borrowBook(bookId, currentUser, days);
                
                if (success) {
                    resultLabel.setText("✅ 借书成功！");
                    resultLabel.setStyle("-fx-text-fill: green;");
                    bookIdField.clear();
                    daysField.clear();
                } else {
                    resultLabel.setText("❌ 借书失败");
                    resultLabel.setStyle("-fx-text-fill: red;");
                }
            } catch (NumberFormatException e) {
                resultLabel.setText("❌ 请输入有效的数字");
                resultLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        form.add(bookIdLabel, 0, 0);
        form.add(bookIdField, 1, 0);
        form.add(daysLabel, 0, 1);
        form.add(daysField, 1, 1);
        form.add(borrowButton, 1, 2);
        form.add(resultLabel, 1, 3);
        
        borrowTab.getChildren().addAll(title, form);
        return borrowTab;
    }
    
    /**
     * 创建还书标签页
     */
    private VBox createReturnTab() {
        VBox returnTab = new VBox(15);
        returnTab.setPadding(new Insets(20));
        returnTab.setAlignment(Pos.TOP_CENTER);
        
        Label title = new Label("还书功能");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        
        Label bookIdLabel = new Label("图书ID:");
        TextField bookIdField = new TextField();
        
        Button returnButton = new Button("还书");
        returnButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        Label resultLabel = new Label();
        
        // 调用你的控制器方法！
        returnButton.setOnAction(event -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText());
                
                // 这里调用你写好的控制器方法！
                boolean success = controller.returnBook(currentUser, bookId);
                
                if (success) {
                    resultLabel.setText("✅ 还书成功！");
                    resultLabel.setStyle("-fx-text-fill: green;");
                    bookIdField.clear();
                } else {
                    resultLabel.setText("❌ 还书失败");
                    resultLabel.setStyle("-fx-text-fill: red;");
                }
            } catch (NumberFormatException e) {
                resultLabel.setText("❌ 请输入有效的图书ID");
                resultLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        form.add(bookIdLabel, 0, 0);
        form.add(bookIdField, 1, 0);
        form.add(returnButton, 1, 1);
        form.add(resultLabel, 1, 2);
        
        returnTab.getChildren().addAll(title, form);
        return returnTab;
    }
    
    /**
     * 创建查询标签页
     */
    private VBox createQueryTab() {
        VBox queryTab = new VBox(15);
        queryTab.setPadding(new Insets(20));
        
        Label title = new Label("我的借阅记录 - " + currentUser);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        TextArea recordsArea = new TextArea();
        recordsArea.setEditable(false);
        recordsArea.setPrefHeight(400);
        recordsArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        
        Button refreshButton = new Button("刷新记录");
        refreshButton.setOnAction(event -> {
            // 调用你的控制器方法显示记录！
            StringBuilder sb = new StringBuilder();
            sb.append("📋 借阅记录 for ").append(currentUser).append("\n\n");
            sb.append(controller.getUserBorrowStats(currentUser)).append("\n\n");
            
            // 这里可以添加更多详细信息
            sb.append("详细借阅记录功能待完善...\n");
            
            recordsArea.setText(sb.toString());
        });
        
        queryTab.getChildren().addAll(title, recordsArea, refreshButton);
        return queryTab;
    }
    
    /**
     * 创建统计标签页
     */
    private VBox createStatsTab() {
        VBox statsTab = new VBox(15);
        statsTab.setPadding(new Insets(20));
        
        Label title = new Label("借阅统计");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        TextArea statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefHeight(400);
        statsArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        
        Button refreshButton = new Button("刷新统计");
        refreshButton.setOnAction(event -> {
            // 调用你的控制器方法！
            String stats = controller.getLibraryStats() + "\n\n" +
                          controller.getUserBorrowStats(currentUser);
            statsArea.setText(stats);
        });
        
        statsTab.getChildren().addAll(title, statsArea, refreshButton);
        return statsTab;
    }
}