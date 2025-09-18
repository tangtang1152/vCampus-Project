package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.text.SimpleDateFormat; // 导入 SimpleDateFormat
// import java.time.format.DateTimeFormatter; // 不再需要此导入，因为使用 SimpleDateFormat

/**
 * 学生详情控制器
 */
public class StudentDetailController extends BaseController {

    @FXML private Label studentIdLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label classNameLabel;
    @FXML private Label sexLabel;
    @FXML private Label emailLabel;
    @FXML private Label idCardLabel;
    @FXML private Label enrollDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label orderCountLabel;
    @FXML private Label totalSpendingLabel;

    private IStudentService studentService = ServiceFactory.getStudentService();
    private Student student;

    /**
     * 设置学生信息
     */
    public void setStudent(Student student) {
        this.student = student;
        if (student != null) {
            // 显示基本信息
            studentIdLabel.setText(student.getStudentId());
            studentNameLabel.setText(student.getStudentName());
            classNameLabel.setText(student.getClassName());
            sexLabel.setText(student.getSex());
            emailLabel.setText(student.getEmail());
            idCardLabel.setText(student.getIdCard());

            // 修复：使用 SimpleDateFormat 格式化日期
            if (student.getEnrollDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                enrollDateLabel.setText(sdf.format(student.getEnrollDate()));
            } else {
                enrollDateLabel.setText("N/A"); // 如果日期为空，显示N/A
            }

            statusLabel.setText(student.getStatus());

            // 显示用户信息（从父类User继承）
            usernameLabel.setText(student.getUsername());
            roleLabel.setText(student.getRole());

            try {
                // 显示商店模块信息
                int orderCount = studentService.getStudentOrderCount(student.getStudentId());
                double totalSpending = studentService.getStudentTotalSpending(student.getStudentId());

                orderCountLabel.setText(String.valueOf(orderCount));
                totalSpendingLabel.setText(String.format("¥%.2f", totalSpending));
            } catch (Exception e) {
                orderCountLabel.setText("获取失败");
                totalSpendingLabel.setText("获取失败");
                System.err.println("获取学生商店信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭按钮点击事件
     */
    @FXML
    private void onClose() {
        studentIdLabel.getScene().getWindow().hide();
    }
}