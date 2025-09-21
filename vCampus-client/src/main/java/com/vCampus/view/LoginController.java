package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Admin; // 导入 Admin 类
import com.vCampus.entity.Student; // 导入 Student 类
import com.vCampus.entity.Teacher; // 导入 Teacher 类
import com.vCampus.entity.User; // 导入 User 类
import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;
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
            IUserService userService = ServiceFactory.getUserService();
            User genericUser = userService.login(username, password); // 获取通用 User 对象（支持多角色）

            if (genericUser != null) {
                // 将通用 User 放入会话，并设置激活角色为主角色（在 SessionContext 内部完成）
                SessionContext.setCurrentUser(genericUser);
                // 可选：预加载主角色对应的详情（不改变会话对象类型）
                try {
                    String primary = genericUser.getPrimaryRole();
                    if ("STUDENT".equalsIgnoreCase(primary)) {
                        ServiceFactory.getStudentService().getByUserId(genericUser.getUserId());
                    } else if ("TEACHER".equalsIgnoreCase(primary)) {
                        ServiceFactory.getTeacherService().getByUserId(genericUser.getUserId());
                    } else if ("ADMIN".equalsIgnoreCase(primary)) {
                        ServiceFactory.getAdminService().getByUserId(genericUser.getUserId());
                    }
                } catch (Exception ignored) {}

                showSuccess("登录成功！欢迎 " + genericUser.getUsername());

                NavigationUtil.navigateTo(
                    getCurrentStage(),
                    "main-view.fxml",
                    "vCampus主界面 - " + genericUser.getUsername()
                );
            } else {
                showError("用户名或密码错误");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            showError("登录失败: " + e.getMessage());
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
}