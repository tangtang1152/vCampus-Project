package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext; // 导入 SessionContext
import com.vCampus.entity.Choose;
import com.vCampus.entity.Student;
import com.vCampus.entity.Subject;
import com.vCampus.entity.User; // 导入 User
import client.net.SocketClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChosenStudentsDialogController extends BaseController {

    @FXML private Label courseNameLabel;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentIdCol;
    @FXML private TableColumn<Student, String> studentNameCol;

    // 移除直接依赖服务，改为通过 SocketClient 通信

    private final ObservableList<Student> chosenStudentsData = FXCollections.observableArrayList();
    private Subject currentSubject; // 用于存储当前显示的课程信息

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        studentNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        studentTable.setItems(chosenStudentsData);
    }

    /**
     * 设置要显示已选学生的课程信息
     * @param subject 课程对象
     */
    public void setCourse(Subject subject) {
        if (subject == null) {
            courseNameLabel.setText("课程信息加载失败");
            return;
        }
        this.currentSubject = subject; // 存储当前课程信息
        courseNameLabel.setText("课程名称: " + subject.getSubjectName() + " (ID: " + subject.getSubjectId() + ")");
        loadChosenStudents(subject.getSubjectId());
    }

    /**
     * 加载已选学生列表
     * @param subjectId 课程ID
     */
    private void loadChosenStudents(String subjectId) {
        try {
            String request = "GET_SUBJECT_CHOOSES:" + subjectId;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:CHOOSES:")) {
                List<Choose> chooses = parseChoosesFromResponse(response);
                chosenStudentsData.clear();
                for (Choose choose : chooses) {
                    String studentRequest = "GET_STUDENT_FULL:" + choose.getStudentId();
                    String studentResponse = SocketClient.sendRequest(studentRequest);
                    if (studentResponse != null && studentResponse.startsWith("SUCCESS:STUDENT:")) {
                        Student student = parseStudentFromResponse(studentResponse);
                        if (student != null) {
                            chosenStudentsData.add(student);
                        }
                    }
                }
            }
        } catch (Exception e) {
            showError("获取选课学生失败: " + e.getMessage());
            chosenStudentsData.clear();
        }
    }

    /**
     * 处理管理员/教师选择学生退课的动作。
     */
    @FXML
    private void onDropStudentSubject() {
        // 权限检查：只有管理员和教师可以操作退课
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null || (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !"TEACHER".equalsIgnoreCase(currentUser.getRole()))) {
            showWarning("您没有权限执行此操作，只有管理员或教师可以退课。");
            return;
        }

        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showWarning("请选择一个要退课的学生。");
            return;
        }
        if (currentSubject == null) {
            showError("课程信息未加载，无法退课。");
            return;
        }

        // 进一步权限检查：教师只能退自己教授的课程的学生
        if ("TEACHER".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentSubject.getTeacherId().equals(((com.vCampus.entity.Teacher) currentUser).getTeacherId())) {
                showWarning("您只能退选您所教授课程的学生。");
                return;
            }
        }

        // 确认对话框
        if (!showConfirmation("确认退课", "确定要为学生《" + selectedStudent.getStudentName() + "》退选课程《" + currentSubject.getSubjectName() + "》吗？")) {
            return;
        }

        // 执行退课操作
        try {
            String request = "ADMIN_DROP_SUBJECT:" + selectedStudent.getStudentId() + ":" + currentSubject.getSubjectId();
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:DROP:")) {
                showSuccess("已成功为学生《" + selectedStudent.getStudentName() + "》退选课程《" + currentSubject.getSubjectName() + "》。");
                loadChosenStudents(currentSubject.getSubjectId()); // 刷新列表
            } else {
                showError("退课失败: " + response);
                return;
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            return;
        }
        
        // 通知 CourseManagementController 刷新其表格，以更新已选人数
            if (getStage().getOwner() instanceof Stage) {
                Stage ownerStage = (Stage) getStage().getOwner();
                if (ownerStage.getScene().lookup("#courseManagementRoot") != null) { // 假设 CourseManagementController 的根节点有一个 id
  // 实际做法需要 CourseManagementController 提供一个公共刷新方法
  // 或者通过 EventBus 等机制通知
  // 简单粗暴的方案（仅为示例，不推荐）：
  // ((CourseManagementController)((FXMLLoader)ownerStage.getScene().getUserData()).getController()).refresh();
  // 更好的方法是 CourseManagementController 监听这个对话框关闭事件或者通过回调
  // 为了简化，目前假设 CourseManagementController 的 refresh 按钮会刷新
  // 或者直接重新加载 CourseManagementController 的数据 (在实际应用中要避免)
                }
            }
    }


    @FXML
    private void onClose() {
        Stage stage = (Stage) courseNameLabel.getScene().getWindow();
        stage.close();
    }
    
    // 辅助方法，用于获取当前对话框的Stage，以便设置Alert的所有者
    private Stage getStage() {
        return (Stage) courseNameLabel.getScene().getWindow();
    }

    /**
     * 从服务器响应解析选课记录列表
     */
    private List<Choose> parseChoosesFromResponse(String response) {
        List<Choose> chooses = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:CHOOSES:")) {
                String data = response.substring("SUCCESS:CHOOSES:".length());
                if (!data.isEmpty()) {
                    String[] chooseStrings = data.split("\\|");
                    for (String chooseString : chooseStrings) {
                        String[] fields = chooseString.split(",");
                        if (fields.length >= 3) {
                            Choose choose = new Choose();
                            choose.setSelectid(fields[0]);
                            choose.setStudentId(fields[1]);
                            choose.setSubjectId(fields[2]);
                            chooses.add(choose);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析选课记录数据异常: " + e.getMessage());
        }
        return chooses;
    }

    /**
     * 从服务器响应解析学生信息
     */
    private Student parseStudentFromResponse(String response) {
        try {
            if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                String data = response.substring("SUCCESS:STUDENT:".length());
                String[] fields = data.split(",");
                if (fields.length >= 6) {
                    Student student = new Student();
                    student.setStudentId(fields[0]);
                    student.setStudentName(fields[1]);
                    student.setUserId(Integer.parseInt(fields[2]));
                    student.setClassName(fields[3]);
                    student.setSex(fields[4]);
                    student.setEmail(fields[5]);
                    return student;
                }
            }
        } catch (Exception e) {
            System.err.println("解析学生数据异常: " + e.getMessage());
        }
        return null;
    }
}