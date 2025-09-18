package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentFormController extends BaseController {
    @FXML private TextField studentIdField;
    @FXML private TextField nameField;
    @FXML private TextField classField;
    @FXML private ComboBox<String> sexBox;
    @FXML private TextField emailField;
    @FXML private TextField idCardField;
    @FXML private DatePicker enrollDatePicker;
    @FXML private ComboBox<String> statusBox;
    @FXML private Button saveBtn;

    private final IStudentService studentService = ServiceFactory.getStudentService();

    @FXML
    public void initialize() {
        if (sexBox != null) {
            sexBox.getItems().setAll("男", "女");
            if (sexBox.getValue() == null && !sexBox.getItems().isEmpty()) sexBox.setValue("男");
        }
        if (statusBox != null) {
            statusBox.getItems().setAll("正常", "休学", "退学", "毕业");
            if (statusBox.getValue() == null && !statusBox.getItems().isEmpty()) statusBox.setValue("正常");
        }
    }

    @FXML
    private void onSave() {
        String sid = trim(studentIdField);
        String name = trim(nameField);
        String clazz = trim(classField);
        String sex = sexBox == null ? null : sexBox.getValue();
        String email = trim(emailField);
        String idCard = trim(idCardField);
        java.util.Date enrollDate = null;
        try {
            if (enrollDatePicker != null && enrollDatePicker.getValue() != null) {
                enrollDate = java.sql.Date.valueOf(enrollDatePicker.getValue());
            }
        } catch (Exception ignored) {}
        String status = statusBox == null ? null : statusBox.getValue();

        if (isBlank(sid) || isBlank(name) || isBlank(clazz)) { showWarning("学号/姓名/班级为必填"); return; }

        Student s = new Student();
        s.setStudentId(sid);
        s.setStudentName(name);
        s.setClassName(clazz);
        s.setSex(sex);
        s.setEmail(email);
        s.setIdCard(idCard);
        if (enrollDate != null) s.setEnrollDate(new java.sql.Date(enrollDate.getTime()));
        s.setStatus(status == null ? "正常" : status);

        try {
            boolean ok;
            // 先判断是否存在
            ok = studentService.exists(sid) ? studentService.update(s) : studentService.add(s);
            if (ok) { showInformation("保存成功", "学生：" + name); closeWindow(); }
            else { showError("保存失败"); }
        } catch (Exception e) {
            showError("保存失败: " + e.getMessage());
        }
    }

    private String trim(TextField tf) { return tf==null? null : tf.getText()==null? null : tf.getText().trim(); }
    private boolean isBlank(String s) { return s==null || s.isEmpty(); }
    private void closeWindow() {
        if (saveBtn != null && saveBtn.getScene() != null) {
            saveBtn.getScene().getWindow().hide();
        }
    }
}