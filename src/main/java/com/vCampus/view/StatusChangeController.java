package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * 状态变更控制器
 */
public class StatusChangeController extends BaseController {
    
    @FXML private Label studentIdLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<String> newStatusComboBox;
    @FXML private TextArea reasonTextArea;
    
    private IStudentService studentService = ServiceFactory.getStudentService();
    private Student currentStudent;
    
    @FXML
    public void initialize() {
        // 初始化状态选项
        newStatusComboBox.getItems().addAll("正常", "休学", "退学", "毕业");
    }
    
    /**
     * 设置当前学生
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        if (student != null) {
            studentIdLabel.setText(student.getStudentId());
            studentNameLabel.setText(student.getStudentName());
            currentStatusLabel.setText(student.getStatus());
            newStatusComboBox.setValue(student.getStatus());
        }
    }
    
    /**
     * 提交变更按钮点击事件
     */
    @FXML
    private void onSubmit() {
        if (currentStudent == null) {
            showError("未选择学生");
            return;
        }
        
        String newStatus = newStatusComboBox.getValue();
        String reason = reasonTextArea.getText();
        
        if (newStatus.equals(currentStudent.getStatus())) {
            showError("新状态与当前状态相同");
            return;
        }
        
        if (reason.isEmpty()) {
            showError("请填写变更原因");
            reasonTextArea.requestFocus();
            return;
        }
        
        try {
            // 使用IStudentService中的updateStudentStatus方法
            boolean success = studentService.updateStudentStatus(
                currentStudent.getStudentId(), newStatus);
            
            if (success) {
                showSuccess("状态变更成功");
                closeWindow();
            } else {
                showError("状态变更失败");
            }
        } catch (Exception e) {
            showError("状态变更失败: " + e.getMessage());
            e.printStackTrace();
        }
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
        studentIdLabel.getScene().getWindow().hide();
    }
}