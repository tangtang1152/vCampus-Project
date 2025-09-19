package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StudentManagementController extends BaseController {

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colStudentName;
    @FXML private TableColumn<Student, String> colClassName;
    @FXML private TableColumn<Student, String> colSex;
    @FXML private TableColumn<Student, java.util.Date> colEnrollDate;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colIdCard;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TextField tfKeyword;
    @FXML private javafx.scene.control.ComboBox<String> cbStatusFilter;

    private final IStudentService studentService = ServiceFactory.getStudentService();
    private FilteredList<Student> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colSex.setCellValueFactory(new PropertyValueFactory<>("sex"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colIdCard.setCellValueFactory(new PropertyValueFactory<>("idCard"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // 入学日期格式化（使用 Date 泛型）
        colEnrollDate.setCellValueFactory(new PropertyValueFactory<>("enrollDate"));
        colEnrollDate.setCellFactory(col -> new javafx.scene.control.TableCell<Student, java.util.Date>() {
            private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            @Override protected void updateItem(java.util.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : sdf.format(item));
            }
        });
        // 调试与布局优化：固定单元格高度，便于滚动条计算
        if (table != null) {
            table.setFixedCellSize(28);
            table.heightProperty().addListener((obs, o, n) -> System.out.println("[SM] table.height=" + n));
            table.widthProperty().addListener((obs, o, n) -> System.out.println("[SM] table.width=" + n));
            table.sceneProperty().addListener((obs, old, sc) -> {
                if (sc != null) {
                    sc.heightProperty().addListener((o2, ov, nv) -> System.out.println("[SM] scene.height=" + nv));
                    sc.widthProperty().addListener((o2, ov, nv) -> System.out.println("[SM] scene.width=" + nv));
                }
            });
        }
        // 首次渲染后刷新，避免初次为空
        javafx.application.Platform.runLater(this::refresh);
        if (cbStatusFilter != null) {
            cbStatusFilter.getItems().setAll("全部", "正常", "休学", "退学", "毕业");
            cbStatusFilter.setValue("全部");
            cbStatusFilter.setOnAction(e -> applyFilter(tfKeyword.getText()));
        }
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        List<Student> all = studentService.getAll();
        filtered = new FilteredList<>(FXCollections.observableArrayList(all), s -> true);
        table.setItems(filtered);
        applyFilter(tfKeyword == null ? null : tfKeyword.getText());
        ensureScrollbarsProbe();
    }

    @FXML
    private void onSearch() {
        applyFilter(tfKeyword.getText());
    }

    private void applyFilter(String kw) {
        if (filtered == null) return;
        String keyword = kw == null ? "" : kw.trim();
        String statusSel = cbStatusFilter == null ? "全部" : cbStatusFilter.getValue();
        if (keyword.isEmpty()) {
            filtered.setPredicate(s ->
                ("全部".equals(statusSel) || (s.getStatus() != null && s.getStatus().equals(statusSel)))
            );
        } else {
            filtered.setPredicate(s ->
                    ((s.getStudentId() != null && s.getStudentId().contains(keyword)) ||
                    (s.getStudentName() != null && s.getStudentName().contains(keyword)) ||
                    (s.getClassName() != null && s.getClassName().contains(keyword))) &&
                    ("全部".equals(statusSel) || (s.getStatus() != null && s.getStatus().equals(statusSel)))
            );
        }
    }

    @FXML
    private void onEdit() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) { showWarning("请先选择一名学生"); return; }
        openStudentFormDialog(s, "编辑学生");
    }

    @FXML
    private void onAdd() {
        openStudentFormDialog(null, "新增学生");
    }

    @FXML
    private void onDetail() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) { showWarning("请先选择一名学生"); return; }
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student-detail-view.fxml"));
            javafx.scene.Parent root = loader.load();
            var ctrl = (StudentDetailController) loader.getController();
            ctrl.setStudent(s);
            var stage = new javafx.stage.Stage();
            stage.setTitle("学生详情");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开详情失败: " + e.getMessage());
        }
    }

    private void openStudentFormDialog(Student editing, String title) {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student-form-view.fxml"));
            javafx.scene.Parent root = loader.load();
            var ctrl = (StudentFormController) loader.getController();
            if (editing != null) ctrl.setEditing(editing);
            var stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setResizable(true);
            stage.setWidth(540); stage.setHeight(600);
            stage.showAndWait();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开表单失败: " + e.getMessage());
        }
    }

    // 调试：探测滚动条可见性
    private void ensureScrollbarsProbe() {
        javafx.application.Platform.runLater(() -> {
            try {
                var bars = table.lookupAll(".scroll-bar");
                for (var node : bars) {
                    if (node instanceof javafx.scene.control.ScrollBar sb) {
                        System.out.println("[SM] scrollbar " + sb.getOrientation() + " visible=" + sb.isVisible() + ", value=" + sb.getValue());
                    }
                }
                System.out.println("[SM] items=" + (table.getItems()==null?0:table.getItems().size()) + ", height=" + table.getHeight());
            } catch (Exception ignored) {}
        });
    }
}


