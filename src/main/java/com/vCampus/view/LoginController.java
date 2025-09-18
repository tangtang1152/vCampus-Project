package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.common.RoleSwitcher;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.User;
import com.vCampus.service.IUserService;
import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;
import java.util.Set;
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
            var user = userService.login(username, password);
            if (user != null) {
                showSuccess("登录成功！欢迎 " + user.getUsername());
                // 保存到会话上下文
                SessionContext.setCurrentUser(user);
                
                // 确保用户有角色分配
                ensureUserHasRole(user);
                
                // 初始化角色切换器
                RoleSwitcher.initialize();
                
                // 跳转到主界面
                NavigationUtil.navigateTo(
                    getCurrentStage(),
                    "main-view.fxml", 
                    "vCampus主界面 - " + user.getUsername()
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
     * 确保用户有角色分配
     */
    private void ensureUserHasRole(User user) {
        try {
            IPermissionService permissionService = ServiceFactory.getPermissionService();
            Set<String> userRoles = permissionService.getUserRoleCodes(user.getUserId());
            
            if (userRoles.isEmpty()) {
                // 用户没有角色，根据用户类型自动分配
                String roleCode = convertRoleToEnglish(user.getRole());
                boolean success = permissionService.assignRoleToUser(user.getUserId(), roleCode, "SYSTEM");
                
                if (success) {
                    System.out.println("✅ 为用户 " + user.getUsername() + " 自动分配角色: " + roleCode);
                } else {
                    System.err.println("❌ 为用户 " + user.getUsername() + " 分配角色失败: " + roleCode);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 检查用户角色时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 将中文角色转换为英文角色代码
     */
    private String convertRoleToEnglish(String role) {
        switch (role) {
            case "学生":
                return "STUDENT";
            case "教师":
                return "TEACHER";
            case "管理员":
                return "ADMIN";
            default:
                return role; // 如果已经是英文，直接返回
        }
    }

    /**
     * 获取当前舞台
     */
    private javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) usernameField.getScene().getWindow();
    }
}