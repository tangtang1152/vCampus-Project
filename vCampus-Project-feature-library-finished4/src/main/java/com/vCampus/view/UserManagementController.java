package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.User;
import client.net.SocketClient;
import com.vCampus.util.RBACUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class UserManagementController extends BaseController {

    @FXML private TableView<User> table;
    @FXML private TableColumn<User, Number> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRoles;
    @FXML private TableColumn<User, String> colRealName;
    @FXML private TableColumn<User, String> colTypeInfo;
    @FXML private CheckBox cbStudent;
    @FXML private CheckBox cbTeacher;
    @FXML private CheckBox cbAdmin;
    @FXML private TextField keywordField;
    @FXML private Label lblCount;

    private final ObservableList<User> data = FXCollections.observableArrayList();
    // 移除直接依赖服务，改为通过 SocketClient 通信

    // 预计算缓存，避免每个单元格反复触发数据库事务
    private java.util.Map<Integer, String> realNameByUserId = new java.util.HashMap<>();
    private java.util.Map<Integer, String> typeInfoByUserId = new java.util.HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!RBACUtil.currentUserCan(RBACUtil::canManageUsers)) {
            showError("需要管理员权限");
            return;
        }

        colUserId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getUserId()));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colRoles.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRoleSet().stream().map(String::toUpperCase).collect(java.util.stream.Collectors.joining(","))
        ));
        colRealName.setCellValueFactory(c -> new SimpleStringProperty(
            realNameByUserId.getOrDefault(c.getValue().getUserId(), "")));
        colTypeInfo.setCellValueFactory(c -> new SimpleStringProperty(
            typeInfoByUserId.getOrDefault(c.getValue().getUserId(), "")));

        table.setItems(data);
        refresh();
    }

    private String resolveRealName(User u) {
        return realNameByUserId.getOrDefault(u.getUserId(), "");
    }

    private String resolveTypeInfo(User u) {
        return typeInfoByUserId.getOrDefault(u.getUserId(), "");
    }

    @FXML private void onSearch() { refresh(); }
    @FXML private void onRefresh() { refresh(); }
    @FXML private void onClose() { ((Stage) table.getScene().getWindow()).close(); }

    private void refresh() {
        List<User> all = new java.util.ArrayList<>();
        try {
            String response = SocketClient.sendRequest("GET_ALL_USERS");
            if (response != null && response.startsWith("SUCCESS:USERS:")) {
                all = parseUsersFromResponse(response);
            }
        } catch (Exception e) {
            showError("获取用户列表失败: " + e.getMessage());
        }

        // 一次性加载三类详情，构建缓存，避免单元格重复触发数据库
        realNameByUserId.clear();
        typeInfoByUserId.clear();
        try {
            // 通过 SocketClient 获取学生信息
            String studentResponse = SocketClient.sendRequest("GET_ALL_STUDENTS");
            if (studentResponse != null && studentResponse.startsWith("SUCCESS:STUDENTS:")) {
                List<Student> studs = parseStudentsFromResponse(studentResponse);
                for (Student s : studs) {
                    realNameByUserId.put(s.getUserId(), s.getStudentName() == null ? "" : s.getStudentName());
                    typeInfoByUserId.put(s.getUserId(), "班级:" + (s.getClassName() == null ? "" : s.getClassName()));
                }
            }
        } catch (Exception ignored) {}
        try {
            // 通过 SocketClient 获取教师信息
            String teacherResponse = SocketClient.sendRequest("GET_ALL_TEACHERS");
            if (teacherResponse != null && teacherResponse.startsWith("SUCCESS:TEACHERS:")) {
                List<Teacher> tchs = parseTeachersFromResponse(teacherResponse);
                for (Teacher t : tchs) {
                    realNameByUserId.put(t.getUserId(), t.getTeacherName() == null ? "" : t.getTeacherName());
                    typeInfoByUserId.put(t.getUserId(), "部门:" + (t.getDepartmentId() == null ? "" : t.getDepartmentId()));
                }
            }
        } catch (Exception ignored) {}
        try {
            // 通过 SocketClient 获取管理员信息
            String adminResponse = SocketClient.sendRequest("GET_ALL_ADMINS");
            if (adminResponse != null && adminResponse.startsWith("SUCCESS:ADMINS:")) {
                List<Admin> adms = parseAdminsFromResponse(adminResponse);
                for (Admin a : adms) {
                    realNameByUserId.put(a.getUserId(), a.getAdminName() == null ? "" : a.getAdminName());
                    typeInfoByUserId.put(a.getUserId(), "工号:" + (a.getAdminId() == null ? "" : a.getAdminId()));
                }
            }
        } catch (Exception ignored) {}
        String kw = keywordField == null ? "" : keywordField.getText().trim().toLowerCase();
        boolean fStu = cbStudent == null || cbStudent.isSelected();
        boolean fTch = cbTeacher == null || cbTeacher.isSelected();
        boolean fAdm = cbAdmin == null || cbAdmin.isSelected();

        List<User> filtered = all.stream().filter(u -> {
            Set<String> rs = u.getRoleSet().stream().map(String::toUpperCase).collect(Collectors.toSet());
            boolean roleOk = (fStu && rs.contains("STUDENT")) || (fTch && rs.contains("TEACHER")) || (fAdm && rs.contains("ADMIN"));
            if (!roleOk) return false;
            if (kw.isEmpty()) return true;
            String uname = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
            String idStr = String.valueOf(u.getUserId());
            String realName = realNameByUserId.getOrDefault(u.getUserId(), "").toLowerCase();
            return uname.contains(kw) || idStr.contains(kw) || realName.contains(kw);
        }).collect(Collectors.toList());

        data.setAll(filtered);
        if (lblCount != null) lblCount.setText(String.valueOf(filtered.size()));
    }

    @FXML private void onAdd() { openUserForm(null); }
    @FXML private void onEdit() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要编辑的用户"); return; }
        openUserForm(sel);
    }
    @FXML private void onDelete() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarning("请选择要删除的用户"); return; }
        if (!showConfirmation("删除用户", "确定删除用户 " + sel.getUsername() + " ? 此操作不可恢复")) return;
        // 按角色删除对应记录并级联删除 tbl_user
        boolean ok = false;
        Set<String> rs = sel.getRoleSet().stream().map(String::toUpperCase).collect(java.util.stream.Collectors.toSet());
        try {
            if (rs.contains("STUDENT")) {
                // 通过 SocketClient 删除学生
                String response = SocketClient.sendRequest("DELETE_STUDENT:" + sel.getUserId());
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            } else if (rs.contains("TEACHER")) {
                // 通过 SocketClient 删除教师
                String response = SocketClient.sendRequest("DELETE_TEACHER:" + sel.getUserId());
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            } else if (rs.contains("ADMIN")) {
                // 通过 SocketClient 删除管理员
                String response = SocketClient.sendRequest("DELETE_ADMIN:" + sel.getUserId());
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            }
        } catch (Exception e) {
            showError("删除失败：可能存在外键引用，请先清理相关数据\n" + e.getMessage());
            ok = false;
        }
        if (ok) { showInformation("提示", "删除成功"); refresh(); } else { showError("删除失败，可能关联信息不存在"); }
    }

    private void openUserForm(User originUser) {
        boolean isEdit = originUser != null;
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? ("编辑用户 - " + originUser.getUsername()) : "新增用户");

        // 竖版表单容器
        var form = new VBox(10);
        form.setPadding(new Insets(16));
        form.setFillWidth(true);

        // 小工具：生成"标签在上，控件在下"的竖版字段块
        java.util.function.BiFunction<String, Node, VBox> makeField = (text, node) -> {
            Label lbl = new Label(text);
            var box = new VBox(4, lbl, node);
            box.setFillWidth(true);
            if (node instanceof Control) {
                ((Control) node).setMaxWidth(Double.MAX_VALUE);
            }
            return box;
        };

        // 通用字段
        TextField tfUsername = new TextField(isEdit ? originUser.getUsername() : "");
        PasswordField pfPassword = new PasswordField();
        if (isEdit) pfPassword.setPromptText("留空则不修改");

        // 角色单选
        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton rbStu = new RadioButton("学生"); rbStu.setToggleGroup(roleGroup);
        RadioButton rbTch = new RadioButton("教师"); rbTch.setToggleGroup(roleGroup);
        RadioButton rbAdm = new RadioButton("管理员"); rbAdm.setToggleGroup(roleGroup);
        var roleRow = new HBox(12, rbStu, rbTch, rbAdm);
        roleRow.setAlignment(Pos.CENTER_LEFT);

        String existingRole = isEdit ? originUser.getRoleSet().stream().findFirst().orElse("") : "";
        if (existingRole.equalsIgnoreCase("STUDENT")) rbStu.setSelected(true);
        else if (existingRole.equalsIgnoreCase("TEACHER")) rbTch.setSelected(true);
        else if (existingRole.equalsIgnoreCase("ADMIN")) rbAdm.setSelected(true);
        else rbStu.setSelected(!isEdit); // 新增默认学生

        // 学生字段
        TextField tfStuId = new TextField(); tfStuId.setPromptText("学号");
        TextField tfStuName = new TextField(); tfStuName.setPromptText("姓名");
        TextField tfClass = new TextField(); tfClass.setPromptText("班级");

        // 教师字段
        TextField tfTchId = new TextField(); tfTchId.setPromptText("教师编号");
        TextField tfTchName = new TextField(); tfTchName.setPromptText("姓名");
        ComboBox<String> cbSex = new ComboBox<>(FXCollections.observableArrayList("男","女"));
        cbSex.setValue("男");
        TextField tfTech = new TextField(); tfTech.setPromptText("职称");
        TextField tfDept = new TextField(); tfDept.setPromptText("部门ID");

        // 管理员字段
        TextField tfAdmId = new TextField(); tfAdmId.setPromptText("管理员工号");
        TextField tfAdmName = new TextField(); tfAdmName.setPromptText("姓名");

        // 分组（竖版）：学生/教师/管理员
        var groupStu = new VBox(8,
            makeField.apply("学号", tfStuId),
            makeField.apply("学生姓名", tfStuName),
            makeField.apply("班级", tfClass)
        );
        var groupTch = new VBox(8,
            makeField.apply("教师编号", tfTchId),
            makeField.apply("教师姓名", tfTchName),
            makeField.apply("性别", cbSex),
            makeField.apply("职称", tfTech),
            makeField.apply("部门ID", tfDept)
        );
        var groupAdm = new VBox(8,
            makeField.apply("管理员工号", tfAdmId),
            makeField.apply("管理员姓名", tfAdmName)
        );

        // 若编辑，回填明细
        if (isEdit) {
            Set<String> rs = originUser.getRoleSet();
            if (rs.contains("STUDENT")) {
                try {
                    String response = SocketClient.sendRequest("GET_STUDENT_BY_USER_ID:" + originUser.getUserId());
                    if (response != null && response.startsWith("SUCCESS:STUDENT:")) {
                        Student s = parseStudentFromResponse(response);
                        if (s != null) {
                            tfStuId.setText(s.getStudentId()); tfStuId.setEditable(false);
                            tfStuName.setText(s.getStudentName()); tfClass.setText(s.getClassName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取学生信息失败: " + e.getMessage());
                }
                rbStu.setSelected(true);
            } else if (rs.contains("TEACHER")) {
                try {
                    String response = SocketClient.sendRequest("GET_TEACHER_BY_USER_ID:" + originUser.getUserId());
                    if (response != null && response.startsWith("SUCCESS:TEACHER:")) {
                        Teacher t = parseTeacherFromResponse(response);
                        if (t != null) {
                            tfTchId.setText(t.getTeacherId()); tfTchId.setEditable(false);
                            tfTchName.setText(t.getTeacherName()); cbSex.setValue(t.getSex());
                            tfTech.setText(t.getTechnical()); tfDept.setText(t.getDepartmentId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取教师信息失败: " + e.getMessage());
                }
                rbTch.setSelected(true);
            } else if (rs.contains("ADMIN")) {
                try {
                    String response = SocketClient.sendRequest("GET_ADMIN_BY_USER_ID:" + originUser.getUserId());
                    if (response != null && response.startsWith("SUCCESS:ADMIN:")) {
                        Admin a = parseAdminFromResponse(response);
                        if (a != null) {
                            tfAdmId.setText(a.getAdminId()); tfAdmId.setEditable(false);
                            tfAdmName.setText(a.getAdminName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取管理员信息失败: " + e.getMessage());
                }
                rbAdm.setSelected(true);
            }
        }

        // 组装表单（竖版）
        form.getChildren().addAll(
            makeField.apply("用户名", tfUsername),
            makeField.apply("密码", pfPassword),
            makeField.apply("角色", roleRow),
            groupStu,
            groupTch,
            groupAdm
        );

        // 角色切换联动（可见性 + 调试输出）
        java.util.function.Consumer<String> applyRoleVisibility = role -> {
            boolean s = "STUDENT".equalsIgnoreCase(role);
            boolean t = "TEACHER".equalsIgnoreCase(role);
            boolean a = "ADMIN".equalsIgnoreCase(role);
            groupStu.setManaged(s); groupStu.setVisible(s);
            groupTch.setManaged(t); groupTch.setVisible(t);
            groupAdm.setManaged(a); groupAdm.setVisible(a);
            System.out.println("[UserMgmt] 角色区域切换 => " + role);
        };
        String initRole = rbStu.isSelected()?"STUDENT":rbTch.isSelected()?"TEACHER":"ADMIN";
        applyRoleVisibility.accept(initRole);
        rbStu.setOnAction(e -> applyRoleVisibility.accept("STUDENT"));
        rbTch.setOnAction(e -> applyRoleVisibility.accept("TEACHER"));
        rbAdm.setOnAction(e -> applyRoleVisibility.accept("ADMIN"));

        // 放入可滚动容器并允许调整大小
        var sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(420);

        dlg.setResizable(true);
        dlg.getDialogPane().setPrefSize(520, 560);
        dlg.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dlg.getDialogPane().setContent(sp);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText();
            boolean chooseStu = rbStu.isSelected();
            boolean chooseTch = rbTch.isSelected();
            boolean chooseAdm = rbAdm.isSelected();
            if (username.isEmpty()) { showError("用户名不能为空"); return; }

            if (!isEdit) {
                // 新增
                if (chooseStu) {
                    if (tfStuId.getText().trim().isEmpty() || tfStuName.getText().trim().isEmpty()) { showError("请填写学号与姓名"); return; }
                    Student s = new Student();
                    s.setUsername(username); s.setPassword(password.isEmpty()?"123456":password);
                    s.setRole("student");
                    s.setStudentId(tfStuId.getText().trim());
                    s.setStudentName(tfStuName.getText().trim());
                    s.setClassName(tfClass.getText().trim());
                    try {
                        String response = SocketClient.sendRequest("REGISTER:" + s.getUsername() + ":" + s.getPassword() + ":" + s.getRole() + ":" + s.getStudentId() + ":" + s.getStudentName() + ":" + s.getClassName() + ":" + s.getSex() + ":" + s.getEmail() + ":" + s.getIdCard() + ":" + s.getStatus());
                        if (response == null || !response.startsWith("SUCCESS:REGISTER:")) { showError("新增学生失败: " + response); return; }
                    } catch (Exception e) {
                        showError("新增学生失败: " + e.getMessage());
                        return;
                    }
                } else if (chooseTch) {
                    if (tfTchId.getText().trim().isEmpty() || tfTchName.getText().trim().isEmpty()) { showError("请填写教师编号与姓名"); return; }
                    Teacher t = new Teacher();
                    t.setUsername(username); t.setPassword(password.isEmpty()?"123456":password);
                    t.setRole("teacher");
                    t.setTeacherId(tfTchId.getText().trim());
                    t.setTeacherName(tfTchName.getText().trim());
                    t.setSex(cbSex.getValue());
                    t.setTechnical(tfTech.getText().trim());
                    t.setDepartmentId(tfDept.getText().trim());
                    try {
                        String response = SocketClient.sendRequest("REGISTER:" + t.getUsername() + ":" + t.getPassword() + ":" + t.getRole() + ":" + t.getTeacherId() + ":" + t.getTeacherName() + ":" + t.getSex() + ":" + t.getTechnical() + ":" + t.getDepartmentId());
                        if (response == null || !response.startsWith("SUCCESS:REGISTER:")) { showError("新增教师失败: " + response); return; }
                    } catch (Exception e) {
                        showError("新增教师失败: " + e.getMessage());
                        return;
                    }
                } else if (chooseAdm) {
                    if (tfAdmId.getText().trim().isEmpty() || tfAdmName.getText().trim().isEmpty()) { showError("请填写管理员工号与姓名"); return; }
                    Admin a = new Admin();
                    a.setUsername(username); a.setPassword(password.isEmpty()?"123456":password);
                    a.setRole("admin");
                    a.setAdminId(tfAdmId.getText().trim());
                    a.setAdminName(tfAdmName.getText().trim());
                    try {
                        String response = SocketClient.sendRequest("REGISTER:" + a.getUsername() + ":" + a.getPassword() + ":" + a.getRole() + ":" + a.getAdminId() + ":" + a.getAdminName());
                        if (response == null || !response.startsWith("SUCCESS:REGISTER:")) { showError("新增管理员失败: " + response); return; }
                    } catch (Exception e) {
                        showError("新增管理员失败: " + e.getMessage());
                        return;
                    }
                }
                showInformation("提示", "新增成功"); refresh();
                return;
            }

            // 编辑：允许切换角色并迁移明细
            // 更新用户核心信息（若密码留空则不改）
            originUser.setUsername(username);
            if (!password.isEmpty()) originUser.setPassword(password);

            String targetRole = (rbStu.isSelected()?"student":rbTch.isSelected()?"teacher":"admin");
            String oldRole = (existingRole == null?"":existingRole).toLowerCase();
            originUser.setRole(targetRole);
            
            // 通过 SocketClient 更新用户信息
            boolean okUser = false;
            try {
                String response = SocketClient.sendRequest("UPDATE_USER:" + originUser.getUserId() + ":" + originUser.getUsername() + ":" + originUser.getPassword() + ":" + originUser.getRole());
                okUser = response != null && response.startsWith("SUCCESS:UPDATE:");
            } catch (Exception e) {
                System.err.println("更新用户信息失败: " + e.getMessage());
            }

            // 通过 SocketClient 更新角色详细信息
            boolean okRole = false;
            try {
                if (rbStu.isSelected()) {
                    String response = SocketClient.sendRequest("UPDATE_STUDENT:" + originUser.getUserId() + ":" + tfStuName.getText().trim() + ":" + tfClass.getText().trim() + ":" + "男" + ":" + "test@example.com" + ":" + "123456" + ":" + "正常");
                    okRole = response != null && response.startsWith("SUCCESS:UPDATE:");
                } else if (rbTch.isSelected()) {
                    String response = SocketClient.sendRequest("UPDATE_TEACHER:" + originUser.getUserId() + ":" + tfTchName.getText().trim() + ":" + cbSex.getValue() + ":" + tfTech.getText().trim() + ":" + tfDept.getText().trim());
                    okRole = response != null && response.startsWith("SUCCESS:UPDATE:");
                } else {
                    String response = SocketClient.sendRequest("UPDATE_ADMIN:" + originUser.getUserId() + ":" + tfAdmName.getText().trim());
                    okRole = response != null && response.startsWith("SUCCESS:UPDATE:");
                }
            } catch (Exception ex) {
                System.err.println("[UserMgmt] 编辑保存失败: " + ex.getMessage());
                okRole = false;
            }
            if (okUser && okRole) { showInformation("提示", "保存成功"); refresh(); } else { showError("保存失败"); }
        });
    }
    @FXML private void onImport() { showInformation("导入", "占位：实现Excel/CSV导入"); }
    @FXML private void onExport() { showInformation("导出", "占位：实现Excel/CSV导出"); }

    /**
     * 从服务器响应解析用户列表
     */
    private List<User> parseUsersFromResponse(String response) {
        List<User> users = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:USERS:")) {
                String data = response.substring("SUCCESS:USERS:".length());
                if (!data.isEmpty()) {
                    String[] userStrings = data.split("\\|");
                    for (String userString : userStrings) {
                        String[] fields = userString.split(",");
                        if (fields.length >= 3) {
                            User user = new User();
                            user.setUserId(Integer.parseInt(fields[0]));
                            user.setUsername(fields[1]);
                            user.setRole(fields[2]);
                            users.add(user);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析用户数据异常: " + e.getMessage());
        }
        return users;
    }

    /**
     * 从服务器响应解析学生列表
     */
    private List<Student> parseStudentsFromResponse(String response) {
        List<Student> students = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:STUDENTS:")) {
                String data = response.substring("SUCCESS:STUDENTS:".length());
                if (!data.isEmpty()) {
                    String[] studentStrings = data.split("\\|");
                    for (String studentString : studentStrings) {
                        String[] fields = studentString.split(",");
                        if (fields.length >= 6) {
                            Student student = new Student();
                            student.setStudentId(fields[0]);
                            student.setStudentName(fields[1]);
                            student.setUserId(Integer.parseInt(fields[2]));
                            student.setClassName(fields[3]);
                            student.setSex(fields[4]);
                            student.setEmail(fields[5]);
                            students.add(student);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析学生数据异常: " + e.getMessage());
        }
        return students;
    }

    /**
     * 从服务器响应解析教师列表
     */
    private List<Teacher> parseTeachersFromResponse(String response) {
        List<Teacher> teachers = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:TEACHERS:")) {
                String data = response.substring("SUCCESS:TEACHERS:".length());
                if (!data.isEmpty()) {
                    String[] teacherStrings = data.split("\\|");
                    for (String teacherString : teacherStrings) {
                        String[] fields = teacherString.split(",");
                        if (fields.length >= 6) {
                            Teacher teacher = new Teacher();
                            teacher.setTeacherId(fields[0]);
                            teacher.setTeacherName(fields[1]);
                            teacher.setUserId(Integer.parseInt(fields[2]));
                            teacher.setSex(fields[3]);
                            teacher.setTechnical(fields[4]);
                            teacher.setDepartmentId(fields[5]);
                            teachers.add(teacher);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析教师数据异常: " + e.getMessage());
        }
        return teachers;
    }

    /**
     * 从服务器响应解析管理员列表
     */
    private List<Admin> parseAdminsFromResponse(String response) {
        List<Admin> admins = new java.util.ArrayList<>();
        try {
            if (response != null && response.startsWith("SUCCESS:ADMINS:")) {
                String data = response.substring("SUCCESS:ADMINS:".length());
                if (!data.isEmpty()) {
                    String[] adminStrings = data.split("\\|");
                    for (String adminString : adminStrings) {
                        String[] fields = adminString.split(",");
                        if (fields.length >= 3) {
                            Admin admin = new Admin();
                            admin.setAdminId(fields[0]);
                            admin.setAdminName(fields[1]);
                            admin.setUserId(Integer.parseInt(fields[2]));
                            admins.add(admin);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析管理员数据异常: " + e.getMessage());
        }
        return admins;
    }

    /**
     * 从服务器响应解析单个学生信息
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
     * 从服务器响应解析单个教师信息
     */
    private Teacher parseTeacherFromResponse(String response) {
        try {
            if (response != null && response.startsWith("SUCCESS:TEACHER:")) {
                String data = response.substring("SUCCESS:TEACHER:".length());
                String[] fields = data.split(",");
                if (fields.length >= 6) {
                    Teacher teacher = new Teacher();
                    teacher.setTeacherId(fields[0]);
                    teacher.setTeacherName(fields[1]);
                    teacher.setUserId(Integer.parseInt(fields[2]));
                    teacher.setSex(fields[3]);
                    teacher.setTechnical(fields[4]);
                    teacher.setDepartmentId(fields[5]);
                    return teacher;
                }
            }
        } catch (Exception e) {
            System.err.println("解析教师数据异常: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从服务器响应解析单个管理员信息
     */
    private Admin parseAdminFromResponse(String response) {
        try {
            if (response != null && response.startsWith("SUCCESS:ADMIN:")) {
                String data = response.substring("SUCCESS:ADMIN:".length());
                String[] fields = data.split(",");
                if (fields.length >= 3) {
                    Admin admin = new Admin();
                    admin.setAdminId(fields[0]);
                    admin.setAdminName(fields[1]);
                    admin.setUserId(Integer.parseInt(fields[2]));
                    return admin;
                }
            }
        } catch (Exception e) {
            System.err.println("解析管理员数据异常: " + e.getMessage());
        }
        return null;
    }
}
