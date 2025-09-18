package com.vCampus.view;

import java.io.IOException; // 导入 IOException
import java.net.URL;
import java.util.ResourceBundle;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.entity.User; // 导入 User 类
import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;
import javafx.application.Platform; // 导入 Platform
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // 导入 FXMLLoader
import javafx.scene.Parent; // 导入 Parent
import javafx.scene.Scene; // 导入 Scene
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage; // 导入 Stage


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

        // 执行登录
        try {
            IUserService userService = ServiceFactory.getUserService();
            User loggedInUser = userService.login(username, password); // 修改变量名

            if (loggedInUser != null) {
                showSuccess("登录成功！欢迎 " + loggedInUser.getUsername());

                // 修复：将用户角色传递给 MainController
                try {
  // 加载主界面 FXML
  FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
  Parent root = loader.load();

  // 获取 MainController 实例
  MainController mainController = loader.getController();
  // 设置当前用户及其角色
  mainController.setCurrentUserAndRole(loggedInUser.getUsername(), loggedInUser.getRole());

  // 配置主舞台
  Stage currentStage = getCurrentStage();
  currentStage.setTitle("vCampus主界面 - " + loggedInUser.getUsername());
  currentStage.setScene(new Scene(root));
  currentStage.show();
  currentStage.toFront();

                } catch (IOException ioException) {
  System.err.println("❌ 加载主界面失败: " + ioException.getMessage());
  ioException.printStackTrace();
  showError("无法加载主界面: " + ioException.getMessage());
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