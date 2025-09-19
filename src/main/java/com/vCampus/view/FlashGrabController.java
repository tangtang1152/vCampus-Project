package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.SessionContext;
import com.vCampus.common.ConfigManager;
import com.vCampus.net.CourseGrabClient;
import com.vCampus.net.CourseGrabResult;
import com.vCampus.entity.Subject;
import com.vCampus.service.IChooseService;
import com.vCampus.service.ISubjectService;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.util.TransactionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class FlashGrabController extends BaseController {

    @FXML private TextField keywordField;
    @FXML private TableView<Subject> table;
    @FXML private TableColumn<Subject, String> colId;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TableColumn<Subject, String> colDate;
    @FXML private TableColumn<Subject, Number> colSlots;
    @FXML private TableColumn<Subject, Number> colCredit;
    @FXML private TableColumn<Subject, String> colTeacher;
    @FXML private TableColumn<Subject, String> colWeekRange;
    @FXML private TableColumn<Subject, String> colWeekType;
    @FXML private TableColumn<Subject, String> colTime;
    @FXML private TableColumn<Subject, String> colRoom;
    @FXML private TableColumn<Subject, Subject> colAction;
    @FXML private Label lbStatus;

    private final ISubjectService subjectService = ServiceFactory.getSubjectService();
    private final IChooseService chooseService = ServiceFactory.getChooseService();
    private final IStudentService studentService = ServiceFactory.getStudentService();
    private final ObservableList<Subject> tableData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        table.setItems(tableData);
        loadData();
    }

    @FXML private void onRefresh() { loadData(); }
    @FXML private void onSearch() { loadData(); }

    private void grab(String subjectId) {
        String studentId = resolveCurrentStudentId();
        if (studentId == null || studentId.isBlank()) { showError("当前登录账号未绑定学生信息，请使用学生账户登录"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认选课");
        confirm.setHeaderText(null);
        confirm.setContentText("确认选课? 课程ID: " + subjectId);
        var r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;
        new Thread(() -> {
            boolean ok;
            String msg;
            if (ConfigManager.isSocketEnabled()) {
                CourseGrabClient client = CourseGrabClient.fromConfig();
                CourseGrabResult rlt = client.choose(studentId, subjectId);
                ok = rlt.isSuccess();
                msg = rlt.getMessage();
            } else {
                ok = chooseService.chooseSubject(studentId, subjectId);
                msg = ok ? "选课成功" : "选课失败：可能已满或已选过";
            }
            final boolean fOk = ok;
            final String fMsg = msg;
            TransactionManager.runLaterSafe(() -> {
                showInformation("结果", fMsg);
                if (fOk) {
                    loadData();
                } else {
                    table.refresh();
                }
            });
        }, "grab-"+subjectId).start();
    }

    private void setupTable() {
        table.setPlaceholder(new Label("未找到课程"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("subjectId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colDate.setCellValueFactory(c -> {
            java.util.Date d = c.getValue().getSubjectDate();
            String s = (d == null) ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
            return new javafx.beans.property.SimpleStringProperty(s);
        });
        colSlots.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSubjectNum()==null?0:c.getValue().getSubjectNum()));
        colCredit.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getCredit()==null?0.0:c.getValue().getCredit()));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherId"));
        colWeekRange.setCellValueFactory(new PropertyValueFactory<>("weekRange"));
        colWeekType.setCellValueFactory(new PropertyValueFactory<>("weekType"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("classTime"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("classroom"));

        colAction.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));
        colAction.setCellFactory(col -> new TableCell<Subject, Subject>() {
            private final Button btn = new Button("选课");
            {
                btn.setOnAction(e -> {
                    Subject s = getItem();
                    if (s != null) grab(s.getSubjectId());
                });
            }
            @Override protected void updateItem(Subject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    btn.setDisable(item.getSubjectNum() != null && item.getSubjectNum() <= 0);
                    setGraphic(btn);
                }
            }
        });
    }

    private String resolveCurrentStudentId() {
        var u = SessionContext.getCurrentUser();
        if (u == null) return null;
        if (u instanceof com.vCampus.entity.Student s) return s.getStudentId();
        try {
            var stu = studentService.getByUserId(u.getUserId());
            return stu == null ? null : stu.getStudentId();
        } catch (Exception e) {
            return null;
        }
    }

    private void loadData() {
        String kw = keywordField == null ? "" : keywordField.getText();
        var list = (kw == null || kw.isBlank()) ? subjectService.getAllSubjects() : subjectService.getSubjectsByName(kw);
        tableData.setAll(list);
        lbStatus.setText("共 " + tableData.size() + " 门课");
        table.refresh();
    }
}


