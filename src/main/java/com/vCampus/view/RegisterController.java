package com.vCampus.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.vCampus.entity.User;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.Admin;
import com.vCampus.service.IUserService;
import com.vCampus.service.ServiceFactory;
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

    // 教师专属字段
    @FXML private VBox teacherFields;
    @FXML private TextField teacherIdField;
    @FXML private TextField teacherNameField;
    @FXML private ComboBox<String> teacherSexComboBox;
    @FXML private TextField technicalField;
    @FXML private TextField departmentIdField;

    // 管理员专属字段
    @FXML private VBox adminFields;
    @FXML private TextField adminIdField;
    @FXML private TextField adminNameField;

    @FXML
    private void initialize() {
        System.out.println("注册控制器初始化");

        // 在控制器中初始化选项
        roleComboBox.getItems().addAll("学生", "教师", "管理员");
        roleComboBox.setValue("学生");

        // 初始化教师性别选项
        teacherSexComboBox.getItems().addAll("男", "女");
        teacherSexComboBox.setValue("男");

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

        // 根据用户角色显示/隐藏相应字段
        studentFields.setVisible("学生".equals(role));
        studentFields.setManaged("学生".equals(role));
        
        teacherFields.setVisible("教师".equals(role));
        teacherFields.setManaged("教师".equals(role));
        
        adminFields.setVisible("管理员".equals(role));
        adminFields.setManaged("管理员".equals(role));
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
                Student student = new Student();
                student.setUsername(username);
                student.setPassword(password);
                student.setRole(roleEnglish);
                student.setStudentName(studentNameField.getText().trim());
                student.setClassName(classNameField.getText().trim());
                student.setStudentId(studentIdField.getText().trim());

                System.out.println("注册学生信息: " + student.toString());
                registerUser(student, "学生");

            } else if ("教师".equals(role)) {
                Teacher teacher = new Teacher();
                teacher.setUsername(username);
                teacher.setPassword(password);
                teacher.setRole(roleEnglish);
                teacher.setTeacherId(teacherIdField.getText().trim());
                teacher.setTeacherName(teacherNameField.getText().trim());
                teacher.setSex(teacherSexComboBox.getValue());
                teacher.setTechnical(technicalField.getText().trim());
                teacher.setDepartmentId(departmentIdField.getText().trim());

                System.out.println("注册教师信息: " + teacher.toString());
                registerUser(teacher, "教师");

            } else if ("管理员".equals(role)) {
                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setPassword(password);
                admin.setRole(roleEnglish);
                admin.setAdminId(adminIdField.getText().trim());
                admin.setAdminName(adminNameField.getText().trim());

                System.out.println("注册管理员信息: " + admin.toString());
                registerUser(admin, "管理员");
            }

        } catch (Exception e) {
            System.err.println("注册过程中出错: " + e.getMessage());
            e.printStackTrace();
            showError("注册过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 注册用户（在后台线程执行）
     */
    private void registerUser(User user, String userType) {
        new Thread(() -> {
        	IUserService userService = ServiceFactory.getUserService();
            IUserService.RegisterResult result = userService.register(user);
            TransactionManager.runLaterSafe(() -> {
                handleRegisterResult(result, userType);
            });
        }).start();
    }

    /**
     * 处理注册结果
     */
    private void handleRegisterResult(IUserService.RegisterResult result, String userType) {
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
            case TEACHER_ID_EXISTS:
                showError("注册失败，教师编号已存在");
                teacherIdField.requestFocus();
                break;
            case ADMIN_ID_EXISTS:
                showError("注册失败，管理员工号已存在");
                adminIdField.requestFocus();
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

        String role = roleComboBox.getValue();
        
        // 根据角色验证专属字段
        if ("学生".equals(role)) {
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

        } else if ("教师".equals(role)) {
            if (teacherIdField.getText().trim().isEmpty()) {
                showError("请输入教师编号");
                return false;
            }
            if (teacherNameField.getText().trim().isEmpty()) {
                showError("请输入教师姓名");
                return false;
            }
            if (technicalField.getText().trim().isEmpty()) {
                showError("请输入职称");
                return false;
            }
            if (departmentIdField.getText().trim().isEmpty()) {
                showError("请输入部门ID");
                return false;
            }

        } else if ("管理员".equals(role)) {
            if (adminIdField.getText().trim().isEmpty()) {
                showError("请输入管理员工号");
                return false;
            }
            if (adminNameField.getText().trim().isEmpty()) {
                showError("请输入管理员姓名");
                return false;
            }
        }

        return true;
    }

    @FXML
    private void onCancel() {
        System.out.println("取消注册");
        closeWindow();
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