package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.common.SessionContext;
import com.vCampus.util.RBACUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.Node;
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
    @FXML private MenuItem miShop;
    @FXML private MenuItem miShopAdmin;
    @FXML private MenuItem miChoose;
    @FXML private ComboBox<String> roleSwitcher;
    @FXML private Button btnUserMgmt;
    @FXML private Button btnStudentMgmt;
    @FXML private Button btnCourseMgmt;
    @FXML private Button btnLibrary;
    @FXML private Button btnShop;
    @FXML private Button btnShopAdmin;
    @FXML private Button btnLibraryAdmin;
    @FXML private Button btnChoose;
    
    // 当前用户信息
    private String currentUsername;
    
    /**
     * 初始化主界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeLabel.setText("欢迎使用 vCampus 系统");
        statusLabel.setText("就绪");
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
        statusLabel.setText("学籍管理模块");
    }
    
    /**
     * 课程管理菜单点击
     */
    @FXML
    private void onCourseManagement() {
        var user = SessionContext.getCurrentUser();
        if (!RBACUtil.canManageCourses(user)) {
            showWarning("需要教师或管理员权限");
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
        var user = SessionContext.getCurrentUser();
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
    }

    @FXML
    private void onShop() {
        loadContent("shop-view.fxml");
        statusLabel.setText("商店");
    }

    @FXML
    private void onShopAdmin() {
        var user = SessionContext.getCurrentUser();
        if (!com.vCampus.util.RBACUtil.isAdmin(user)) { showWarning("需要管理员权限"); return; }
        loadContent("shop-admin-view.fxml");
        statusLabel.setText("商店管理");
    }

    /**
     * 选课系统菜单点击（合并分支：使用RBAC判定学生或管理员可访问）
     */
    @FXML
    private void onChoose() {
        var user = SessionContext.getCurrentUser();
        boolean allowed = RBACUtil.isStudent(user) || RBACUtil.isAdmin(user);
        if (!allowed) { showWarning("需要学生或管理员权限"); return; }
        loadContent("flash-grab-view.fxml");
        statusLabel.setText("抢课系统");
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
                SessionContext.clear();
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
        String active = SessionContext.getActiveRole();
        boolean isAdmin = active != null && active.equalsIgnoreCase("ADMIN");
        boolean isTeacher = active != null && active.equalsIgnoreCase("TEACHER");
        boolean isStudent = active != null && active.equalsIgnoreCase("STUDENT");

        boolean canUserMgmt = isAdmin;
        boolean canCourse = isTeacher || isAdmin;
        boolean canLibrary = isStudent || isTeacher || isAdmin;
        boolean canLibraryAdmin = isAdmin;
        boolean canChoose = isStudent || isAdmin;

        if (miUserMgmt != null) miUserMgmt.setDisable(!canUserMgmt);
        if (miStudentMgmt != null) miStudentMgmt.setDisable(!(canCourse || canUserMgmt));
        if (miCourseMgmt != null) miCourseMgmt.setDisable(!canCourse);
        if (miLibrary != null) miLibrary.setDisable(!canLibrary);
        if (miLibraryAdmin != null) miLibraryAdmin.setDisable(!canLibraryAdmin);
        if (miShop != null) miShop.setDisable(false);
        if (miShopAdmin != null) miShopAdmin.setDisable(!isAdmin);
        if (miChoose != null) miChoose.setDisable(!canChoose);

        // 左侧功能导航：根据角色显示/隐藏并释放布局空间
        setNodeVisible(btnUserMgmt, canUserMgmt);
        setNodeVisible(btnStudentMgmt, (canCourse || canUserMgmt));
        setNodeVisible(btnCourseMgmt, canCourse);
        setNodeVisible(btnLibrary, canLibrary);
        setNodeVisible(btnLibraryAdmin, canLibraryAdmin);
        setNodeVisible(btnShop, true);
        setNodeVisible(btnShopAdmin, isAdmin);
        setNodeVisible(btnChoose, canChoose);
    }

    private void setNodeVisible(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }
}