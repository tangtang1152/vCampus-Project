package com.vCampus.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.vCampus.common.NavigationUtil;
import com.vCampus.entity.User;
import com.vCampus.entity.Student;
import com.vCampus.service.UserService;
import com.vCampus.util.TransactionManager;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    
    // 学生专属字段
    @FXML private VBox studentFields;
    @FXML private TextField studentNameField;
    @FXML private TextField classNameField;
    @FXML private TextField studentIdField;

    @FXML
    private void initialize() {
        System.out.println("注册控制器初始化");
        
        // 在控制器中初始化选项
        roleComboBox.getItems().addAll("学生", "教师", "管理员");
        roleComboBox.setValue("学生");
        
        // 添加监听器，当选项变化时调用
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            onRoleChanged();
        });
        
        // 默认显示学生字段
        onRoleChanged();
    }

    @FXML
    private void onRoleChanged() {
        if (roleComboBox.getValue() == null) return;
        
        String role = roleComboBox.getValue();
        System.out.println("用户角色改变为: " + role);
        
        // 根据用户角色显示/隐藏相应字段 - 使用中文匹配
        if ("学生".equals(role)) {
            System.out.println("显示学生字段");
            studentFields.setVisible(true);
            studentFields.setManaged(true); // 重要：确保布局管理器也处理这个容器
        } else {
            System.out.println("隐藏学生字段");
            studentFields.setVisible(false);
            studentFields.setManaged(false);
        }
    }

    @FXML
    private void onRegister() {
        try {
            System.out.println("注册按钮被点击");
            
            // 清除之前的错误信息
            errorLabel.setVisible(false);
            
            // 验证输入
            if (!validateInput()) {
                return;
            }
            
            // 获取基本用户信息
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String role = roleComboBox.getValue();
            
            // 将中文角色转换为英文（用于数据库存储）
            String roleEnglish = convertRoleToEnglish(role);
            
            // 根据角色创建相应的对象
            if ("学生".equals(role)) {
                // 创建Student对象
                Student student = new Student();
                student.setUsername(username);
                student.setPassword(password);
                student.setRole(roleEnglish); // 使用英文角色
                student.setStudentName(studentNameField.getText().trim());
                student.setClassName(classNameField.getText().trim());
                student.setStudentId(studentIdField.getText().trim());

                System.out.println("注册学生信息: " + student.toString());
                
                // 在后台线程执行注册操作，避免阻塞UI
                new Thread(() -> {
                    UserService.RegisterResult result = UserService.register(student);
                    
                    TransactionManager.runLaterSafe(() -> {
                        handleRegisterResult(result, "学生");
                    });
                }).start();
                
            } else {
                // 创建普通User对象（教师或管理员）
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(roleEnglish);
                
                System.out.println("注册用户信息: " + username + ", 角色: " + roleEnglish);
                
                // 在后台线程执行注册操作
                new Thread(() -> {
                    UserService.RegisterResult result = UserService.register(user);
                    
                    TransactionManager.runLaterSafe(() -> {
                        handleRegisterResult(result, "用户");
                    });
                }).start();
            }
            
        } catch (Exception e) {
            System.err.println("注册过程中出错: " + e.getMessage());
            e.printStackTrace();
            showError("注册过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理注册结果
     */
    private void handleRegisterResult(UserService.RegisterResult result, String userType) {
        System.out.println("处理注册结果: " + result + ", 用户类型: " + userType);
        
        switch (result) {
            case SUCCESS:
                showSuccess(userType + "注册成功！");
                closeWindow();
                break;
            case USERNAME_EXISTS:
                showError("注册失败，用户名已存在");
                usernameField.requestFocus();
                break;
            case STUDENT_ID_EXISTS:
                showError("注册失败，学号已存在");
                studentIdField.requestFocus();
                break;
            case VALIDATION_FAILED:
                showError("注册失败，数据验证失败，请检查输入");
                break;
            case DATABASE_ERROR:
                showError("注册失败，数据库错误，请检查数据格式或联系管理员");
                break;
            default:
                showError("注册失败，未知错误");
        }
    }
    
    
    @FXML
    private void onCancel() {
        System.out.println("取消注册");
        closeWindow();
    }

    private boolean validateInput() {
        // 检查必填字段
        if (usernameField.getText().trim().isEmpty()) {
            showError("请输入用户名");
            return false;
        }
        
        if (passwordField.getText().isEmpty()) {
            showError("请输入密码");
            return false;
        }
        
        if (confirmPasswordField.getText().isEmpty()) {
            showError("请确认密码");
            return false;
        }
        
        // 检查密码是否匹配
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("两次输入的密码不一致");
            return false;
        }
        
        // 检查密码长度
        if (passwordField.getText().length() < 6) {
            showError("密码长度至少6位");
            return false;
        }
        
        // 如果是学生，验证学生专属字段
        if ("学生".equals(roleComboBox.getValue())) {
            if (studentNameField.getText().trim().isEmpty()) {
                showError("请输入学生姓名");
                return false;
            }
            if (classNameField.getText().trim().isEmpty()) {
                showError("请输入班级");
                return false;
            }
            if (studentIdField.getText().trim().isEmpty()) {
                showError("请输入学号");
                return false;
            }
            
            // 验证学号是否为数字
            try {
                Integer.parseInt(studentIdField.getText().trim());
            } catch (NumberFormatException e) {
                showError("学号必须是数字");
                return false;
            }
        }
        
        return true;
    }

    /**
     * 将中文角色转换为英文角色
     */
    private String convertRoleToEnglish(String chineseRole) {
        switch (chineseRole) {
            case "学生": return "STUDENT";
            case "教师": return "TEACHER";
            case "管理员": return "ADMIN";
            default: return "STUDENT";
        }
    }

    private void showError(String message) {
        try {
            System.out.println("显示错误信息: " + message);
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } catch (Exception e) {
            System.err.println("显示错误信息时发生异常: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        try {
            System.out.println("显示成功信息: " + message);
            javafx.application.Platform.runLater(() -> {
                try {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("注册成功");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    
                    // 设置对话框所有者，避免Timer cancelled错误
                    Stage stage = getStage();
                    if (stage != null && stage.isShowing()) {
                        alert.initOwner(stage);
                    }
                    
                    alert.showAndWait();
                    closeWindow();
                } catch (IllegalStateException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Timer already cancelled")) {
                        System.err.println("忽略Timer already cancelled错误，对话框可能已关闭");
                        closeWindow();
                    } else {
                        System.err.println("显示成功对话框时发生异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    System.err.println("显示成功对话框时发生未知异常: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("准备显示成功信息时发生异常: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private Stage getStage() {
        return (Stage) usernameField.getScene().getWindow();
    }
}