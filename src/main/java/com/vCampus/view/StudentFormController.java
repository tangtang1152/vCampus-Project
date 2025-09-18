package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IUserService;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.service.ValidationService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
    private IUserService userService = ServiceFactory.getUserService();
    private Student currentStudent; // 用于编辑模式下的当前学生对象

    @FXML
    public void initialize() {
        sexComboBox.getItems().addAll("男", "女");
        statusComboBox.getItems().addAll("正常", "休学", "退学", "毕业");

        enrollDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue("正常");
        sexComboBox.setValue("男");
    }

    /**
     * 设置当前学生（编辑模式）
     */
    public void setStudent(Student student) {
        this.currentStudent = student; // 关键：将传入的学生对象保存为 currentStudent
        if (student != null) {
            studentIdField.setText(student.getStudentId());
            studentNameField.setText(student.getStudentName());
            classNameField.setText(student.getClassName());
            sexComboBox.setValue(student.getSex());
            emailField.setText(student.getEmail());
            idCardField.setText(student.getIdCard());
            if (student.getEnrollDate() != null) {
                enrollDatePicker.setValue(student.getEnrollDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            statusComboBox.setValue(student.getStatus());

            studentIdField.setDisable(true); // 编辑模式下学号不可编辑
        }
    }

    /**
     * 保存按钮点击事件
     */
    @FXML
    private void onSave() {
        if (!validateForm()) {
            return;
        }

        try {
            boolean success = false;
            if (currentStudent == null) {
                // --- 添加新学生模式 ---
                Student newStudent = new Student(); // 创建新学生对象
                newStudent.setStudentId(studentIdField.getText().trim());
                newStudent.setStudentName(studentNameField.getText().trim());
                newStudent.setClassName(classNameField.getText().trim());
                newStudent.setSex(sexComboBox.getValue());
                newStudent.setEmail(emailField.getText().trim());
                newStudent.setIdCard(idCardField.getText().trim());
                LocalDate localDate = enrollDatePicker.getValue();
                if (localDate != null) {
  newStudent.setEnrollDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                newStudent.setStatus(statusComboBox.getValue());

                // 为新学生设置默认的用户信息
                newStudent.setUsername(studentIdField.getText().trim()); // 默认用户名设为学号
                newStudent.setPassword("123456"); // 默认密码，生产环境应更安全
                newStudent.setRole("STUDENT"); // 默认角色

                IUserService.RegisterResult result = userService.register(newStudent);
                success = (result == IUserService.RegisterResult.SUCCESS);

                if (!success) {
  showError("添加学生失败: " + result.getMessage());
  return;
                }
            } else {
                // --- 更新现有学生模式 ---
                // 直接更新 currentStudent 对象的属性
                currentStudent.setStudentName(studentNameField.getText().trim());
                currentStudent.setClassName(classNameField.getText().trim());
                currentStudent.setSex(sexComboBox.getValue());
                currentStudent.setEmail(emailField.getText().trim());
                currentStudent.setIdCard(idCardField.getText().trim());
                LocalDate localDate = enrollDatePicker.getValue();
                if (localDate != null) {
  currentStudent.setEnrollDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                currentStudent.setStatus(statusComboBox.getValue());

                // 调用 studentService 的 update 方法来更新数据库
                // 这个 update 方法会调用 StudentServiceImpl 的 doUpdate，它已修改为同时更新 tbl_student 和 tbl_user
                success = studentService.update(currentStudent); // 将修改后的 currentStudent 传给服务层
            }

            if (success) {
                showSuccess(currentStudent == null ? "添加成功" : "更新成功");
                closeWindow();
            } else {
                showError("操作失败，请检查控制台日志。");
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
        // 创建一个临时的学生对象用于验证，避免直接修改 currentStudent
        Student studentToValidate = new Student();
        // 如果是编辑模式，拷贝 currentStudent 的信息
        if (currentStudent != null) {
            studentToValidate.setUserId(currentStudent.getUserId());
            studentToValidate.setUsername(currentStudent.getUsername());
            studentToValidate.setPassword(currentStudent.getPassword());
            studentToValidate.setRole(currentStudent.getRole());
            studentToValidate.setStudentId(currentStudent.getStudentId()); // 学号不可编辑，所以直接用现有
        } else {
            // 新增模式，学号从输入框获取
            studentToValidate.setStudentId(studentIdField.getText().trim());
        }

        studentToValidate.setStudentName(studentNameField.getText().trim());
        studentToValidate.setClassName(classNameField.getText().trim());
        studentToValidate.setSex(sexComboBox.getValue());
        studentToValidate.setEmail(emailField.getText().trim());
        studentToValidate.setIdCard(idCardField.getText().trim());
        LocalDate localDate = enrollDatePicker.getValue();
        if (localDate != null) {
            studentToValidate.setEnrollDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        studentToValidate.setStatus(statusComboBox.getValue());


        if (!ValidationService.validateStudent(studentToValidate)) {
            showError("数据验证失败，请检查输入！");
            return false;
        }

        // 检查学号是否已存在（仅添加模式）
        if (currentStudent == null) {
            try {
                boolean exists = studentService.exists(studentIdField.getText().trim());
                if (exists) {
  showError("学号已存在！");
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