package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;

/**
 * 学生表单控制器
 */
public class StudentFormController extends BaseController {
    
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private TextField classNameField;
    @FXML private ComboBox<String> sexComboBox;
    @FXML private TextField emailField;
    @FXML private TextField idCardField;
    @FXML private DatePicker enrollDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    
    private IStudentService studentService = ServiceFactory.getStudentService();
    private Student currentStudent;
    
    @FXML
    public void initialize() {
        // 初始化下拉框
        sexComboBox.getItems().addAll("男", "女");
        statusComboBox.getItems().addAll("正常", "休学", "退学", "毕业");
        
        // 设置默认值
        enrollDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue("正常");
    }
    
    /**
     * 设置当前学生（编辑模式）
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        if (student != null) {
            studentIdField.setText(student.getStudentId());
            studentNameField.setText(student.getStudentName());
            classNameField.setText(student.getClassName());
            sexComboBox.setValue(student.getSex());
            emailField.setText(student.getEmail());
            idCardField.setText(student.getIdCard());
            if (student.getEnrollDate() != null) {
                enrollDatePicker.setStyle(student.getEnrollDate().toLocaleString());
            }
            statusComboBox.setValue(student.getStatus());
            
            // 编辑模式下学号不可编辑
            studentIdField.setDisable(true);
        }
    }
    
    /**
     * 保存按钮点击事件
     */
    @FXML
    private void onSave() {
        // 表单验证
        if (!validateForm()) {
            return;
        }
        
        try {
            Student student = new Student();
            student.setStudentId(studentIdField.getText());
            student.setStudentName(studentNameField.getText());
            student.setClassName(classNameField.getText());
            student.setSex(sexComboBox.getValue());
            student.setEmail(emailField.getText());
            student.setIdCard(idCardField.getText());
            student.setEnrollDate(java.sql.Date.valueOf(enrollDatePicker.getValue()));
            student.setStatus(statusComboBox.getValue());
            
            boolean success;
            if (currentStudent == null) {
                // 添加新学生 - 需要设置用户信息
                student.setUsername(studentIdField.getText()); // 默认用户名设为学号
                student.setPassword("123456"); // 默认密码
                student.setRole("student"); // 默认角色
                
                // 使用IBaseService中的add方法，同时添加tbl_user和tbl_student记录
                success = studentService.add(student);
            } else {
                // 更新现有学生 - 使用IStudentService中的updateStudentOnly方法，只更新tbl_student表
                success = studentService.updateStudentOnly(student);
            }
            
            if (success) {
                showSuccess(currentStudent == null ? "添加成功" : "更新成功");
                closeWindow();
            } else {
                showError("操作失败");
            }
        } catch (Exception e) {
            showError("操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 表单验证
     */
    private boolean validateForm() {
        if (studentIdField.getText().isEmpty()) {
            showError("学号不能为空");
            studentIdField.requestFocus();
            return false;
        }
        
        if (studentNameField.getText().isEmpty()) {
            showError("姓名不能为空");
            studentNameField.requestFocus();
            return false;
        }
        
        if (classNameField.getText().isEmpty()) {
            showError("班级不能为空");
            classNameField.requestFocus();
            return false;
        }
        
        // 检查学号是否已存在（仅添加模式）
        if (currentStudent == null) {
            try {
                boolean exists = studentService.exists(studentIdField.getText());
                if (exists) {
                    showError("学号已存在");
                    studentIdField.requestFocus();
                    return false;
                }
            } catch (Exception e) {
                showError("检查学号失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 取消按钮点击事件
     */
    @FXML
    private void onCancel() {
        closeWindow();
    }
    
    /**
     * 关闭窗口
     */
    private void closeWindow() {
        studentIdField.getScene().getWindow().hide();
    }
}