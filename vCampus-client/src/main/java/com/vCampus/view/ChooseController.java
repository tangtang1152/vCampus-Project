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
        String kw = keywordField.getText() == null ? "" : keywordField.getText();
        List<Subject> list;
        if (kw.isBlank()) {
            list = subjectService.getAllSubjects();
        } else {
            list = subjectService.getSubjectsByName(kw);
        }
        allSubjects.setAll(list);
        infoLabel.setText("共 " + list.size() + " 条可选课程");
    }

    private void loadMy() {
        mySubjects.setAll(chooseService.getStudentSubjects(getCurrentStudentId()));
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