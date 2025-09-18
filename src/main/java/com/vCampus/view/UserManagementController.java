package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.RoleSwitcher;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.*;
import com.vCampus.service.IUserManagementService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.util.PermissionUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 提供统一的用户管理界面
 */
public class UserManagementController extends BaseController implements Initializable {
    
    @FXML private ComboBox<String> roleSwitcher;
    @FXML private Label currentUserLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> classFilter;
    
    @FXML private TableView<UserWrapper> userTable;
    @FXML private TableColumn<UserWrapper, Boolean> selectColumn;
    @FXML private TableColumn<UserWrapper, Integer> userIdColumn;
    @FXML private TableColumn<UserWrapper, String> usernameColumn;
    @FXML private TableColumn<UserWrapper, String> nameColumn;
    @FXML private TableColumn<UserWrapper, String> rolesColumn;
    @FXML private TableColumn<UserWrapper, String> departmentColumn;
    @FXML private TableColumn<UserWrapper, String> statusColumn;
    @FXML private TableColumn<UserWrapper, String> actionsColumn;
    
    @FXML private Button prevPageBtn;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageBtn;
    @FXML private ComboBox<String> pageSizeCombo;
    
    @FXML private Label totalUsersLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label teacherCountLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label selectedCountLabel;
    
    private IUserManagementService userManagementService;
    private ObservableList<UserWrapper> userList;
    private List<User> allUsers;
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userManagementService = ServiceFactory.getUserManagementService();
        userList = FXCollections.observableArrayList();
        
        initializeComponents();
        loadUserData();
        updateStatistics();
        updateRoleSwitcher();
    }
    
    private void initializeComponents() {
        // 初始化表格
        setupTableColumns();
        
        // 初始化分页
        pageSizeCombo.setValue("20");
        pageSizeCombo.setOnAction(e -> {
            pageSize = Integer.parseInt(pageSizeCombo.getValue());
            currentPage = 1;
            loadUserData();
        });
        
        // 初始化角色切换器
        roleSwitcher.setOnAction(e -> onRoleChanged());
        
        // 初始化筛选器
        roleFilter.setOnAction(e -> onFilterChanged());
        departmentFilter.setOnAction(e -> onFilterChanged());
        classFilter.setOnAction(e -> onFilterChanged());
    }
    
    private void setupTableColumns() {
        // 设置表格列
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        rolesColumn.setCellValueFactory(new PropertyValueFactory<>("rolesDisplay"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("departmentClass"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // 操作列
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));
        actionsColumn.setCellFactory(new Callback<TableColumn<UserWrapper, String>, TableCell<UserWrapper, String>>() {
            @Override
            public TableCell<UserWrapper, String> call(TableColumn<UserWrapper, String> param) {
                return new TableCell<UserWrapper, String>() {
                    private final Button editBtn = new Button("编辑");
                    private final Button deleteBtn = new Button("删除");
                    private final Button rolesBtn = new Button("角色");
                    
                    {
                        editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-min-width: 60;");
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-min-width: 60;");
                        rolesBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-min-width: 60;");
                        
                        editBtn.setOnAction(e -> onEditUser(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(e -> onDeleteUser(getTableView().getItems().get(getIndex())));
                        rolesBtn.setOnAction(e -> onManageRoles(getTableView().getItems().get(getIndex())));
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(5);
                            hbox.getChildren().addAll(editBtn, deleteBtn, rolesBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
        
        userTable.setItems(userList);
    }
    
    private void loadUserData() {
        try {
            // 获取筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (roleFilter.getValue() != null && !roleFilter.getValue().equals("所有角色")) {
                String roleCode = getRoleCode(roleFilter.getValue());
                if (roleCode != null) {
                    filters.put("role", roleCode);
                }
            }
            if (departmentFilter.getValue() != null && !departmentFilter.getValue().equals("所有部门")) {
                filters.put("department", departmentFilter.getValue());
            }
            if (classFilter.getValue() != null && !classFilter.getValue().equals("所有班级")) {
                filters.put("class", classFilter.getValue());
            }
            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                filters.put("keyword", searchField.getText().trim());
            }
            
            // 获取用户数据
            if (filters.isEmpty()) {
                allUsers = userManagementService.getAllUsers();
            } else {
                allUsers = userManagementService.getUsersWithFilters(filters);
            }
            
            // 分页处理
            totalPages = (int) Math.ceil((double) allUsers.size() / pageSize);
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allUsers.size());
            
            List<User> pageUsers = allUsers.subList(startIndex, endIndex);
            
            // 转换为UserWrapper
            userList.clear();
            for (User user : pageUsers) {
                userList.add(new UserWrapper(user));
            }
            
            // 更新分页控件
            updatePaginationControls();
            updateSelectedCount();
            
        } catch (Exception e) {
            showError("加载用户数据失败", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updatePaginationControls() {
        pageInfoLabel.setText(String.format("第 %d 页，共 %d 页", currentPage, totalPages));
        prevPageBtn.setDisable(currentPage <= 1);
        nextPageBtn.setDisable(currentPage >= totalPages);
    }
    
    private void updateStatistics() {
        try {
            Map<String, Integer> stats = userManagementService.getUserStatistics();
            totalUsersLabel.setText("总用户数: " + stats.getOrDefault("total", 0));
            studentCountLabel.setText("学生: " + stats.getOrDefault("students", 0));
            teacherCountLabel.setText("教师: " + stats.getOrDefault("teachers", 0));
            adminCountLabel.setText("管理员: " + stats.getOrDefault("admins", 0));
        } catch (Exception e) {
            System.err.println("更新统计信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateRoleSwitcher() {
        try {
            List<String> availableRoles = RoleSwitcher.getAvailableRoles();
            roleSwitcher.getItems().clear();
            roleSwitcher.getItems().addAll(availableRoles);
            
            String currentRole = RoleSwitcher.getCurrentActiveRole();
            if (currentRole != null) {
                roleSwitcher.setValue(RoleSwitcher.getRoleDisplayName(currentRole));
            }
            
            User currentUser = SessionContext.getCurrentUser();
            if (currentUser != null) {
                currentUserLabel.setText("当前用户: " + currentUser.getUsername());
            }
        } catch (Exception e) {
            System.err.println("更新角色切换器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateSelectedCount() {
        long selectedCount = userList.stream().filter(UserWrapper::isSelected).count();
        selectedCountLabel.setText("已选择: " + selectedCount + " 个用户");
    }
    
    private String getRoleCode(String roleName) {
        switch (roleName) {
            case "学生": return "STUDENT";
            case "教师": return "TEACHER";
            case "管理员": return "ADMIN";
            default: return null;
        }
    }
    
    // 事件处理方法
    @FXML
    private void onRoleChanged() {
        String selectedRole = roleSwitcher.getValue();
        if (selectedRole != null) {
            String roleCode = getRoleCode(selectedRole);
            if (roleCode != null && RoleSwitcher.setCurrentActiveRole(roleCode)) {
                showInfo("角色切换成功", "已切换到 " + selectedRole + " 角色");
                // 重新加载数据以反映权限变化
                loadUserData();
            } else {
                showError("角色切换失败", "无法切换到 " + selectedRole + " 角色");
            }
        }
    }
    
    @FXML
    private void onSearch() {
        currentPage = 1;
        loadUserData();
    }
    
    @FXML
    private void onFilterChanged() {
        currentPage = 1;
        loadUserData();
    }
    
    @FXML
    private void onClearFilters() {
        searchField.clear();
        roleFilter.setValue("所有角色");
        departmentFilter.setValue("所有部门");
        classFilter.setValue("所有班级");
        currentPage = 1;
        loadUserData();
    }
    
    @FXML
    private void onRefresh() {
        loadUserData();
        updateStatistics();
        updateRoleSwitcher();
    }
    
    @FXML
    private void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUserData();
        }
    }
    
    @FXML
    private void onNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadUserData();
        }
    }
    
    @FXML
    private void onAddUser() {
        if (!PermissionUtil.hasPermission("user:create")) {
            showError("权限不足", "您没有创建用户的权限");
            return;
        }
        
        // TODO: 打开添加用户对话框
        showInfo("功能开发中", "添加用户功能正在开发中");
    }
    
    @FXML
    private void onBatchImport() {
        if (!PermissionUtil.hasPermission("user:create")) {
            showError("权限不足", "您没有导入用户的权限");
            return;
        }
        
        // TODO: 打开批量导入对话框
        showInfo("功能开发中", "批量导入功能正在开发中");
    }
    
    @FXML
    private void onExportData() {
        if (!PermissionUtil.hasPermission("user:view")) {
            showError("权限不足", "您没有导出数据的权限");
            return;
        }
        
        // TODO: 打开导出对话框
        showInfo("功能开发中", "导出功能正在开发中");
    }
    
    @FXML
    private void onBatchAssignRoles() {
        List<UserWrapper> selectedUsers = userList.stream()
                .filter(UserWrapper::isSelected)
                .collect(Collectors.toList());
        
        if (selectedUsers.isEmpty()) {
            showWarning("请选择用户", "请先选择要分配角色的用户");
            return;
        }
        
        if (!PermissionUtil.hasPermission("user:manage_roles")) {
            showError("权限不足", "您没有管理用户角色的权限");
            return;
        }
        
        // TODO: 打开批量分配角色对话框
        showInfo("功能开发中", "批量分配角色功能正在开发中");
    }
    
    @FXML
    private void onBatchDelete() {
        List<UserWrapper> selectedUsers = userList.stream()
                .filter(UserWrapper::isSelected)
                .collect(Collectors.toList());
        
        if (selectedUsers.isEmpty()) {
            showWarning("请选择用户", "请先选择要删除的用户");
            return;
        }
        
        if (!PermissionUtil.hasPermission("user:delete")) {
            showError("权限不足", "您没有删除用户的权限");
            return;
        }
        
        // 确认删除
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除选中的用户？");
        alert.setContentText("此操作不可撤销，确定要继续吗？");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<Integer> userIds = selectedUsers.stream()
                    .map(UserWrapper::getUserId)
                    .collect(Collectors.toList());
            
            if (userManagementService.batchDeleteUsers(userIds)) {
                showInfo("删除成功", "已成功删除 " + selectedUsers.size() + " 个用户");
                loadUserData();
                updateStatistics();
            } else {
                showError("删除失败", "删除用户时发生错误");
            }
        }
    }
    
    private void onEditUser(UserWrapper userWrapper) {
        if (!PermissionUtil.hasPermission("user:update")) {
            showError("权限不足", "您没有编辑用户的权限");
            return;
        }
        
        // TODO: 打开编辑用户对话框
        showInfo("功能开发中", "编辑用户功能正在开发中");
    }
    
    private void onDeleteUser(UserWrapper userWrapper) {
        if (!PermissionUtil.hasPermission("user:delete")) {
            showError("权限不足", "您没有删除用户的权限");
            return;
        }
        
        // 确认删除
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除用户 " + userWrapper.getDisplayName() + "？");
        alert.setContentText("此操作不可撤销，确定要继续吗？");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userManagementService.deleteUser(userWrapper.getUserId())) {
                showInfo("删除成功", "用户 " + userWrapper.getDisplayName() + " 已删除");
                loadUserData();
                updateStatistics();
            } else {
                showError("删除失败", "删除用户时发生错误");
            }
        }
    }
    
    private void onManageRoles(UserWrapper userWrapper) {
        if (!PermissionUtil.hasPermission("user:manage_roles")) {
            showError("权限不足", "您没有管理用户角色的权限");
            return;
        }
        
        // TODO: 打开角色管理对话框
        showInfo("功能开发中", "角色管理功能正在开发中");
    }
    
    /**
     * 用户包装类，用于表格显示
     */
    public static class UserWrapper {
        private final User user;
        private boolean selected = false;
        
        public UserWrapper(User user) {
            this.user = user;
        }
        
        public User getUser() {
            return user;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        public Integer getUserId() {
            return user.getUserId();
        }
        
        public String getUsername() {
            return user.getUsername();
        }
        
        public String getDisplayName() {
            if (user instanceof Student) {
                return ((Student) user).getStudentName();
            } else if (user instanceof Teacher) {
                return ((Teacher) user).getTeacherName();
            } else if (user instanceof Admin) {
                return ((Admin) user).getAdminName();
            }
            return user.getUsername();
        }
        
        public String getRolesDisplay() {
            // TODO: 获取用户角色并格式化显示
            return user.getRole();
        }
        
        public String getDepartmentClass() {
            if (user instanceof Student) {
                return ((Student) user).getClassName();
            } else if (user instanceof Teacher) {
                return ((Teacher) user).getDepartmentId();
            }
            return "";
        }
        
        public String getStatus() {
            // TODO: 获取用户状态
            return "正常";
        }
        
        public String getActions() {
            return "操作";
        }
    }
}
