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

    /**
     * 新建用户后立即更新缓存，避免等待refresh()
     */
    private void updateUserCacheAfterCreate(boolean isStudent, boolean isTeacher, boolean isAdmin,
                                          String studentName, String teacherName, String adminName,
                                          String className, String departmentId, String adminId,
                                          int userId) {
        if (isStudent && !studentName.isEmpty()) {
            realNameByUserId.put(userId, studentName);
            typeInfoByUserId.put(userId, "班级:" + className);
            System.out.println("[UserMgmt] 立即更新学生缓存: userId=" + userId + ", name=" + studentName + ", class=" + className);
        } else if (isTeacher && !teacherName.isEmpty()) {
            realNameByUserId.put(userId, teacherName);
            typeInfoByUserId.put(userId, "部门:" + departmentId);
            System.out.println("[UserMgmt] 立即更新教师缓存: userId=" + userId + ", name=" + teacherName + ", dept=" + departmentId);
        } else if (isAdmin && !adminName.isEmpty()) {
            realNameByUserId.put(userId, adminName);
            typeInfoByUserId.put(userId, "工号:" + adminId);
            System.out.println("[UserMgmt] 立即更新管理员缓存: userId=" + userId + ", name=" + adminName + ", id=" + adminId);
        }
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
                System.out.println("[UserMgmt] 成功获取用户列表，共 " + all.size() + " 个用户");
            } else {
                System.err.println("[UserMgmt] 获取用户列表失败，响应: " + response);
                showError("获取用户列表失败: " + response);
                return;
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 获取用户列表异常: " + e.getMessage());
            e.printStackTrace();
            showError("获取用户列表失败: " + e.getMessage());
            return;
        }

        // 一次性加载三类详情，构建缓存，避免单元格重复触发数据库
        // 注意：不清空缓存，保留可能已经更新的数据
        // realNameByUserId.clear();
        // typeInfoByUserId.clear();
        
        // 为所有用户初始化空的姓名和类型信息，确保新增用户也能显示
        for (User user : all) {
            // 只有当缓存中没有该用户信息时才初始化为空
            if (!realNameByUserId.containsKey(user.getUserId())) {
                realNameByUserId.put(user.getUserId(), "");
            }
            if (!typeInfoByUserId.containsKey(user.getUserId())) {
                typeInfoByUserId.put(user.getUserId(), "");
            }
        }
        System.out.println("[UserMgmt] 初始化用户缓存，共 " + all.size() + " 个用户，缓存大小: " + realNameByUserId.size());
        
        // 获取学生信息
        try {
            String studentResponse = SocketClient.sendRequest("GET_ALL_STUDENTS");
            if (studentResponse != null && studentResponse.startsWith("SUCCESS:STUDENTS:")) {
                List<Student> studs = parseStudentsFromResponse(studentResponse);
                System.out.println("[UserMgmt] 成功获取学生信息，共 " + studs.size() + " 个学生");
                for (Student s : studs) {
                    String studentName = s.getStudentName() == null ? "" : s.getStudentName();
                    String className = s.getClassName() == null ? "" : s.getClassName();
                    realNameByUserId.put(s.getUserId(), studentName);
                    typeInfoByUserId.put(s.getUserId(), "班级:" + className);
                    System.out.println("[UserMgmt] 更新学生缓存: userId=" + s.getUserId() + ", name=" + studentName + ", class=" + className);
                }
            } else {
                System.err.println("[UserMgmt] 获取学生信息失败，响应: " + studentResponse);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 获取学生信息异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 获取教师信息
        try {
            String teacherResponse = SocketClient.sendRequest("GET_ALL_TEACHERS");
            if (teacherResponse != null && teacherResponse.startsWith("SUCCESS:TEACHERS:")) {
                List<Teacher> tchs = parseTeachersFromResponse(teacherResponse);
                System.out.println("[UserMgmt] 成功获取教师信息，共 " + tchs.size() + " 个教师");
                for (Teacher t : tchs) {
                    realNameByUserId.put(t.getUserId(), t.getTeacherName() == null ? "" : t.getTeacherName());
                    typeInfoByUserId.put(t.getUserId(), "部门:" + (t.getDepartmentId() == null ? "" : t.getDepartmentId()));
                }
            } else {
                System.err.println("[UserMgmt] 获取教师信息失败，响应: " + teacherResponse);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 获取教师信息异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 获取管理员信息
        try {
            String adminResponse = SocketClient.sendRequest("GET_ALL_ADMINS");
            if (adminResponse != null && adminResponse.startsWith("SUCCESS:ADMINS:")) {
                List<Admin> adms = parseAdminsFromResponse(adminResponse);
                System.out.println("[UserMgmt] 成功获取管理员信息，共 " + adms.size() + " 个管理员");
                for (Admin a : adms) {
                    realNameByUserId.put(a.getUserId(), a.getAdminName() == null ? "" : a.getAdminName());
                    typeInfoByUserId.put(a.getUserId(), "工号:" + (a.getAdminId() == null ? "" : a.getAdminId()));
                }
            } else {
                System.err.println("[UserMgmt] 获取管理员信息失败，响应: " + adminResponse);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 获取管理员信息异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 应用过滤条件
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
        
        // 重新绑定表格列，确保显示最新的缓存数据
        colRealName.setCellValueFactory(c -> {
            String name = realNameByUserId.getOrDefault(c.getValue().getUserId(), "");
            System.out.println("[UserMgmt] 绑定姓名列: userId=" + c.getValue().getUserId() + ", name=" + name);
            return new SimpleStringProperty(name);
        });
        colTypeInfo.setCellValueFactory(c -> {
            String typeInfo = typeInfoByUserId.getOrDefault(c.getValue().getUserId(), "");
            System.out.println("[UserMgmt] 绑定类型列: userId=" + c.getValue().getUserId() + ", typeInfo=" + typeInfo);
            return new SimpleStringProperty(typeInfo);
        });
        
        System.out.println("[UserMgmt] 数据刷新完成，显示 " + filtered.size() + " 个用户");
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
        String errorMessage = "";
        Set<String> rs = sel.getRoleSet().stream().map(String::toUpperCase).collect(java.util.stream.Collectors.toSet());
        
        try {
            String response = null;
            if (rs.contains("STUDENT")) {
                // 通过 SocketClient 删除学生
                System.out.println("[UserMgmt] 尝试删除学生，用户ID: " + sel.getUserId());
                response = SocketClient.sendRequest("DELETE_STUDENT:" + sel.getUserId());
                System.out.println("[UserMgmt] 删除学生响应: " + response);
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            } else if (rs.contains("TEACHER")) {
                // 通过 SocketClient 删除教师
                System.out.println("[UserMgmt] 尝试删除教师，用户ID: " + sel.getUserId());
                response = SocketClient.sendRequest("DELETE_TEACHER:" + sel.getUserId());
                System.out.println("[UserMgmt] 删除教师响应: " + response);
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            } else if (rs.contains("ADMIN")) {
                // 通过 SocketClient 删除管理员
                System.out.println("[UserMgmt] 尝试删除管理员，用户ID: " + sel.getUserId());
                response = SocketClient.sendRequest("DELETE_ADMIN:" + sel.getUserId());
                System.out.println("[UserMgmt] 删除管理员响应: " + response);
                ok = response != null && response.startsWith("SUCCESS:DELETE:");
            }
            
            if (!ok && response != null) {
                errorMessage = response;
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 删除用户异常: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "删除失败：可能存在外键引用，请先清理相关数据\n" + e.getMessage();
            ok = false;
        }
        
        if (ok) { 
            showInformation("提示", "删除成功"); 
            refresh(); 
        } else { 
            showError("删除失败: " + errorMessage); 
        }
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
                    // 设置默认值
                    s.setSex("男");
                    s.setEmail("test@example.com");
                    s.setIdCard("123456789012345678");
                    s.setStatus("正常");
                    try {
                        String response = SocketClient.sendRequest("REGISTER:" + s.getUsername() + ":" + s.getPassword() + ":" + s.getRole() + ":" + s.getStudentId() + ":" + s.getStudentName() + ":" + s.getClassName() + ":" + s.getSex() + ":" + s.getEmail() + ":" + s.getIdCard() + ":" + s.getStatus());
                        if (response == null || !response.startsWith("SUCCESS:REGISTER:")) { showError("新增学生失败: " + response); return; }
                        
                        // 解析注册响应，获取新创建用户的userId
                        System.out.println("[UserMgmt] 注册响应: " + response);
                        String[] responseParts = response.split(":");
                        System.out.println("[UserMgmt] 响应分割后长度: " + responseParts.length);
                        if (responseParts.length >= 4) {
                            try {
                                int newUserId = Integer.parseInt(responseParts[3]);
                                System.out.println("[UserMgmt] 解析到新用户ID: " + newUserId);
                                // 立即更新缓存，避免等待refresh()
                                updateUserCacheAfterCreate(true, false, false, 
                                    tfStuName.getText().trim(), "", "",
                                    tfClass.getText().trim(), "", "",
                                    newUserId);
                                System.out.println("[UserMgmt] 缓存更新完成，当前缓存大小: " + realNameByUserId.size());
                            } catch (NumberFormatException e) {
                                System.err.println("[UserMgmt] 解析新用户ID失败: " + e.getMessage());
                            }
                        } else {
                            System.err.println("[UserMgmt] 响应格式不正确，期望至少4个部分，实际: " + responseParts.length);
                        }
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
                        
                        // 解析注册响应，获取新创建用户的userId
                        String[] responseParts = response.split(":");
                        if (responseParts.length >= 4) {
                            try {
                                int newUserId = Integer.parseInt(responseParts[3]);
                                // 立即更新缓存，避免等待refresh()
                                updateUserCacheAfterCreate(false, true, false, 
                                    "", tfTchName.getText().trim(), "",
                                    "", tfDept.getText().trim(), "",
                                    newUserId);
                            } catch (NumberFormatException e) {
                                System.err.println("[UserMgmt] 解析新用户ID失败: " + e.getMessage());
                            }
                        }
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
                        
                        // 解析注册响应，获取新创建用户的userId
                        String[] responseParts = response.split(":");
                        if (responseParts.length >= 4) {
                            try {
                                int newUserId = Integer.parseInt(responseParts[3]);
                                // 立即更新缓存，避免等待refresh()
                                updateUserCacheAfterCreate(false, false, true, 
                                    "", "", tfAdmName.getText().trim(),
                                    "", "", tfAdmId.getText().trim(),
                                    newUserId);
                            } catch (NumberFormatException e) {
                                System.err.println("[UserMgmt] 解析新用户ID失败: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        showError("新增管理员失败: " + e.getMessage());
                        return;
                    }
                }
                
                showInformation("提示", "新增成功"); 
                refresh();
                // 强制刷新表格显示
                table.refresh();
                // 强制刷新列绑定，确保显示最新的缓存数据
                colRealName.setCellValueFactory(c -> new SimpleStringProperty(
                    realNameByUserId.getOrDefault(c.getValue().getUserId(), "")));
                colTypeInfo.setCellValueFactory(c -> new SimpleStringProperty(
                    typeInfoByUserId.getOrDefault(c.getValue().getUserId(), "")));
                return;
            }

            // 编辑：允许切换角色并迁移明细
            // 更新用户核心信息（若密码留空则不改）
            originUser.setUsername(username);
            if (password != null && !password.isEmpty()) {
                originUser.setPassword(password);
            }

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
                    String response = SocketClient.sendRequest("UPDATE_STUDENT:" + tfStuId.getText().trim() + ":" + tfStuName.getText().trim() + ":" + tfClass.getText().trim() + ":" + "男" + ":" + "test@example.com" + ":" + "123456" + ":" + "正常");
                    okRole = response != null && response.startsWith("SUCCESS:UPDATE:");
                } else if (rbTch.isSelected()) {
                    String response = SocketClient.sendRequest("UPDATE_TEACHER:" + tfTchId.getText().trim() + ":" + tfTchName.getText().trim() + ":" + cbSex.getValue() + ":" + tfTech.getText().trim() + ":" + tfDept.getText().trim());
                    okRole = response != null && response.startsWith("SUCCESS:UPDATE:");
                } else {
                    String response = SocketClient.sendRequest("UPDATE_ADMIN:" + tfAdmId.getText().trim() + ":" + tfAdmName.getText().trim());
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
                        if (userString.trim().isEmpty()) continue;
                        String[] fields = userString.split(",");
                        if (fields.length >= 3) {
                            User user = new User();
                            user.setUserId(Integer.parseInt(fields[0]));
                            user.setUsername(fields[1]);
                            user.setRole(fields[2]);
                            users.add(user);
                        } else {
                            System.err.println("[UserMgmt] 用户数据字段不足: " + userString);
                        }
                    }
                }
            } else {
                System.err.println("[UserMgmt] 用户数据响应格式错误: " + response);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 解析用户数据异常: " + e.getMessage());
            e.printStackTrace();
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
                        if (studentString.trim().isEmpty()) continue;
                        System.out.println("[UserMgmt] 解析学生数据: " + studentString);
                        String[] fields = studentString.split(",");
                        System.out.println("[UserMgmt] 字段数量: " + fields.length + ", 字段内容: " + java.util.Arrays.toString(fields));
                        if (fields.length >= 4) {
                            Student student = new Student();
                            student.setStudentId(fields[0]);
                            student.setStudentName(fields[1]);
                            student.setUserId(Integer.parseInt(fields[2]));
                            student.setClassName(fields[3]);
                            // 安全地处理可能缺失的字段
                            student.setSex(fields.length > 4 ? fields[4] : "");
                            student.setEmail(fields.length > 5 ? fields[5] : "");
                            System.out.println("[UserMgmt] 解析结果: userId=" + student.getUserId() + ", name=" + student.getStudentName() + ", class=" + student.getClassName());
                            students.add(student);
                        } else {
                            System.err.println("[UserMgmt] 学生数据字段不足: " + studentString);
                        }
                    }
                }
            } else {
                System.err.println("[UserMgmt] 学生数据响应格式错误: " + response);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 解析学生数据异常: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace(); // 添加详细错误信息
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
                        if (adminString.trim().isEmpty()) continue;
                        String[] fields = adminString.split(",");
                        if (fields.length >= 3) {
                            Admin admin = new Admin();
                            admin.setAdminId(fields[0]);
                            admin.setAdminName(fields[1]);
                            admin.setUserId(Integer.parseInt(fields[2]));
                            admins.add(admin);
                        } else {
                            System.err.println("[UserMgmt] 管理员数据字段不足: " + adminString);
                        }
                    }
                }
            } else {
                System.err.println("[UserMgmt] 管理员数据响应格式错误: " + response);
            }
        } catch (Exception e) {
            System.err.println("[UserMgmt] 解析管理员数据异常: " + e.getMessage());
            e.printStackTrace();
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
