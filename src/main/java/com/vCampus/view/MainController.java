package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
<<<<<<< HEAD
import com.vCampus.common.SessionContext;
import com.vCampus.util.RBACUtil;
=======
import com.vCampus.common.SessionContext; // 确保引入 SessionContext
>>>>>>> refs/heads/feature/course-selection-finalllll
import javafx.fxml.FXML;
import javafx.scene.control.Label;
<<<<<<< HEAD
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ComboBox;
=======
import javafx.scene.control.Menu; // 确保引入 Menu
import javafx.scene.control.MenuItem; // 确保引入 MenuItem
>>>>>>> refs/heads/feature/course-selection-finalllll
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
    @FXML private Menu menuManage;
    @FXML private MenuItem miUserMgmt;
    @FXML private MenuItem miStudentMgmt;
    @FXML private MenuItem miCourseMgmt;
    @FXML private MenuItem miLibrary;
    @FXML private MenuItem miLibraryAdmin;
    @FXML private ComboBox<String> roleSwitcher;
    
    // 当前用户信息
    private String currentUsername;
    
    /**
     * 初始化主界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 可以在这里加载用户信息
        // 确保登录后设置 welcomeLabel, 例如:
        // var user = SessionContext.getCurrentUser();
        // if (user != null) {
        //     welcomeLabel.setText("欢迎, " + user.getUsername() + "!");
        // } else {
        //     welcomeLabel.setText("欢迎使用 vCampus 系统");
        // }
        welcomeLabel.setText("欢迎使用 vCampus 系统"); // 初始显示
        statusLabel.setText("就绪");

        // 初始化角色切换器
        initRoleSwitcherAndPermissions();
    }
    
    /**
     * 设置当前用户
     */
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("欢迎, " + username + "!");
        initRoleSwitcherAndPermissions();
    }
    
    /**
     * 用户管理菜单点击
     */
    @FXML
    private void onUserManagement() {
        var user = SessionContext.getCurrentUser();
        if (!RBACUtil.canManageUsers(user)) {
            showWarning("需要管理员权限");
            return;
        }
        loadContent("user-management-view.fxml");
        statusLabel.setText("用户管理模块");
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
    	var user = SessionContext.getCurrentUser();
        if (user == null || user.getRole() == null || (!user.getRole().toLowerCase().contains("admin") && !user.getRole().contains("管理员"))) {
            showWarning("需要管理员权限");
            return;
        }
        loadContent("course-management-view.fxml");
        statusLabel.setText("课程管理模块");
    }

    /**
     * 管理员图书维护
     */
    @FXML
    private void onLibraryAdmin() {
<<<<<<< HEAD
        var user = com.vCampus.common.SessionContext.getCurrentUser();
        if (!RBACUtil.canMaintainLibrary(user)) {
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
=======
        var user = SessionContext.getCurrentUser();
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
     * 选课系统菜单点击
     */
    @FXML // 新增的选课系统入口方法
    private void onChoose() {
        var user = SessionContext.getCurrentUser();

        if (user == null || user.getRole() == null || (!user.getRole().toLowerCase().contains("student") && !user.getRole().contains("学生"))) {
            showWarning("需要学生权限才能访问选课系统");
            return;
        }

        loadContent("choose-view.fxml");
        statusLabel.setText("选课系统");
>>>>>>> refs/heads/feature/course-selection-finalllll
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
<<<<<<< HEAD
                com.vCampus.common.SessionContext.clear();
=======
                SessionContext.clear(); // 清除会话信息
>>>>>>> refs/heads/feature/course-selection-finalllll
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

    private void initRoleSwitcherAndPermissions() {
        var user = SessionContext.getCurrentUser();
        if (roleSwitcher != null) {
            roleSwitcher.getItems().clear();
            if (user != null) {
                roleSwitcher.getItems().addAll(user.getRoleSet());
                String active = SessionContext.getActiveRole();
                if (active == null && !user.getRoleSet().isEmpty()) {
                    active = user.getPrimaryRole();
                }
                if (active != null) roleSwitcher.setValue(active);
                roleSwitcher.setOnAction(e -> {
                    String sel = roleSwitcher.getValue();
                    SessionContext.setActiveRole(sel);
                    applyPermissions();
                });
            }
        }
        applyPermissions();
    }

    private void applyPermissions() {
        var user = SessionContext.getCurrentUser();
        boolean canUserMgmt = RBACUtil.canManageUsers(user);
        boolean canCourse = RBACUtil.canManageCourses(user);
        boolean canLibrary = RBACUtil.canUseLibrary(user);
        boolean canLibraryAdmin = RBACUtil.canMaintainLibrary(user);

        if (miUserMgmt != null) miUserMgmt.setDisable(!canUserMgmt);
        if (miStudentMgmt != null) miStudentMgmt.setDisable(!(canCourse || canUserMgmt));
        if (miCourseMgmt != null) miCourseMgmt.setDisable(!canCourse);
        if (miLibrary != null) miLibrary.setDisable(!canLibrary);
        if (miLibraryAdmin != null) miLibraryAdmin.setDisable(!canLibraryAdmin);
    }
}