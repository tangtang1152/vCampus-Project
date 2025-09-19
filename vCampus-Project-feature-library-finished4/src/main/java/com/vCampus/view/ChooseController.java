package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Subject;
import client.net.SocketClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ChooseController extends BaseController {
    @FXML private TextField keywordField;
    @FXML private Label infoLabel;

    // 可选课程表
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, String> colId;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TableColumn<Subject, String> colDate;
    @FXML private TableColumn<Subject, String> colNum;
    @FXML private TableColumn<Subject, String> colCredit;
    @FXML private TableColumn<Subject, String> colTeacher;
    @FXML private TableColumn<Subject, String> colWeekRange;
    @FXML private TableColumn<Subject, String> colWeekType;
    @FXML private TableColumn<Subject, String> colTime;
    @FXML private TableColumn<Subject, String> colRoom;

    // 已选课程表
    @FXML private TableView<Subject> myTable;
    @FXML private TableColumn<Subject, String> myColId;
    @FXML private TableColumn<Subject, String> myColName;
    @FXML private TableColumn<Subject, String> myColTeacher;
    @FXML private TableColumn<Subject, String> myColTime;

    // 移除直接依赖服务，改为通过 SocketClient 通信
    private final ObservableList<Subject> allSubjects = FXCollections.observableArrayList();
    private final ObservableList<Subject> mySubjects = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCols();
        subjectTable.setItems(allSubjects);
        myTable.setItems(mySubjects);
        loadAll();
        loadMy();
    }

    private void initCols() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectName()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(sdf.format(c.getValue().getSubjectDate())));
        colNum.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSubjectNum())));
        colCredit.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCredit())));
        colTeacher.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeacherId()));
        colWeekRange.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeekRange()));
        colWeekType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeekType()));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassTime()));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassroom()));

        myColId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectId()));
        myColName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubjectName()));
        myColTeacher.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeacherId()));
        myColTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassTime()));
    }

    @FXML
    private void onSearch() {
        loadAll();
    }

    private void loadAll() {
        try {
            String kw = keywordField.getText() == null ? "" : keywordField.getText();
            String request = "GET_SUBJECTS:" + kw;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:SUBJECTS:")) {
                List<Subject> list = parseSubjectsFromResponse(response);
                allSubjects.setAll(list);
                infoLabel.setText("共 " + list.size() + " 条可选课程");
            } else {
                showError("获取课程列表失败: " + response);
                allSubjects.clear();
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMy() {
        try {
            String studentId = getCurrentStudentId();
            if (studentId == null) {
                // 如果无法获取学生ID，清空我的课程列表
                mySubjects.clear();
                return;
            }
            
            String request = "GET_MY_SUBJECTS:" + studentId;
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:SUBJECTS:")) {
                List<Subject> subjects = parseSubjectsFromResponse(response);
                mySubjects.setAll(subjects);
            } else {
                showError("获取我的课程失败: " + response);
                mySubjects.clear();
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onChoose() {
        Subject sel = subjectTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要选的课程"); return; }
        
        String studentId = getCurrentStudentId();
        if (studentId == null) {
            return; // 错误信息已在 getCurrentStudentId 中显示
        }
        
        try {
            String request = "CHOOSE_SUBJECT:" + studentId + ":" + sel.getSubjectId();
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:CHOOSE:")) {
                showSuccess("选课成功");
                loadAll();
                loadMy();
            } else {
                showError("选课失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDrop() {
        Subject sel = myTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要退的课程"); return; }
        
        String studentId = getCurrentStudentId();
        if (studentId == null) {
            return; // 错误信息已在 getCurrentStudentId 中显示
        }
        
        try {
            String request = "DROP_SUBJECT:" + studentId + ":" + sel.getSubjectId();
            String response = SocketClient.sendRequest(request);
            
            if (response != null && response.startsWith("SUCCESS:DROP:")) {
                showSuccess("退课成功");
                loadAll();
                loadMy();
            } else {
                showError("退课失败: " + response);
            }
        } catch (Exception e) {
            showError("网络连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onRefreshMySubjects() {
        loadMy();
    }

    private String getCurrentStudentId() {
        var u = SessionContext.getCurrentUser();
        if (u == null) {
            showError("用户未登录，请重新登录");
            return null;
        }
        
        // 如果是学生，直接返回学号
        if (u instanceof com.vCampus.entity.Student s) {
            return s.getStudentId();
        }
        
        // 如果是管理员，显示提示信息
        if (u instanceof com.vCampus.entity.Admin) {
            showWarning("管理员无法使用选课功能，请以学生身份登录");
            return null;
        }
        
        // 如果是教师，显示提示信息
        if (u instanceof com.vCampus.entity.Teacher) {
            showWarning("教师无法使用选课功能，请以学生身份登录");
            return null;
        }
        
        // 兼容：登录保存的是通用 User 时，根据 userId 反查学生学号
        try {
            String request = "GET_STUDENT_BY_USER_ID:" + u.getUserId();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                String[] parts = response.split(":");
                String studentId = parts.length > 2 ? parts[2] : null;
                if (studentId != null && !studentId.trim().isEmpty()) {
                    return studentId;
                }
            } else if (response != null && response.startsWith("ERROR:")) {
                // 服务器返回了明确的错误信息
                showError("获取学生信息失败: " + response.substring(6)); // 去掉 "ERROR:" 前缀
                return null;
            }
        } catch (Exception e) {
            System.err.println("获取学生信息失败: " + e.getMessage());
        }
        
        showError("当前用户没有学生身份，无法使用选课功能。请使用学生账号登录。");
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
                            // 跳过 subjectDate (fields[2])
                            subject.setSubjectNum(Integer.parseInt(fields[3]));
                            subject.setCredit(Double.parseDouble(fields[4]));
                            subject.setTeacherId(fields[5]);
                            subject.setWeekRange(fields[6]);
                            subject.setWeekType(fields[7]);
                            subject.setClassTime(fields[8]);
                            subject.setClassroom(fields[9]);
                            subjects.add(subject);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析课程数据异常: " + e.getMessage());
        }
        return subjects;
    }
}