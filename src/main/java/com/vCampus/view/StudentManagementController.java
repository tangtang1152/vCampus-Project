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

import java.net.URL; // 导入 URL
import java.text.SimpleDateFormat; // 导入 SimpleDateFormat
import java.util.List;
import java.util.ResourceBundle; // 导入 ResourceBundle

/**
 * 学籍管理控制器
 */
public class StudentManagementController extends BaseController {

    @FXML private TableView<Student> studentTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> classFilter;

    // 修复：使用 fx:id 绑定 TableColumn，避免通过索引获取
    @FXML private TableColumn<Student, String> studentIdCol;
    @FXML private TableColumn<Student, String> studentNameCol;
    @FXML private TableColumn<Student, String> classNameCol;
    @FXML private TableColumn<Student, String> sexCol;
    @FXML private TableColumn<Student, String> emailCol;
    @FXML private TableColumn<Student, String> idCardCol;
    @FXML private TableColumn<Student, String> enrollDateCol;
    @FXML private TableColumn<Student, String> statusCol;
    @FXML private TableColumn<Student, Void> actionCol;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private IStudentService studentService = ServiceFactory.getStudentService();

    /**
     * 初始化方法
     */
    @FXML
    @Override // 标记为重写父类方法
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadStudentData();
    }

    /**
     * 设置表格
     */
    private void setupTable() {
        // 修复：直接使用 @FXML 绑定的列
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        idCardCol.setCellValueFactory(new PropertyValueFactory<>("idCard"));

        // 修复：使用 SimpleDateFormat 格式化日期
        enrollDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEnrollDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return new SimpleStringProperty(sdf.format(cellData.getValue().getEnrollDate()));
            }
            return new SimpleStringProperty("");
        });

        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 设置操作列
        actionCol.setCellFactory(createActionCellFactory());
    }

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
        statusFilter.getItems().addAll("全部", "正常", "休学", "退学", "毕业");
        statusFilter.setValue("全部");
        statusFilter.setOnAction(event -> filterStudents());

        // 初始化班级筛选器
        classFilter.getItems().add("全部");
        classFilter.setValue("全部");
        classFilter.setOnAction(event -> filterStudents());
    }

    /**
     * 加载学生数据
     */
    private void loadStudentData() {
        try {
            List<Student> students = studentService.getAll();
            studentList.setAll(students);
            studentTable.setItems(studentList);

            // 更新班级筛选器
            updateClassFilter(students);

            showSuccess("数据加载成功，共 " + students.size() + " 条记录");
        } catch (Exception e) {
            showError("加载学生数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新班级筛选器
     */
    private void updateClassFilter(List<Student> students) {
        classFilter.getItems().clear();
        classFilter.getItems().add("全部");

        students.stream()
                .map(Student::getClassName)
                .distinct()
                .sorted()
                .forEach(classFilter.getItems()::add);

        classFilter.setValue("全部");
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
    private void onSearch() {
        filterStudents();
    }

    /**
     * 刷新按钮点击事件
     */
    @FXML
    private void onRefresh() {
        loadStudentData();
        searchField.clear();
        statusFilter.setValue("全部");
        classFilter.setValue("全部");
    }

    /**
     * 添加学生按钮点击事件
     */
    @FXML
    private void onAddStudent() {
        // 修复：调用 NavigationUtil 中新增的方法，传递 null 表示新增
        NavigationUtil.showStudentFormDialog(null, "添加学生");
        loadStudentData(); // 刷新数据
    }

    /**
     * 编辑学生
     */
    private void onEditStudent(Student student) {
        // 修复：调用 NavigationUtil 中新增的方法，传递要编辑的学生对象
        NavigationUtil.showStudentFormDialog(student, "编辑学生 - " + student.getStudentName());
        loadStudentData(); // 刷新数据
    }

    /**
     * 更改学籍状态
     */
    private void onChangeStatus(Student student) {
        // 修复：调用 NavigationUtil 中新增的方法，传递学生对象
        NavigationUtil.showStatusChangeDialog(student, "更改学籍状态 - " + student.getStudentName());
        loadStudentData(); // 刷新数据
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
        filterStudents(); // 直接调用 filterStudents 统一处理
    }

    /**
     * 按状态筛选
     */
    @FXML
    private void onFilterByStatus() {
        filterStudents(); // 直接调用 filterStudents 统一处理
    }
}