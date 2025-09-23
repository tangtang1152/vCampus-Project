package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext; // 导入 SessionContext
import com.vCampus.entity.Choose;
import com.vCampus.entity.Student;
import com.vCampus.entity.Subject;
import com.vCampus.entity.User; // 导入 User
import com.vCampus.service.IChooseService; // 导入 IChooseService
import com.vCampus.service.IStudentService; // 导入 IStudentService
import com.vCampus.service.ServiceFactory; // 导入 ServiceFactory
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

    // 使用 ServiceFactory 获取 Service 实例
    private final IChooseService chooseService = ServiceFactory.getChooseService();
    private final IStudentService studentService = ServiceFactory.getStudentService();

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
        List<Choose> chooses = chooseService.getSubjectChooses(subjectId);
        chosenStudentsData.clear();
        for (Choose choose : chooses) {
            Student student = studentService.getStudentFull(choose.getStudentId()); // 使用 getStudentFull 获取更完整的学生信息
            if (student != null) {
                chosenStudentsData.add(student);
            }
        }
        // 如果列表为空，TableView 的 placeholder 会自动显示
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

        // 查找对应的选课记录ID
        Choose chooseRecord = chooseService.findByStudentAndSubject(selectedStudent.getStudentId(), currentSubject.getSubjectId());
        
        if (chooseRecord == null) {
            showError("未找到该学生与该课程的选课记录。");
            return;
        }

        // 执行退课操作
        // 注意：chooseService.dropSubject() 内部会检查退选有效期。
        // 如果需要管理员/教师无视退选有效期强制退课，ChooseServiceImpl 也需要修改，
        // 增加一个 `adminForceDropSubject` 类似的方法。
        // 这里我们假设管理员/教师退课也遵循退选有效期。
        boolean dropSuccess = chooseService.dropSubject(chooseRecord.getSelectid());

        if (dropSuccess) {
            showSuccess("已成功为学生《" + selectedStudent.getStudentName() + "》退选课程《" + currentSubject.getSubjectName() + "》。");
            loadChosenStudents(currentSubject.getSubjectId()); // 刷新列表
            
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


        } else {
            showError("退课失败。请检查是否已超过退选时间或联系系统管理员。");
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
}