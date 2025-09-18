package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

import client.net.*;

/**
 * 登录界面控制器
 * 处理用户登录逻辑
 */
public class LoginController extends BaseController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    /**
     * 初始化方法
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置回车键登录
        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
        
        // 设置初始焦点
        usernameField.requestFocus();
    }
    
    /**
     * 处理键盘事件
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLogin();
        }
    }
    
    /**
     * 登录按钮点击事件
     */
    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // 输入验证
        if (!validateInput(username, password)) {//在common包里面的函数
            showError("用户名和密码不能为空");
            usernameField.requestFocus();
            return;
        }
        
        if (username.length() < 1 || username.length() > 50) {
            showError("用户名长度必须在1-50个字符之间");
            usernameField.requestFocus();
            return;
        }
        
        // 执行登录:涉及到读写数据库的操作,需要socket
        try {
            String request = "LOGIN|" + username + "|" + password;
            String response = SocketClient.sendRequest(request);

            // 处理空响应
            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("服务器无响应");
            }

            String[] parts = response.split("\\|");
            if (parts[0].equals("LOGIN_SUCCESS") && parts.length >= 2) {
                showSuccess("登录成功！欢迎 " + parts[1]);
                NavigationUtil.navigateTo(
                    getCurrentStage(),
                    "main-view.fxml", 
                    "vCampus主界面 - " + parts[1]
                );
            } else if (parts[0].equals("LOGIN_FAILED")) {
                showError("用户名或密码错误!");
                passwordField.clear();
                passwordField.requestFocus();
            } 
            else {
            	showError("未知响应");
            }
        } catch (Exception e) {
            showError("登录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 注册按钮点击事件
     */
    @FXML
    private void onRegister() {
        System.out.println("🎯 注册按钮被点击");
        
        try {
            System.out.println("🔍 尝试显示注册对话框...");
            NavigationUtil.showDialog("register-view.fxml", "用户注册");
            System.out.println("✅ 注册对话框调用完成");
        } catch (Exception e) {
            System.err.println("❌ 打开注册对话框失败: " + e.getMessage());
            e.printStackTrace();
            showError("无法打开注册界面: " + e.getMessage());
        }
    }
    /**
     * 忘记密码点击事件
     */
    @FXML
    private void onForgotPassword() {
        showInformation("忘记密码", "请联系系统管理员重置密码");
    }
    
    /**
     * 获取当前舞台
     */
    private javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) usernameField.getScene().getWindow();
    }
}