package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.entity.Subject;
import com.vCampus.service.ChooseServiceImpl;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.service.SubjectServiceImpl;
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

    private final SubjectServiceImpl subjectService = new SubjectServiceImpl();
    private final ChooseServiceImpl chooseService = new ChooseServiceImpl();
    private final ObservableList<Subject> allSubjects = FXCollections.observableArrayList();
    private final ObservableList<Subject> mySubjects = FXCollections.observableArrayList();
    private javafx.animation.Timeline autoRefresh;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCols();
        subjectTable.setItems(allSubjects);
        myTable.setItems(mySubjects);
        loadAll();
        loadMy();
        startAutoRefresh();
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
        String kw = keywordField.getText() == null ? "" : keywordField.getText();
        new Thread(() -> {
            java.util.List<Subject> list;
            if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
                list = fetchSubjectsFromServer(kw);
                if (list == null) list = fetchSubjectsLocally(kw);
            } else {
                list = fetchSubjectsLocally(kw);
            }
            final java.util.List<Subject> flist = list;
            com.vCampus.util.TransactionManager.runLaterSafe(() -> {
                allSubjects.setAll(flist);
                infoLabel.setText("共 " + flist.size() + " 条可选课程");
            });
        }, "choose-loadAll").start();
    }

    private java.util.List<Subject> fetchSubjectsFromServer(String kw) {
        try {
            var req = new com.vCampus.net.dto.SocketRequest("SUBJECT_LIST").put("keyword", kw);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m && m.get("rows") instanceof java.util.List<?> rows) {
                    java.util.List<Subject> list = new java.util.ArrayList<>();
                    for (Object r : rows) {
                        if (r instanceof java.util.Map<?,?> rm) {
                            Subject sObj = new Subject();
                            sObj.setSubjectId(String.valueOf(rm.get("subjectId")));
                            sObj.setSubjectName(String.valueOf(rm.get("subjectName")));
                            Object dt = rm.get("subjectDate");
                            if (dt instanceof java.sql.Date d) sObj.setSubjectDate(new java.util.Date(d.getTime()));
                            else if (dt instanceof java.util.Date d2) sObj.setSubjectDate(d2);
                            Object sn = rm.get("subjectNum"); if (sn != null) sObj.setSubjectNum(((Number)sn).intValue());
                            Object cr = rm.get("credit"); if (cr != null) sObj.setCredit(((Number)cr).doubleValue());
                            sObj.setTeacherId(String.valueOf(rm.get("teacherId")));
                            sObj.setWeekRange(String.valueOf(rm.get("weekRange")));
                            sObj.setWeekType(String.valueOf(rm.get("weekType")));
                            sObj.setClassTime(String.valueOf(rm.get("classTime")));
                            sObj.setClassroom(String.valueOf(rm.get("classroom")));
                            list.add(sObj);
                        }
                    }
                    return list;
                }
            } finally { s.close(); }
        } catch (Exception ignored) {}
        return null;
    }

    private java.util.List<Subject> fetchSubjectsLocally(String kw) {
        return kw == null || kw.isBlank() ? subjectService.getAllSubjects() : subjectService.getSubjectsByName(kw);
    }

    private void loadMy() {
        new Thread(() -> {
            java.util.List<Subject> list;
            if (com.vCampus.common.ConfigManager.isSocketEnabled()) {
                list = fetchMySubjectsFromServer(getCurrentStudentId());
                if (list == null) list = chooseService.getStudentSubjects(getCurrentStudentId());
            } else {
                list = chooseService.getStudentSubjects(getCurrentStudentId());
            }
            final java.util.List<Subject> flist = list;
            com.vCampus.util.TransactionManager.runLaterSafe(() -> mySubjects.setAll(flist));
        }, "choose-loadMy").start();
    }

    private java.util.List<Subject> fetchMySubjectsFromServer(String sid) {
        try {
            var req = new com.vCampus.net.dto.SocketRequest("MY_SUBJECTS").put("studentId", sid);
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(com.vCampus.common.ConfigManager.getSocketServerHost(), com.vCampus.common.ConfigManager.getSocketServerPort()), com.vCampus.common.ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(com.vCampus.common.ConfigManager.getSocketSoTimeoutMs());
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
                 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
                out.writeObject(req); out.flush();
                Object obj = in.readObject();
                if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m && m.get("rows") instanceof java.util.List<?> rows) {
                    java.util.List<Subject> list = new java.util.ArrayList<>();
                    for (Object r : rows) {
                        if (r instanceof java.util.Map<?,?> rm) {
                            Subject sObj = new Subject();
                            sObj.setSubjectId(String.valueOf(rm.get("subjectId")));
                            sObj.setSubjectName(String.valueOf(rm.get("subjectName")));
                            Object dt = rm.get("subjectDate");
                            if (dt instanceof java.sql.Date d) sObj.setSubjectDate(new java.util.Date(d.getTime()));
                            else if (dt instanceof java.util.Date d2) sObj.setSubjectDate(d2);
                            Object sn = rm.get("subjectNum"); if (sn != null) sObj.setSubjectNum(((Number)sn).intValue());
                            Object cr = rm.get("credit"); if (cr != null) sObj.setCredit(((Number)cr).doubleValue());
                            sObj.setTeacherId(String.valueOf(rm.get("teacherId")));
                            sObj.setWeekRange(String.valueOf(rm.get("weekRange")));
                            sObj.setWeekType(String.valueOf(rm.get("weekType")));
                            sObj.setClassTime(String.valueOf(rm.get("classTime")));
                            sObj.setClassroom(String.valueOf(rm.get("classroom")));
                            list.add(sObj);
                        }
                    }
                    return list;
                }
            } finally { s.close(); }
        } catch (Exception ignored) {}
        return null;
    }

    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                    loadAll();
                    loadMy();
                })
        );
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();
        subjectTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, ov, nv) -> {
                    if (nv != null) nv.setOnHidden(evt -> { if (autoRefresh != null) autoRefresh.stop(); });
                });
            }
        });
    }

    @FXML
    private void onChoose() {
        Subject sel = subjectTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要选的课程"); return; }
        boolean ok = chooseService.chooseSubject(getCurrentStudentId(), sel.getSubjectId());
        if (ok) { showSuccess("选课成功"); loadAll(); loadMy(); } else { showError("选课失败"); }
    }

    @FXML
    private void onDrop() {
        Subject sel = myTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要退的课程"); return; }
        // 根据学生id和课程id找到对应的选课记录id
        var chooses = chooseService.getSubjectChooses(sel.getSubjectId());
        var myRecord = chooses.stream()
                .filter(c -> c.getStudentId().equals(getCurrentStudentId()))
                .findFirst()
                .orElse(null);
        if (myRecord == null) { showError("未找到选课记录"); return; }
        boolean ok = chooseService.dropSubject(myRecord.getSelectid());
        if (ok) { showSuccess("退课成功"); loadAll(); loadMy(); } else { showError("退课失败"); }
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
            IStudentService stuSvc = ServiceFactory.getStudentService();
            var stu = stuSvc.getByUserId(u.getUserId());
            return stu == null ? "" : stu.getStudentId();
        } catch (Exception e) {
            return "";
        }
    }
}