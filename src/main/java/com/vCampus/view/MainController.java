package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.common.RoleSwitcher;
import com.vCampus.common.SessionContext;
import com.vCampus.util.PermissionUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 * 应用程序的主框架界面
 */
public class MainController extends BaseController {
    
    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Button btnLibraryAdmin;        // 左侧按钮（管理员）
    @FXML private MenuItem menuLibraryAdmin;     // 顶部菜单项（管理员）
    @FXML private Button btnShopManagement;      // 左侧按钮（管理员：商品管理）
    @FXML private MenuItem menuShopManagement;   // 顶部菜单项（管理员：商品管理）
    @FXML private VBox leftNavigation;           // 左侧导航区域
    @FXML private ComboBox<String> roleSwitcher; // 角色切换器
    
    // 当前用户信息
    private String currentUsername;
    
    /**
     * 初始化主界面
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化用户信息
        updateUserInfo();
        
        // 初始化角色切换器
        initializeRoleSwitcher();
        
        // 初始化动态导航菜单
        initializeDynamicNavigation();
        
        // 根据权限控制菜单可见性
        updateMenuVisibility();
    }
    
    /**
     * 更新用户信息
     */
    private void updateUserInfo() {
        try {
            var user = SessionContext.getCurrentUser();
            if (user != null) {
                this.currentUsername = user.getUsername();
                welcomeLabel.setText("欢迎, " + user.getUsername() + "!");
                statusLabel.setText("就绪");
            } else {
                welcomeLabel.setText("欢迎使用 vCampus 系统");
                statusLabel.setText("未登录");
            }
        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化角色切换器
     */
    private void initializeRoleSwitcher() {
        try {
            if (roleSwitcher != null) {
                List<String> availableRoles = RoleSwitcher.getAvailableRoles();
                roleSwitcher.getItems().clear();
                
                // 添加角色显示名称到下拉框
                for (String roleCode : availableRoles) {
                    String displayName = RoleSwitcher.getRoleDisplayName(roleCode);
                    roleSwitcher.getItems().add(displayName);
                }
                
                String currentRole = RoleSwitcher.getCurrentActiveRole();
                if (currentRole != null) {
                    roleSwitcher.setValue(RoleSwitcher.getRoleDisplayName(currentRole));
                }
                
                roleSwitcher.setOnAction(e -> onRoleChanged());
                
                System.out.println("✅ 角色切换器初始化完成，可用角色: " + availableRoles);
            }
        } catch (Exception e) {
            System.err.println("初始化角色切换器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化动态导航菜单
     */
    private void initializeDynamicNavigation() {
        try {
            if (leftNavigation != null) {
                // 清除现有按钮
                leftNavigation.getChildren().clear();
                
                // 添加标题
                Label titleLabel = new Label("功能导航");
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                leftNavigation.getChildren().add(titleLabel);
                
                // 添加分隔线
                Separator separator = new Separator();
                leftNavigation.getChildren().add(separator);
                
                // 根据权限添加菜单项
                addNavigationButton("用户管理", this::onUserManagement, PermissionUtil.hasPermission("user:view"));
                addNavigationButton("学生管理", this::onStudentManagement, PermissionUtil.hasPermission("student:manage"));
                addNavigationButton("教师管理", this::onTeacherManagement, PermissionUtil.hasPermission("teacher:manage"));
                addNavigationButton("图书馆", this::onLibrary, PermissionUtil.hasPermission("library:view"));
                addNavigationButton("图书维护(管理员)", this::onLibraryAdmin, PermissionUtil.hasPermission("library:admin"));
                addNavigationButton("商店", this::onShop, PermissionUtil.hasPermission("shop:view"));
                addNavigationButton("商品管理", this::onShopManagement, PermissionUtil.hasPermission("shop:admin"));
                
                // 添加退出按钮
                Separator exitSeparator = new Separator();
                leftNavigation.getChildren().add(exitSeparator);
                
                Button exitButton = new Button("退出系统");
                exitButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-min-width: 150;");
                exitButton.setOnAction(e -> onExit());
                leftNavigation.getChildren().add(exitButton);
            }
        } catch (Exception e) {
            System.err.println("初始化动态导航菜单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 添加导航按钮
     */
    private void addNavigationButton(String text, Runnable action, boolean hasPermission) {
        if (hasPermission) {
            Button button = new Button(text);
            button.setStyle("-fx-min-width: 150;");
            button.setOnAction(e -> action.run());
            leftNavigation.getChildren().add(button);
        }
    }
    
    /**
     * 更新菜单可见性
     */
    private void updateMenuVisibility() {
        try {
            // 根据权限控制菜单项可见性
            if (btnLibraryAdmin != null) {
                boolean canManageLibrary = PermissionUtil.hasPermission("library:admin");
                btnLibraryAdmin.setVisible(canManageLibrary);
                btnLibraryAdmin.setManaged(canManageLibrary);
            }
            if (menuLibraryAdmin != null) {
                menuLibraryAdmin.setVisible(PermissionUtil.hasPermission("library:admin"));
            }
            
            if (btnShopManagement != null) {
                boolean canManageShop = PermissionUtil.hasPermission("shop:admin");
                btnShopManagement.setVisible(canManageShop);
                btnShopManagement.setManaged(canManageShop);
            }
            if (menuShopManagement != null) {
                menuShopManagement.setVisible(PermissionUtil.hasPermission("shop:admin"));
            }
        } catch (Exception e) {
            System.err.println("更新菜单可见性失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 角色切换事件处理
     */
    @FXML
    private void onRoleChanged() {
        try {
            String selectedRole = roleSwitcher.getValue();
            if (selectedRole != null) {
                String roleCode = getRoleCode(selectedRole);
                if (roleCode != null && RoleSwitcher.setCurrentActiveRole(roleCode)) {
                    showInfo("角色切换成功", "已切换到 " + selectedRole + " 角色");
                    // 重新初始化导航菜单
                    initializeDynamicNavigation();
                    updateMenuVisibility();
                } else {
                    showError("角色切换失败", "无法切换到 " + selectedRole + " 角色");
                }
            }
        } catch (Exception e) {
            showError("角色切换失败", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取角色代码
     */
    private String getRoleCode(String roleName) {
        switch (roleName) {
            case "学生": return "STUDENT";
            case "教师": return "TEACHER";
            case "管理员": return "ADMIN";
            default: return null;
        }
    }
    
    /**
     * 设置当前用户
     */
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("欢迎, " + username + "!");
    }
    
    /**
     * 教师管理菜单点击
     */
    @FXML
    private void onTeacherManagement() {
        if (!PermissionUtil.hasPermission("teacher:manage")) {
            showError("权限不足", "您没有管理教师的权限");
            return;
        }
        // TODO: 打开教师管理界面
        showInfo("功能开发中", "教师管理功能正在开发中");
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
    
    /** 商店入口（学生/教师/管理员均可访问） */
    @FXML
    private void onShop() {
        loadContent("shop-view.fxml");
        statusLabel.setText("商店");
    }
    
    /** 商品管理入口 */
    @FXML
    private void onShopManagement() {
        // 学生/教师进入“商店交易”界面；管理员进入“商品管理”界面
        var user = com.vCampus.common.SessionContext.getCurrentUser();
        boolean isAdmin = user != null && user.getRole() != null &&
                (user.getRole().toLowerCase().contains("admin") || user.getRole().contains("管理员"));
        if (isAdmin) {
            loadContent("shop-management-view.fxml");
            statusLabel.setText("商品管理");
        } else {
            loadContent("shop-view.fxml");
            statusLabel.setText("商店");
        }
    }
    
    /**
     * 获取当前舞台
     */
    private javafx.stage.Stage getCurrentStage() {
        return (javafx.stage.Stage) mainContainer.getScene().getWindow();
    }
    
    //---------------------------------------------------------------
    
  
}