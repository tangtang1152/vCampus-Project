package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    
    // 当前用户信息
    private String currentUsername;
    
    /**
     * 初始化主界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 可以在这里加载用户信息
        welcomeLabel.setText("欢迎使用 vCampus 系统");
        statusLabel.setText("就绪");
    }
    
    /**
     * 设置当前用户
     */
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("欢迎, " + username + "!");
    }
    
    /**
     * 用户管理菜单点击
     */
    @FXML
    private void onUserManagement() {
        NavigationUtil.showDialog("user-management-view.fxml", "用户管理");
    }
    
    /**
     * 学生管理菜单点击
     */
    @FXML
    private void onStudentManagement() {
        loadContent("student-management-view.fxml");
        statusLabel.setText("学生管理模块");
    }
    
    /**
     * 课程管理菜单点击
     */
    @FXML
    private void onCourseManagement() {
        loadContent("course-management-view.fxml");
        statusLabel.setText("课程管理模块");
    }

    /**
     * 管理员图书维护
     */
    @FXML
    private void onLibraryAdmin() {
        var user = com.vCampus.common.SessionContext.getCurrentUser();
        if (user == null || user.getRole() == null || (!user.getRole().toLowerCase().contains("admin") && !user.getRole().contains("管理员"))) {
            showWarning("需要管理员权限");
            return;
        }
        loadContent("library-admin-view.fxml");
        statusLabel.setText("图书维护");
    }

    /**
     * 图书馆菜单点击
     */
    @FXML
    private void onLibrary() {
        loadContent("library-view.fxml");
        statusLabel.setText("图书馆模块");
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
                com.vCampus.common.SessionContext.clear();
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