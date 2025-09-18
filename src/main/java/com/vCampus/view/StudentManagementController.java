package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.common.NavigationUtil;
import com.vCampus.entity.Student;
import com.vCampus.service.IStudentService;
import com.vCampus.service.ServiceFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 学籍管理控制器
 */
public class StudentManagementController extends BaseController {
    
    @FXML private TableView<Student> studentTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> classFilter;
    
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private IStudentService studentService = ServiceFactory.getStudentService();
    
    /**
     * 初始化方法
     */
    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadStudentData();
        setupRowStyle();
    }
    
    /**
     * 设置表格
     */
    private void setupTable() {
        // 获取所有列引用
        TableColumn<Student, String> studentIdCol = (TableColumn<Student, String>) studentTable.getColumns().get(0);
        TableColumn<Student, String> studentNameCol = (TableColumn<Student, String>) studentTable.getColumns().get(1);
        TableColumn<Student, String> classNameCol = (TableColumn<Student, String>) studentTable.getColumns().get(2);
        TableColumn<Student, String> sexCol = (TableColumn<Student, String>) studentTable.getColumns().get(3);
        TableColumn<Student, String> emailCol = (TableColumn<Student, String>) studentTable.getColumns().get(4);
        TableColumn<Student, String> idCardCol = (TableColumn<Student, String>) studentTable.getColumns().get(5);
        TableColumn<Student, String> enrollDateCol = (TableColumn<Student, String>) studentTable.getColumns().get(6);
        TableColumn<Student, String> statusCol = (TableColumn<Student, String>) studentTable.getColumns().get(7);
        TableColumn<Student, Void> actionCol = (TableColumn<Student, Void>) studentTable.getColumns().get(8);
        
        // 设置单元格值工厂
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        idCardCol.setCellValueFactory(new PropertyValueFactory<>("idCard"));
        
        // 格式化日期显示（兼容 java.sql.Date 与 java.util.Date）
        enrollDateCol.setCellValueFactory(cellData -> {
            try {
                var d = cellData.getValue().getEnrollDate();
                if (d == null) return new SimpleStringProperty("");
                if (d instanceof java.sql.Date) {
                    return new SimpleStringProperty(((java.sql.Date) d).toLocalDate()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
                // 兼容 util.Date
                return new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd").format(d));
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });
        
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // 设置操作列
        actionCol.setCellFactory(createActionCellFactory());
    }
   /* private void setupTable() {
        // 设置单元格值工厂
        TableColumn<Student, String> studentIdCol = (TableColumn<Student, String>) studentTable.getColumns().get(0);
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        
        TableColumn<Student, String> studentNameCol = (TableColumn<Student, String>) studentTable.getColumns().get(1);
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        
        TableColumn<Student, String> classNameCol = (TableColumn<Student, String>) studentTable.getColumns().get(2);
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        
        TableColumn<Student, String> sexCol = (TableColumn<Student, String>) studentTable.getColumns().get(3);
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        
        TableColumn<Student, String> emailCol = (TableColumn<Student, String>) studentTable.getColumns().get(4);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Student, String> idCardCol = (TableColumn<Student, String>) studentTable.getColumns().get(5);
        idCardCol.setCellValueFactory(new PropertyValueFactory<>("idCard"));
        
        // 格式化日期显示
        TableColumn<Student, String> enrollDateCol = (TableColumn<Student, String>) studentTable.getColumns().get(6);
        enrollDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEnrollDate() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getEnrollDate().toLocaleString().formatted(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                );
            }
            return new SimpleStringProperty("");
        });
        
        TableColumn<Student, String> statusCol = (TableColumn<Student, String>) studentTable.getColumns().get(7);
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // 设置操作列
        TableColumn<Student, Void> actionCol = (TableColumn<Student, Void>) studentTable.getColumns().get(8);
        actionCol.setCellFactory(createActionCellFactory());
    }*/
    
    /**
     * 创建操作列单元格工厂
     */
    private Callback<TableColumn<Student, Void>, TableCell<Student, Void>> createActionCellFactory() {
        return new Callback<TableColumn<Student, Void>, TableCell<Student, Void>>() {
            @Override
            public TableCell<Student, Void> call(final TableColumn<Student, Void> param) {
                return new TableCell<Student, Void>() {
                    private final Button viewBtn = new Button("详情");
                    private final Button editBtn = new Button("编辑");
                    private final Button statusBtn = new Button("状态");
                    private final Button deleteBtn = new Button("删除");
                    private final HBox pane = new HBox(5, viewBtn, editBtn, statusBtn, deleteBtn);
                    
                    {
                        viewBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 12px;");
                        editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 12px;");
                        statusBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-size: 12px;");
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");
                        
                        viewBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            onViewStudent(student);
                        });
                        
                        editBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            onEditStudent(student);
                        });
                        
                        statusBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            onChangeStatus(student);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            onDeleteStudent(student);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * 设置筛选器
     */
    private void setupFilters() {
        // 初始化状态筛选器
        statusFilter.getItems().setAll("全部", "正常", "休学", "退学", "毕业");
        statusFilter.setValue("全部");
        statusFilter.setOnAction(event -> onFilterByStatus());
        
        // 初始化班级筛选器
        classFilter.getItems().setAll("全部");
        classFilter.setValue("全部");
        classFilter.setOnAction(event -> onFilterByClass());
    }
    
    /**
     * 加载学生数据
     */
    private void loadStudentData() {
        try {
            List<Student> students = studentService.getAll();
            if (students == null) students = List.of();
            studentList.setAll(students);
            studentTable.setItems(studentList);
            updateClassFilter(students);
        } catch (Exception e) {
            showError("加载学生数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新班级筛选器
     */
    private void updateClassFilter(List<Student> students) {
        if (classFilter == null) return;
        classFilter.getItems().setAll("全部");
        students.stream().map(Student::getClassName)
                .filter(s -> s != null && !s.isBlank())
                .distinct().sorted()
                .forEach(s -> classFilter.getItems().add(s));
        if (!classFilter.getItems().isEmpty()) classFilter.setValue("全部");
    }
    
    /**
     * 筛选学生
     */
    private void filterStudents() {
        String status = statusFilter.getValue();
        String className = classFilter.getValue();
        String keyword = searchField.getText().toLowerCase();
        
        ObservableList<Student> filteredList = FXCollections.observableArrayList();
        
        for (Student student : studentList) {
            // 状态筛选
            if (!"全部".equals(status) && !status.equals(student.getStatus())) {
                continue;
            }
            
            // 班级筛选
            if (!"全部".equals(className) && !className.equals(student.getClassName())) {
                continue;
            }
            
            // 关键字搜索
            if (keyword != null && !keyword.isEmpty()) {
                boolean matches = (student.getStudentId() != null && student.getStudentId().toLowerCase().contains(keyword)) ||
                                 (student.getStudentName() != null && student.getStudentName().toLowerCase().contains(keyword)) ||
                                 (student.getClassName() != null && student.getClassName().toLowerCase().contains(keyword));
                if (!matches) {
                    continue;
                }
            }
            
            filteredList.add(student);
        }
        
        studentTable.setItems(filteredList);
    }
    
    /**
     * 查看学生详情
     */
    private void onViewStudent(Student student) {
        try {
            // 获取完整学生信息（包括tbl_user中的信息）
            Student fullStudent = studentService.getStudentFull(student.getStudentId());
            NavigationUtil.showStudentDetail(fullStudent);
        } catch (Exception e) {
            showError("获取学生详情失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 搜索按钮点击事件
     */
    @FXML
    private void onSearch() { filterStudents(); }
    
    /**
     * 刷新按钮点击事件
     */
    @FXML
    private void onRefresh() {
        loadStudentData();
        searchField.clear();
    }
    
    /**
     * 添加学生按钮点击事件
     */
    @FXML
    private void onAddStudent() {
        NavigationUtil.showDialog("student-form-view.fxml", "添加学生");
        loadStudentData(); // 刷新数据
    }
    
    /**
     * 编辑学生
     */
    private void onEditStudent(Student student) {
        NavigationUtil.showDialog("student-form-view.fxml", "编辑学生 - " + student.getStudentName());
        loadStudentData(); // 刷新数据
    }
    
    /**
     * 更改学籍状态
     */
    private void onChangeStatus(Student student) {
        if (student == null) return;
        var options = Arrays.asList("正常", "休学", "退学", "毕业");
        ChoiceDialog<String> dlg = new ChoiceDialog<>(student.getStatus(), options);
        dlg.setTitle("更改学籍状态");
        dlg.setHeaderText("学生：" + student.getStudentName() + " (" + student.getStudentId() + ")");
        dlg.setContentText("选择新的学籍状态：");
        dlg.showAndWait().ifPresent(sel -> {
            try {
                boolean ok = studentService.updateStudentStatus(student.getStudentId(), sel);
                if (ok) { showSuccess("状态已更新为：" + sel); loadStudentData(); }
                else { showError("状态更新失败"); }
            } catch (Exception e) {
                showError("状态更新失败: " + e.getMessage());
            }
        });
    }

    /**
     * 按学籍状态为表格行着色
     */
    private void setupRowStyle() {
        studentTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }
                String st = item.getStatus() == null ? "" : item.getStatus();
                // 正常/休学/退学/毕业：绿/黄/红/灰（淡色背景）
                switch (st) {
                    case "正常":
                        setStyle("-fx-background-color: #e8f5e9;");
                        break;
                    case "休学":
                        setStyle("-fx-background-color: #fff8e1;");
                        break;
                    case "退学":
                        setStyle("-fx-background-color: #ffebee;");
                        break;
                    case "毕业":
                        setStyle("-fx-background-color: #eceff1;");
                        break;
                    default:
                        setStyle("");
                }
            }
        });
    }
    
    /**
     * 删除学生
     */
    private void onDeleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确定要删除学生 " + student.getStudentName() + " 吗？");
        alert.setContentText("学号: " + student.getStudentId() + "\n此操作将同时删除用户账户！");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 使用IBaseService中的delete方法，同时删除tbl_user和tbl_student记录
                    boolean success = studentService.delete(student.getStudentId());
                    if (success) {
                        showSuccess("删除成功");
                        loadStudentData();
                    } else {
                        showError("删除失败");
                    }
                } catch (Exception e) {
                    showError("删除失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 导出数据
     */
    @FXML
    private void onExport() {
        // 实现数据导出功能
        showInformation("导出数据", "数据导出功能尚未实现");
    }
    
    /**
     * 批量操作
     */
    @FXML
    private void onBatchOperation() {
        // 实现批量操作功能
        showInformation("批量操作", "批量操作功能尚未实现");
    }
    
    /**
     * 按班级筛选
     */
    @FXML
    private void onFilterByClass() {
        String className = classFilter.getValue();
        if ("全部".equals(className)) {
            studentTable.setItems(studentList);
        } else {
            try {
                List<Student> students = studentService.getStudentsByClass(className);
                studentTable.setItems(FXCollections.observableArrayList(students));
                showSuccess("找到 " + students.size() + " 名学生");
            } catch (Exception e) {
                showError("筛选失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 按状态筛选
     */
    @FXML
    private void onFilterByStatus() {
        String status = statusFilter.getValue();
        if ("全部".equals(status)) {
            studentTable.setItems(studentList);
        } else {
            try {
                List<Student> students = studentService.getStudentsByStatus(status);
                studentTable.setItems(FXCollections.observableArrayList(students));
                showSuccess("找到 " + students.size() + " 名学生");
            } catch (Exception e) {
                showError("筛选失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}