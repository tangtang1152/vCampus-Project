package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Choose;
import com.vCampus.entity.Student;
import com.vCampus.entity.Subject;
import client.net.SocketClient;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CourseManagementController extends BaseController {

    @FXML private TableView<Subject> table;
    @FXML private TableColumn<Subject, String> colId;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TableColumn<Subject, String> colDate;
    @FXML private TableColumn<Subject, Number> colNum;
    @FXML private TableColumn<Subject, Number> colCredit;
    @FXML private TableColumn<Subject, String> colTeacher;
    @FXML private TableColumn<Subject, String> colWeekRange;
    @FXML private TableColumn<Subject, String> colWeekType;
    @FXML private TableColumn<Subject, String> colTime;
    @FXML private TableColumn<Subject, String> colRoom;
    @FXML private TableColumn<Subject, Number> colChosenCount;

    @FXML private TextField keywordField;

    // 移除直接依赖服务，改为通过 SocketClient 通信

    private final ObservableList<Subject> data = FXCollections.observableArrayList();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 权限校验：确保只有管理员和教师角色能访问课程维护页 (MainController已经做了一次，这里可以再加一层保险)
        var user = SessionContext.getCurrentUser();
        if (user == null || user.getRole() == null ||
            (!user.getRole().toLowerCase().contains("admin") && !user.getRole().toLowerCase().contains("teacher"))) {
            // 如果通过其他方式直接访问此Fxml，可以在这里做一次提示或跳转。
            // 为了简化，这里假设通过MainController的权限检查。
            System.out.println("警告: 非管理员或教师尝试访问课程管理。");
        }

        initTableColumns();
        table.setItems(data);
        addContextMenu();
        refresh();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectName()));
        colDate.setCellValueFactory(c -> {
            java.util.Date date = c.getValue().getSubjectDate();
            return new SimpleStringProperty(date == null ? "" : sdf.format(date));
        });
        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSubjectNum() == null ? 0 : c.getValue().getSubjectNum()));
        colCredit.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getCredit() == null ? 0.0 : c.getValue().getCredit()));
        colTeacher.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeacherId()));
        colWeekRange.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeekRange()));
        colWeekType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeekType()));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassTime()));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassroom()));

        colChosenCount.setCellValueFactory(c -> {
            String subjectId = c.getValue().getSubjectId();
            if (subjectId == null || subjectId.isEmpty()) return new SimpleIntegerProperty(0);
            try {
                String request = "GET_SUBJECT_CHOOSES:" + subjectId;
                String response = SocketClient.sendRequest(request);
                if (response != null && response.startsWith("SUCCESS:CHOOSES:")) {
                    List<Choose> chooses = parseChoosesFromResponse(response);
                    return new SimpleIntegerProperty(chooses.size());
                }
            } catch (Exception e) {
                // 忽略错误，返回0
            }
            return new SimpleIntegerProperty(0);
        });
    }

    /**
     * 添加右键菜单，包括查看已选学生和管理员代选功能。
     */
    private void addContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem viewStudentsItem = new MenuItem("查看已选学生");
        viewStudentsItem.setOnAction(event -> onViewChosenStudents());
        
        MenuItem assistChooseItem = new MenuItem("管理员代选");
        assistChooseItem.setOnAction(event -> onAdminAssistChoose());
        
        var user = SessionContext.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            contextMenu.getItems().addAll(viewStudentsItem, assistChooseItem);
        } else {
            // 教师或普通用户只能查看已选学生
            contextMenu.getItems().add(viewStudentsItem);
        }
        
        table.setContextMenu(contextMenu);
    }

    @FXML 
    private void onRefresh() { 
        refresh(); 
    }
    
    @FXML 
    private void onSearch() { 
        refresh(); 
    }

    private void refresh() {
        try {
            String kw = keywordField == null ? "" : keywordField.getText();
            if (kw == null) kw = "";
            
            // 确保请求格式正确：GET_SUBJECTS_BY_NAME:关键词
            String request = "GET_SUBJECTS_BY_NAME:" + kw;
            System.out.println("[CourseMgmt] 发送请求: " + request);
            String response = SocketClient.sendRequest(request);
            System.out.println("[CourseMgmt] 收到响应: " + response);
            
            if (response != null && response.startsWith("SUCCESS:SUBJECTS:")) {
                List<Subject> list = parseSubjectsFromResponse(response);
                System.out.println("[CourseMgmt] 解析到 " + list.size() + " 门课程");
                data.setAll(list);
                System.out.println("[CourseMgmt] 表格数据已更新，当前显示 " + data.size() + " 门课程");
            } else {
                System.err.println("[CourseMgmt] 获取课程列表失败: " + response);
                showError("获取课程列表失败: " + response);
                data.clear();
            }
        } catch (Exception e) {
            System.err.println("[CourseMgmt] 网络连接失败: " + e.getMessage());
            e.printStackTrace();
            showError("网络连接失败: " + e.getMessage());
        }
    }

    @FXML 
    private void onAdd() { 
        editDialog(null); 
    }
    
    @FXML 
    private void onEdit() { 
        editDialog(table.getSelectionModel().getSelectedItem()); 
    }

    @FXML 
    private void onDelete() {
        Subject sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { 
            showWarning("请选择要删除的课程"); 
            return; 
        }

        var user = SessionContext.getCurrentUser();
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            showWarning("您没有权限删除课程。");
            return;
        }

        if (!showConfirmation("删除课程", "确认删除课程《" + sel.getSubjectName() + "》? 这将同时删除所有相关选课记录！")) {
            return;
        }

        try {
            // 检查是否有学生已选此课程
            String checkRequest = "GET_SUBJECT_CHOOSES:" + sel.getSubjectId();
            String checkResponse = SocketClient.sendRequest(checkRequest);
            if (checkResponse != null && checkResponse.startsWith("SUCCESS:CHOOSES:")) {
                List<Choose> chooses = parseChoosesFromResponse(checkResponse);
                if (!chooses.isEmpty()) {
                    if (!showConfirmation("警告", "该课程有学生已选，删除将导致这些学生的选课记录丢失。确定要继续吗？")) {
                        return;
                    }
                }
            }

            // 删除课程
            String deleteRequest = "DELETE_SUBJECT:" + sel.getSubjectId();
            String deleteResponse = SocketClient.sendRequest(deleteRequest);
            if (deleteResponse != null && deleteResponse.startsWith("SUCCESS:DELETE:")) {
                showInformation("提示", "删除成功");
                refresh();
            } else {
                showError("删除失败: " + deleteResponse);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML 
    private void onImportCsv() { 
        showInformation("导入CSV", "占位：可在此实现文件选择并批量导入课程"); 
    }
    
    @FXML 
    private void onExportCsv() { 
        showInformation("导出CSV", "占位：可在此实现导出当前课程列表为CSV"); 
    }

    @FXML 
    private void onViewChosenStudents() {
        Subject sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { 
            showWarning("请选择一门课程查看已选学生"); 
            return; 
        }
        showChosenStudentsDialog(sel);
    }

    /**
     * 管理员代选课程功能。
     * 允许管理员为指定学生代选课程，可以选择性地无视时间冲突。
     */
    @FXML
    private void onAdminAssistChoose() {
        // 权限检查：确保只有管理员能执行此操作
        var user = SessionContext.getCurrentUser();
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            showWarning("您没有权限执行此操作，只有管理员可以代选课程。");
            return;
        }

        Subject selectedSubject = table.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) {
            showWarning("请选择一门要代选的课程。");
            return;
        }

        // 弹出对话框获取学生ID
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("管理员代选课程");
        dialog.setHeaderText("为课程《" + selectedSubject.getSubjectName() + "》代选学生");
        dialog.setContentText("请输入学生的学号:");
        
        Stage currentStage = (Stage) table.getScene().getWindow();
        dialog.initOwner(currentStage);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(studentId -> {
            if (studentId.trim().isEmpty()) {
                showWarning("学生学号不能为空。");
                return;
            }

            try {
                String studentRequest = "GET_STUDENT_BY_ID:" + studentId.trim();
                String studentResponse = SocketClient.sendRequest(studentRequest);
                Student student = null;
                if (studentResponse != null && studentResponse.startsWith("SUCCESS:STUDENT:")) {
                    student = parseStudentFromResponse(studentResponse);
                }
                if (student == null) {
                    showError("学号为 " + studentId.trim() + " 的学生不存在，请检查学号。");
                    return;
                }

            // 询问管理员是否无视时间冲突
            Alert confirmConflictIgnore = new Alert(Alert.AlertType.CONFIRMATION);
            confirmConflictIgnore.setTitle("确认代选设置");
            confirmConflictIgnore.setHeaderText("是否无视时间冲突进行代选？");
            confirmConflictIgnore.setContentText("选择'是'将强制选课，即使学生已有时间冲突的课程。\n(注意：管理员代选默认无视选课有效期。)");
            confirmConflictIgnore.initOwner(currentStage);
            
            ButtonType yesButton = new ButtonType("是", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("否", ButtonBar.ButtonData.NO);
            confirmConflictIgnore.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> ignoreResult = confirmConflictIgnore.showAndWait();
            boolean ignoreTimeConflict = ignoreResult.isPresent() && ignoreResult.get() == yesButton;

                // 调用管理员代选服务方法
                String chooseRequest = "ADMIN_ASSIST_CHOOSE:" + studentId.trim() + ":" + selectedSubject.getSubjectId() + ":" + ignoreTimeConflict;
                String chooseResponse = SocketClient.sendRequest(chooseRequest);
                if (chooseResponse != null && chooseResponse.startsWith("SUCCESS:CHOOSE:")) {
                    showSuccess("已成功为学生 " + student.getStudentName() + " (" + studentId.trim() + ") 代选课程《" + selectedSubject.getSubjectName() + "》。");
                    refresh();
                } else {
                    showError("代选课程失败: " + chooseResponse);
                }
            } catch (Exception e) {
                showError("网络连接失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 新增/编辑课程的对话框。
     * @param origin 待编辑的课程对象，如果为null则表示新增课程。
     */
    private void editDialog(Subject origin) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(origin == null ? "新增课程" : "编辑课程");
        
        Stage currentStage = (Stage) table.getScene().getWindow();
        dlg.initOwner(currentStage);
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(10));

        TextField subjectId = new TextField(origin == null ? "" : origin.getSubjectId());
        if (origin != null) subjectId.setEditable(false);
        
        TextField subjectName = new TextField(origin == null ? "" : origin.getSubjectName());
        
        LocalDate initialDate = null;
        if (origin != null && origin.getSubjectDate() != null) {
            initialDate = new java.sql.Date(origin.getSubjectDate().getTime()).toLocalDate(); 
        }
        DatePicker subjectDate = new DatePicker(initialDate);

        Spinner<Integer> subjectNum = new Spinner<>(0, 1000, origin == null || origin.getSubjectNum() == null ? 0 : origin.getSubjectNum());
        SpinnerValueFactory<Double> creditValueFactory = 
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10.0, origin == null || origin.getCredit() == null ? 0.0 : origin.getCredit(), 0.5);
        Spinner<Double> credit = new Spinner<>(creditValueFactory);
        credit.setEditable(true);

        TextField teacherId = new TextField(origin == null ? "" : origin.getTeacherId());
        TextField weekRange = new TextField(origin == null ? "1-16" : origin.getWeekRange());
        ComboBox<String> weekType = new ComboBox<>(FXCollections.observableArrayList("ALL", "ODD", "EVEN"));
        weekType.setValue(origin == null || origin.getWeekType() == null || origin.getWeekType().isEmpty() ? "ALL" : origin.getWeekType());
        TextField classTime = new TextField(origin == null ? "" : origin.getClassTime());
        TextField classroom = new TextField(origin == null ? "" : origin.getClassroom());

        grid.addRow(0, new Label("课程号"), subjectId);
        grid.addRow(1, new Label("课程名称"), subjectName);
        grid.addRow(2, new Label("开课日期"), subjectDate);
        grid.addRow(3, new Label("课程容量"), subjectNum);
        grid.addRow(4, new Label("学分"), credit);
        grid.addRow(5, new Label("教师ID"), teacherId);
        grid.addRow(6, new Label("周次范围"), weekRange);
        grid.addRow(7, new Label("单双周"), weekType);
        grid.addRow(8, new Label("上课时间"), classTime);
        grid.addRow(9, new Label("教室"), classroom);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                String id = subjectId.getText().trim();
                String name = subjectName.getText().trim();
                LocalDate dateValue = subjectDate.getValue();
                Integer numValue = subjectNum.getValue();
                Double creditValue = credit.getValue();
                String teacher = teacherId.getText().trim();
                String range = weekRange.getText().trim();
                String type = weekType.getValue();
                String time = classTime.getText().trim();
                String room = classroom.getText().trim();

                Subject tempSubject = new Subject();
                tempSubject.setSubjectId(id);
                tempSubject.setSubjectName(name);
                tempSubject.setSubjectDate(dateValue != null ? Date.valueOf(dateValue) : null);
                tempSubject.setSubjectNum(numValue);
                tempSubject.setCredit(creditValue);
                tempSubject.setTeacherId(teacher);
                tempSubject.setWeekRange(range);
                tempSubject.setWeekType(type);
                tempSubject.setClassTime(time);
                tempSubject.setClassroom(room);

                try {
                    // 验证课程信息
                    String validateRequest = "VALIDATE_SUBJECT:" + id + "," + name + "," + teacher + "," + range + "," + type + "," + time + "," + room + "," + numValue + "," + creditValue;
                    String validateResponse = SocketClient.sendRequest(validateRequest);
                    if (validateResponse == null || !validateResponse.startsWith("SUCCESS:VALIDATE:")) {
                        showError("课程信息验证失败，请检查输入！");
                        return;
                    }
                    
                    Subject s = origin == null ? new Subject() : origin;
                    s.setSubjectId(id);
                    s.setSubjectName(name);
                    s.setSubjectDate(Date.valueOf(dateValue));
                    s.setSubjectNum(numValue);
                    s.setCredit(creditValue);
                    s.setTeacherId(teacher);
                    s.setWeekRange(range);
                    s.setWeekType(type);
                    s.setClassTime(time);
                    s.setClassroom(room);
                    
                    // 保存课程
                    String saveRequest;
                    if (origin == null) {
                        saveRequest = "ADD_SUBJECT:" + id + "," + name + "," + dateValue + "," + numValue + "," + creditValue + "," + teacher + "," + range + "," + type + "," + time + "," + room;
                    } else {
                        saveRequest = "UPDATE_SUBJECT:" + id + "," + name + "," + dateValue + "," + numValue + "," + creditValue + "," + teacher + "," + range + "," + type + "," + time + "," + room;
                    }
                    String saveResponse = SocketClient.sendRequest(saveRequest);
                    if (saveResponse != null && (saveResponse.startsWith("SUCCESS:ADD_SUBJECT:") || saveResponse.startsWith("SUCCESS:UPDATE_SUBJECT:"))) {
                        showInformation("提示", origin == null ? "新增成功" : "保存成功");
                        refresh();
                    } else {
                        showError("保存失败: " + saveResponse);
                    }
                } catch (Exception e) {
                    showError("网络连接失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showChosenStudentsDialog(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chosen-students-dialog.fxml"));
            Parent root = loader.load(); 
            ChosenStudentsDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("课程《" + subject.getSubjectName() + "》的已选学生");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(table.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller.setCourse(subject);

            dialogStage.showAndWait();

        } catch (IOException e) {
            showError("无法加载已选学生对话框: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("打开已选学生对话框时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
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

    /**
     * 从服务器响应解析课程列表
     */
    private List<Subject> parseSubjectsFromResponse(String response) {
        List<Subject> subjects = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:SUBJECTS:")) {
                String data = response.substring("SUCCESS:SUBJECTS:".length());
                if (!data.isEmpty()) {
                    String[] subjectStrings = data.split("\\|");
                    for (String subjectString : subjectStrings) {
                        String[] fields = subjectString.split(",");
                        if (fields.length >= 10) {
                            Subject subject = new Subject();
                            subject.setSubjectId(fields[0]);
                            subject.setSubjectName(fields[1]);
                            // 跳过 subjectDate (fields[2]) - 开课日期
                            subject.setSubjectNum(Integer.parseInt(fields[3]));
                            subject.setCredit(Double.parseDouble(fields[4]));
                            subject.setTeacherId(fields[5]);
                            subject.setWeekRange(fields[6]);
                            subject.setWeekType(fields[7]);
                            subject.setClassTime(fields[8]);
                            subject.setClassroom(fields[9]);
                            subjects.add(subject);
                        } else {
                            System.err.println("[CourseMgmt] 课程数据字段不足: " + subjectString + " (期望10个字段，实际" + fields.length + "个)");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[CourseMgmt] 解析课程数据异常: " + e.getMessage());
            e.printStackTrace();
        }
        return subjects;
    }
}