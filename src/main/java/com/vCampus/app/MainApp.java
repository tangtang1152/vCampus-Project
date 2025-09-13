package com.vCampus.app;

import java.sql.Connection;
import java.sql.SQLException;

import com.vCampus.common.ConfigManager;
import com.vCampus.common.NavigationUtil;
import com.vCampus.util.DBUtil;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * vCampus 系统主应用程序入口
 * 负责初始化应用程序并启动主界面
 */
public class MainApp extends Application {
    
    private static Stage primaryStage;
    
    /**
     * 获取主舞台实例
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    @Override
    public void start(Stage primaryStage) {
        MainApp.primaryStage = primaryStage;
        
        try {
            // 配置主舞台
            configurePrimaryStage();
            
            // 显示登录界面
            showLoginView();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("应用程序启动失败", e.getMessage());
        }
    }
    
    /**
     * 配置主舞台属性
     */
    private void configurePrimaryStage() {
        primaryStage.setTitle(ConfigManager.getAppTitle());
        primaryStage.setWidth(ConfigManager.getAppWidth());
        primaryStage.setHeight(ConfigManager.getAppHeight());
        primaryStage.setResizable(true);
        
        // 设置应用程序图标
        try {
            primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
            System.out.println("警告: 应用程序图标加载失败");
        }
        
        // 设置关闭请求处理
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // 先消耗事件
            confirmExit();   // 显示确认退出对话框
        });
    }
    
    /**
     * 显示登录界面
     */
    private void showLoginView() {
        NavigationUtil.navigateTo(primaryStage, "login-view.fxml", "用户登录 - vCampus");
    }
    
    /**
     * 显示错误对话框
     */
    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 确认退出应用程序
     */
    private void confirmExit() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText("您确定要退出 vCampus 系统吗？");
        alert.setContentText("所有未保存的数据将会丢失。");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                primaryStage.close();
                System.exit(0);
            }
        });
    }
    
    /**
     * 应用程序主方法
     */
    public static void main(String[] args) {
        // 设置系统编码为 UTF-8
        System.setProperty("file.encoding", "UTF-8");
    	System.out.println("🚀 启动 vCampus 虚拟校园系统...");
        System.out.println("📁 数据库路径: " + ConfigManager.getDatabasePath());
        
        // 检查数据库连接
        //checkDatabaseConnection();
        
        // 启动JavaFX应用
        launch(args);
    }
    
    /**
     * 检查数据库连接
     */
    private static void checkDatabaseConnection() {
        System.out.println("========== 数据库连接测试 ==========");
        try {
            // 这里可以添加数据库连接测试
            Connection conn = DBUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
            	System.out.println("✅ 数据库连接检查通过");
                System.out.println("数据库URL: " + conn.getMetaData().getURL());
                conn.close();
            } else {
                System.out.println("❌ 数据库连接失败");
            }
        } catch (Exception e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            showStartupError("数据库连接失败", "请检查数据库文件是否存在且可访问");
        }
        System.out.println("========== 测试结束 ==========");
    }
    
    /**
     * 显示启动错误对话框
     */
    private static void showStartupError(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("启动错误");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
            System.exit(1);
        });
    }
}