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
            String request = "GET_MY_SUBJECTS:" + getCurrentStudentId();
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
        
        try {
            String request = "CHOOSE_SUBJECT:" + getCurrentStudentId() + ":" + sel.getSubjectId();
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
        
        try {
            String request = "DROP_SUBJECT:" + getCurrentStudentId() + ":" + sel.getSubjectId();
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
        if (u == null) return "";
        if (u instanceof com.vCampus.entity.Student s) {
            return s.getStudentId();
        }
        // 兼容：登录保存的是通用 User 时，根据 userId 反查学生学号
        try {
            String request = "GET_STUDENT_BY_USER_ID:" + u.getUserId();
            String response = SocketClient.sendRequest(request);
            if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                String[] parts = response.split(":");
                return parts.length > 2 ? parts[2] : "";
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return "";
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
                        if (fields.length >= 8) {
                            Subject subject = new Subject();
                            subject.setSubjectId(fields[0]);
                            subject.setSubjectName(fields[1]);
                            subject.setSubjectNum(Integer.parseInt(fields[2]));
                            subject.setCredit(Double.parseDouble(fields[3]));
                            subject.setTeacherId(fields[4]);
                            subject.setWeekRange(fields[5]);
                            subject.setWeekType(fields[6]);
                            subject.setClassTime(fields[7]);
                            if (fields.length > 8) {
                                subject.setClassroom(fields[8]);
                            }
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