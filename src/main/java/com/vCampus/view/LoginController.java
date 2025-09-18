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
            User genericUser = userService.login(username, password); // 获取通用 User 对象

            if (genericUser != null) {
                // 根据通用 User 对象的角色，获取对应的具体子类对象
                User specificUser = null; // 用于存储具体的学生/教师/管理员对象

                switch (genericUser.getRole()) {
  case "STUDENT":
  specificUser = ServiceFactory.getStudentService().getByUserId(genericUser.getUserId());
  break;
  case "TEACHER":
  specificUser = ServiceFactory.getTeacherService().getByUserId(genericUser.getUserId());
  break;
  case "ADMIN":
  specificUser = ServiceFactory.getAdminService().getByUserId(genericUser.getUserId());
  break;
  default:
  // 如果角色未知或没有对应的子类，则仍然使用通用 User
  specificUser = genericUser;
                }

                if (specificUser != null) {
  SessionContext.setCurrentUser(specificUser); // 将具体的子类对象存入 SessionContext
  showSuccess("登录成功！欢迎 " + specificUser.getUsername());

  NavigationUtil.navigateTo(
  getCurrentStage(),
  "main-view.fxml",
  "vCampus主界面 - " + specificUser.getUsername()
  );
                } else {
  // 登录成功但无法获取具体的角色信息，可能是数据不一致
  showError("登录失败: 无法获取完整的用户角色信息");
  passwordField.clear();
  passwordField.requestFocus();
                }

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