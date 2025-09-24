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
        // 优先尝试走 Socket 查询服务端数据库（即使未开启开关也尝试，失败再回退）
        try {
            var req = new com.vCampus.net.dto.SocketRequest("SUBJECT_CHOOSES").put("subjectId", subjectId);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(
                    com.vCampus.common.ConfigManager.getSocketServerHost(),
                    com.vCampus.common.ConfigManager.getSocketServerPort()),
                    com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                if (obj instanceof com.vCampus.net.dto.SocketResponse resp
                        && resp.isSuccess()
                        && resp.getData() instanceof java.util.Map<?,?> m
                        && m.get("rows") instanceof java.util.List<?> rows) {
                    chosenStudentsData.clear();
                    for (Object r : rows) {
                        if (r instanceof java.util.Map<?,?> rm) {
                            Student stu = new Student();
                            Object sid = rm.get("studentId");
                            Object sname = rm.get("studentName");
                            stu.setStudentId(sid == null ? "" : String.valueOf(sid));
                            if (sname != null) stu.setStudentName(String.valueOf(sname));
                            chosenStudentsData.add(stu);
                        }
                    }
                    return; // 已经加载完成
                }
            } finally { s.close(); }
        } catch (Exception e) {
            // 忽略，回退到本地
        }
        // 回退到本地服务（单机模式）
        List<Choose> chooses = chooseService.getSubjectChooses(subjectId);
        chosenStudentsData.clear();
        for (Choose choose : chooses) {
            Student student = studentService.getStudentFull(choose.getStudentId());
            if (student != null) {
                chosenStudentsData.add(student);
            }
        }
    }

    private String fetchSelectIdFromServer(String subjectId, String studentId) {
        try {
            var req = new com.vCampus.net.dto.SocketRequest("SUBJECT_CHOOSES").put("subjectId", subjectId);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(
                    com.vCampus.common.ConfigManager.getSocketServerHost(),
                    com.vCampus.common.ConfigManager.getSocketServerPort()),
                    com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                if (obj instanceof com.vCampus.net.dto.SocketResponse resp
                        && resp.isSuccess()
                        && resp.getData() instanceof java.util.Map<?,?> m
                        && m.get("rows") instanceof java.util.List<?> rows) {
                    for (Object r : rows) {
                        if (r instanceof java.util.Map<?,?> rm) {
                            Object sid = rm.get("studentId");
                            if (sid != null && studentId.equals(String.valueOf(sid))) {
                                Object selId = rm.get("selectid");
                                if (selId != null) return String.valueOf(selId);
                            }
                        }
                    }
                }
            } finally { s.close(); }
        } catch (Exception e) {
            // 忽略，返回空
        }
        return null;
    }

    /**
     * 处理管理员/教师选择学生退课的动作。
     */
    @FXML
    private void onDropStudentSubject() {
        // 权限检查：只有管理员和教师可以操作退课（以会话激活角色优先判断，兼容多角色）
        User currentUser = SessionContext.getCurrentUser();
        String activeRole = com.vCampus.common.SessionContext.getActiveRole();
        boolean isAdmin = (activeRole != null && activeRole.equalsIgnoreCase("ADMIN"))
                || (currentUser != null && currentUser.getRoleSet().contains("ADMIN"));
        boolean isTeacher = (activeRole != null && activeRole.equalsIgnoreCase("TEACHER"))
                || (currentUser != null && currentUser.getRoleSet().contains("TEACHER"));
        if (currentUser == null || !(isAdmin || isTeacher)) {
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

        // 进一步权限检查：教师（非管理员身份）只能退自己教授的课程的学生
        if (!isAdmin && isTeacher) {
            String myTid = null;
            try {
                var t = ServiceFactory.getTeacherService().getByUserId(currentUser.getUserId());
                if (t != null) myTid = t.getTeacherId();
            } catch (Exception ignored) {}
            if (myTid == null || !currentSubject.getTeacherId().equals(myTid)) {
                showWarning("您只能退选您所教授课程的学生。");
                return;
            }
        }

        // 确认对话框
        if (!showConfirmation("确认退课", "确定要为学生《" + selectedStudent.getStudentName() + "》退选课程《" + currentSubject.getSubjectName() + "》吗？")) {
            return;
        }

        // 查找对应的选课记录ID（优先从服务器获取，避免跨机本地库不一致）
        String selectId = null;
        try {
            selectId = fetchSelectIdFromServer(currentSubject.getSubjectId(), selectedStudent.getStudentId());
        } catch (Exception ignored) {}
        if (selectId == null || selectId.isBlank()) {
            // 回退本地
            Choose chooseRecordLocal = chooseService.findByStudentAndSubject(selectedStudent.getStudentId(), currentSubject.getSubjectId());
            if (chooseRecordLocal != null) selectId = chooseRecordLocal.getSelectid();
        }
        if (selectId == null || selectId.isBlank()) {
            showError("未找到该学生与该课程的选课记录。");
            return;
        }

        boolean dropSuccess;
        String msg = null;
        if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
            try {
                var req = new com.vCampus.net.dto.SocketRequest("DROP").put("selectid", selectId);
                java.net.Socket s = new java.net.Socket();
                s.connect(new java.net.InetSocketAddress(
                        com.vCampus.common.ConfigManager.getSocketServerHost(),
                        com.vCampus.common.ConfigManager.getSocketServerPort()),
                        com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
                s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                     java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                    out.writeObject(req); out.flush();
                    Object obj = in.readObject();
                    if (obj instanceof com.vCampus.net.dto.SocketResponse resp) {
                        dropSuccess = resp.isSuccess();
                        msg = resp.getMessage();
                    } else { dropSuccess = false; msg = "服务器返回非法响应"; }
                } finally { s.close(); }
            } catch (Exception e) {
                dropSuccess = false; msg = "连接失败: " + e.getMessage();
            }
        } else {
            dropSuccess = chooseService.dropSubject(selectId);
        }

        if (dropSuccess) {
            showSuccess(msg == null ? ("已成功为学生《" + selectedStudent.getStudentName() + "》退选课程《" + currentSubject.getSubjectName() + "》。") : msg);
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
            showError(msg == null ? "退课失败。请检查是否已超过退选时间或联系系统管理员。" : msg);
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