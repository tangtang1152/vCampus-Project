package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Admin; // 导入 Admin 类
import com.vCampus.entity.Student; // 导入 Student 类
import com.vCampus.entity.Teacher; // 导入 Teacher 类
import com.vCampus.entity.User; // 导入 User 类
import client.net.SocketClient;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 登录界面控制器
 * 处理用户登录逻辑
 */
public class LoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
        usernameField.requestFocus();
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLogin();
        }
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (!validateInput(username, password)) {
            showError("用户名和密码不能为空");
            usernameField.requestFocus();
            return;
        }

        if (username.length() < 1 || username.length() > 50) {
            showError("用户名长度必须在1-50个字符之间");
            usernameField.requestFocus();
            return;
        }

        try {
            // 构建登录请求
            String request = "LOGIN:" + username + ":" + password;
            String response = SocketClient.sendRequest(request);
            
            // 解析响应
            if (response != null && response.startsWith("SUCCESS:LOGIN:")) {
                String[] parts = response.split(":");
                if (parts.length >= 5) {
                    String role = parts[2];
                    String selfId = parts[3];
                    String userId = parts[4];
                    
                    // 创建用户对象
                    User user = createUserFromLogin(role, username, selfId, userId);
                    if (user != null) {
                        SessionContext.setCurrentUser(user);
                        showSuccess("登录成功！欢迎 " + user.getUsername());
                        
                        NavigationUtil.navigateTo(
                            getCurrentStage(),
                            "main-view.fxml",
                            "vCampus主界面 - " + user.getUsername()
                        );
                    } else {
                        showError("登录失败: 无法创建用户对象");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                } else {
                    showError("登录失败: 服务器响应格式错误");
                    passwordField.clear();
                    passwordField.requestFocus();
                }
            } else {
                showError("用户名或密码错误");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            showError("网络连接失败，请检查服务器是否启动");
            e.printStackTrace();
        }
    }

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

    @FXML
    private void onForgotPassword() {
        showInformation("忘记密码", "请联系系统管理员重置密码");
    }

    private javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) usernameField.getScene().getWindow();
    }

    private boolean validateInput(String username, String password) {
        return username != null && !username.isEmpty() &&
               password != null && !password.isEmpty();
    }

    /**
     * 根据登录响应创建用户对象
     */
    private User createUserFromLogin(String role, String username, String selfId, String userId) {
        try {
            Integer userIdInt = Integer.parseInt(userId);
            
            switch (role.toUpperCase()) {
                case "STUDENT":
                    Student student = new Student();
                    student.setUserId(userIdInt);
                    student.setUsername(username);
                    student.setRole("STUDENT");
                    student.setStudentId(selfId);
                    return student;
                case "TEACHER":
                    Teacher teacher = new Teacher();
                    teacher.setUserId(userIdInt);
                    teacher.setUsername(username);
                    teacher.setRole("TEACHER");
                    teacher.setTeacherId(selfId);
                    return teacher;
                case "ADMIN":
                    Admin admin = new Admin();
                    admin.setUserId(userIdInt);
                    admin.setUsername(username);
                    admin.setRole("ADMIN");
                    admin.setAdminId(selfId);
                    return admin;
                default:
                    // 创建通用用户对象
                    User user = new User();
                    user.setUserId(userIdInt);
                    user.setUsername(username);
                    user.setRole(role);
                    return user;
            }
        } catch (NumberFormatException e) {
            System.err.println("解析userId失败: " + userId);
            // 如果解析失败，创建没有userId的用户对象
            User user = new User();
            user.setUsername(username);
            user.setRole(role);
            return user;
        }
    }
}