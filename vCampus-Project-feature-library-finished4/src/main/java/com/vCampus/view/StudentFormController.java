package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import client.net.SocketClient;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class StudentFormController extends BaseController {

    @FXML private TextField tfStudentId;
    @FXML private TextField tfStudentName;
    @FXML private TextField tfClassName;
    @FXML private ComboBox<String> cbSex;
    @FXML private DatePicker dpEnrollDate;
    @FXML private TextField tfEmail;
    @FXML private TextField tfIdCard;
    @FXML private ComboBox<String> cbStatus;

    // 移除直接依赖服务，改为通过 SocketClient 通信
    private Student editing; // null 表示新增

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbSex.getItems().setAll("男", "女");
        cbStatus.getItems().setAll("正常", "休学", "退学", "毕业");
    }

    public void setEditing(Student s) {
        this.editing = s;
        if (s != null) {
            tfStudentId.setText(s.getStudentId());
            tfStudentId.setDisable(true);
            tfStudentName.setText(s.getStudentName());
            tfClassName.setText(s.getClassName());
            cbSex.setValue(s.getSex());
            if (s.getEnrollDate() != null) {
                dpEnrollDate.setValue(s.getEnrollDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            tfEmail.setText(s.getEmail());
            tfIdCard.setText(s.getIdCard());
            cbStatus.setValue(s.getStatus());
        }
    }

    @FXML
    private void onOk() {
        Student s = editing == null ? new Student() : editing;
        s.setStudentId(tfStudentId.getText());
        s.setStudentName(tfStudentName.getText());
        s.setClassName(tfClassName.getText());
        s.setSex(cbSex.getValue());
        s.setEmail(tfEmail.getText());
        s.setIdCard(tfIdCard.getText());
        s.setStatus(cbStatus.getValue());
        if (dpEnrollDate.getValue() != null) {
            s.setEnrollDate(Date.from(dpEnrollDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            s.setEnrollDate(null);
        }

        if (!validateStudent(s)) {
            showWarning("请检查必填项与长度");
            return;
        }

        try {
            boolean ok;
            if (editing == null) {
                // 新增：走注册总线，默认创建用户后再创建学生
                s.setUsername(s.getStudentId());
                s.setPassword("123456");
                s.setRole("STUDENT");
                ok = registerStudent(s);
            } else {
                ok = updateStudent(s);
            }
            if (ok) {
                showSuccess("保存成功");
                closeIfDialog();
            } else {
                showError("保存失败");
            }
        } catch (Exception e) {
            showError("保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        closeIfDialog();
    }

    private void closeIfDialog() {
        try {
            ((javafx.stage.Stage) tfStudentId.getScene().getWindow()).close();
        } catch (Exception ignored) {}
    }

    /**
     * 验证学生信息
     */
    private boolean validateStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            return false;
        }
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            return false;
        }
        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            return false;
        }
        if (student.getSex() == null || student.getSex().trim().isEmpty()) {
            return false;
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            return false;
        }
        if (student.getIdCard() == null || student.getIdCard().trim().isEmpty()) {
            return false;
        }
        if (student.getStatus() == null || student.getStatus().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 注册学生
     */
    private boolean registerStudent(Student student) {
        try {
            String request = "REGISTER:" + student.getUsername() + ":" + student.getPassword() + ":" + 
                           student.getRole() + ":" + student.getStudentId() + ":" + student.getStudentName() + 
                           ":" + student.getClassName() + ":" + student.getSex() + ":" + student.getEmail() + 
                           ":" + student.getIdCard() + ":" + (student.getStatus() != null ? student.getStatus() : "正常");
            String response = SocketClient.sendRequest(request);
            return response != null && response.startsWith("SUCCESS:REGISTER:");
        } catch (Exception e) {
            System.err.println("注册学生失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新学生信息
     */
    private boolean updateStudent(Student student) {
        try {
            String request = "UPDATE_STUDENT:" + student.getStudentId() + ":" + student.getStudentName() + 
                           ":" + student.getClassName() + ":" + student.getSex() + ":" + student.getEmail() + 
                           ":" + student.getIdCard() + ":" + (student.getStatus() != null ? student.getStatus() : "正常");
            String response = SocketClient.sendRequest(request);
            return response != null && response.startsWith("SUCCESS:UPDATE:");
        } catch (Exception e) {
            System.err.println("更新学生失败: " + e.getMessage());
            return false;
        }
    }
}
