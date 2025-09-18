package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 * 应用程序的主框架界面
 */
public class MainController extends BaseController {

    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // FXML 绑定可以保留，但我们不会再调用它们的 setDisable() 方法
    @FXML private Menu manageMenu;
    @FXML private MenuItem userManagementMenuItem;
    @FXML private MenuItem studentManagementMenuItem;
    @FXML private MenuItem courseManagementMenuItem;

    @FXML private Button userManagementButton;
    @FXML private Button studentManagementButton;
    @FXML private Button courseManagementButton;


    private String currentUsername;
    private String currentUserRole; // 存储用户角色

    /**
     * 初始化主界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeLabel.setText("欢迎使用 vCampus 系统");
        statusLabel.setText("就绪");

        // 修复：移除所有默认禁用功能的代码。所有按钮默认都是启用的。
        // initialize() 方法中不再需要额外的功能启用/禁用逻辑
    }

    /**
     * 设置当前用户和角色
     */
    public void setCurrentUserAndRole(String username, String role) {
        this.currentUsername = username;
        this.currentUserRole = role;
        welcomeLabel.setText("欢迎, " + username + " (" + role + ")!");
        // 修复：登录后状态栏显示“就绪”，不显示权限受限信息
        statusLabel.setText("就绪");
    }

    /**
     * 辅助方法，用于进行权限检查并显示错误信息
     * @param featureName 功能名称（例如“用户管理”）
     * @return true 如果用户有权限，false 如果没有
     */
    private boolean checkAdminPermission(String featureName) {
        if ("ADMIN".equals(currentUserRole)) {
            return true;
        } else {
            showError("您没有权限访问" + featureName + "功能。");
            return false;
        }
    }


    /**
     * 用户管理菜单点击
     */
    @FXML
    private void onUserManagement() {
        if (checkAdminPermission("用户管理")) { // 修复：调用辅助方法进行权限检查
            NavigationUtil.showDialog("user-management-view.fxml", "用户管理");
        }
        // 如果没有权限，checkAdminPermission 会显示错误，无需在此处重复
    }

    /**
     * 学籍管理菜单点击
     */
    @FXML
    private void onStudentManagement() {
        if (checkAdminPermission("学籍管理")) { // 修复：调用辅助方法进行权限检查
            loadContent("student-management-view.fxml");
            statusLabel.setText("学籍管理模块");
        }
        // 如果没有权限，checkAdminPermission 会显示错误，无需在此处重复
    }

    /**
     * 课程管理菜单点击
     */
    @FXML
    private void onCourseManagement() {
        if (checkAdminPermission("课程管理")) { // 修复：调用辅助方法进行权限检查
            loadContent("course-management-view.fxml");
            statusLabel.setText("课程管理模块");
        }
        // 如果没有权限，checkAdminPermission 会显示错误，无需在此处重复
    }

    /**
     * 退出系统
     */
    @FXML
    private void onExit() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText("确定要退出系统吗？");
        alert.setContentText("您将返回到登录界面");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                this.currentUsername = null;
                this.currentUserRole = null;
                NavigationUtil.navigateTo(
  getCurrentStage(),
  "login-view.fxml",
  "用户登录 - vCampus"
                );
            }
        });
    }

    /**
     * 关于系统
     */
    @FXML
    private void onAbout() {
        showInformation("关于 vCampus",
                "vCampus 虚拟校园系统\n" +
  "版本: 1.0.0\n" +
  "开发团队: 您的团队名称\n" +
  "© 2024 版权所有"
        );
    }

    /**
     * 加载内容到主容器
     */
    private void loadContent(String fxmlPath) {
        try {
            var loader = new javafx.fxml.FXMLLoader(
  getClass().getResource("/fxml/" + fxmlPath));
            mainContainer.setCenter(loader.load());
        } catch (Exception e) {
            showError("加载界面失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取当前舞台
     */
    private javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) mainContainer.getScene().getWindow();
    }
}