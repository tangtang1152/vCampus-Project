package com.vCampus.app;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.vCampus.common.ConfigManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * vCampus 系统主应用程序入口
 * 负责初始化应用程序并启动主界面
 */
public class MainApp extends Application {
	
    @Override
    public void init() throws Exception {

        System.out.println("🔧 init() 方法被调用 - JavaFX 初始化开始");
        super.init();
    }
    
    private static Stage primaryStage;
    
    /**
     * 获取主舞台实例
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("🎬 start() 方法被调用 - JavaFX 启动开始");
        MainApp.primaryStage = primaryStage;
        
        try {
            // 配置主舞台
            configurePrimaryStage();
            
            // 显示登录界面
            showLoginView();
            
            System.out.println("✅ JavaFX 启动完成，界面应该显示");
            
        } catch (Exception e) {
            System.err.println("❌ JavaFX 启动失败: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("应用程序启动失败", e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        System.out.println("🛑 stop() 方法被调用 - 应用程序正在关闭");
        super.stop();
    }
    
    /**
     * 配置主舞台属性
     */
    private void configurePrimaryStage() {
        System.out.println("⚙️ 配置主舞台...");
        primaryStage.setTitle(ConfigManager.getAppTitle());
        primaryStage.setWidth(ConfigManager.getAppWidth());
        primaryStage.setHeight(ConfigManager.getAppHeight());
        primaryStage.setResizable(true);
        
        primaryStage.setX(100); // 设置X坐标
        primaryStage.setY(100); // 设置Y坐标
        primaryStage.centerOnScreen(); // 或者居中显示
        
        // 设置应用程序图标
        try {
            primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/images/app-icon.png")));
            System.out.println("✅ 应用程序图标加载成功");
        } catch (Exception e) {
            System.out.println("⚠️ 警告: 应用程序图标加载失败: " + e.getMessage());
        }
        
        // 设置关闭请求处理
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🗑️ 收到关闭请求");
            event.consume(); // 先消耗事件
            confirmExit();   // 显示确认退出对话框
        });
    }
    
    /**
     * 显示登录界面
     */
    private void showLoginView() {
        System.out.println("👤 尝试显示登录界面...");
        try {
            // 正确的路径：/fxml/login-view.fxml
            java.net.URL fxmlUrl = getClass().getResource("/fxml/login-view.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ FXML 文件未找到: /fxml/login-view.fxml");
                System.err.println("❌ 请检查文件是否在: src/main/resources/fxml/login-view.fxml");
                showErrorDialog("界面加载失败", "登录界面文件未找到");
                return;
            }
            System.out.println("✅ FXML 文件找到: " + fxmlUrl);
            
            // 加载 FXML
            Parent root = FXMLLoader.load(fxmlUrl);
            System.out.println("✅ FXML 加载成功");
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show(); // 确保显示
            primaryStage.toFront(); // 提到前面
            
            System.out.println("✅ 登录界面显示完成");
            
        } catch (Exception e) {
            System.err.println("❌ 登录界面显示失败: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("界面加载失败", e.getMessage());
        }
    }
    
    /**
     * 显示错误对话框
     */
    private void showErrorDialog(String title, String message) {
        System.out.println("❌ 显示错误对话框: " + title);
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * 确认退出应用程序
     */
    private void confirmExit() {
        System.out.println("❓ 显示退出确认对话框");
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("您确定要退出 vCampus 系统吗？");
            alert.setContentText("所有未保存的数据将会丢失。");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    System.out.println("👋 用户确认退出");
                    primaryStage.close();
                    System.exit(0);
                } else {
                    System.out.println("↩️ 用户取消退出");
                }
            });
        });
    }
    
    /**
     * 应用程序主方法
     */
    public static void main(String[] args) {
    	
		
        // 保存原始的System.err
        PrintStream originalErr = System.err;
        
        // 重定向System.err到空输出，完全隐藏所有错误信息
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // 完全丢弃所有错误输出
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String message = new String(b, off, len);
                // 只允许显示我们自己程序的错误，过滤掉所有UCanAccess和HSQLDB的错误
                if (message.contains("可替换") || message.contains("注册") || 
                    message.contains("登录") || message.contains("错误")) {
                    originalErr.write(b, off, len);
                }
                // 其他错误全部丢弃
            }
        }));
    	
        // 强制设置系统编码为 UTF-8
        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
            System.out.println("✅ 编码设置为 UTF-8");
        } catch (Exception e) {
            System.err.println("❌ 编码设置失败: " + e.getMessage());
        }
    	
        System.out.println("==========================================");
        System.out.println("🚀 vCampus 应用程序启动开始");
        System.out.println("==========================================");
        
        // 输出系统信息
        System.out.println("📋 系统信息:");
        System.out.println("   Java 版本: " + System.getProperty("java.version"));
        System.out.println("   系统编码: " + System.getProperty("file.encoding"));
        System.out.println("   用户目录: " + System.getProperty("user.dir"));
        System.out.println("   JavaFX 版本: " + System.getProperty("javafx.version", "未知"));
        
        
        System.out.println("📁 数据库路径: " + ConfigManager.getDatabasePath());
        
        // 检查 JavaFX 模块是否可用
        try {
            Class.forName("javafx.application.Application");
            System.out.println("✅ JavaFX 模块检测成功");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JavaFX 模块未找到: " + e.getMessage());
            System.exit(1);
        }
        
        // 检查数据库连接（注释掉以测试是否影响启动）
        // checkDatabaseConnection();
        
        System.out.println("🎯 准备调用 launch(args)...");
        System.out.println("==========================================");
        
        try {
            // 启动JavaFX应用
            launch(args);
            System.out.println("✅ launch(args) 调用完成");
        } catch (Exception e) {
            System.err.println("❌ launch(args) 调用失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("==========================================");
        System.out.println("🏁 main() 方法结束");
        System.out.println("==========================================");
    }
}