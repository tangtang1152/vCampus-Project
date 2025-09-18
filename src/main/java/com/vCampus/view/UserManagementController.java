package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Admin;
import com.vCampus.entity.Student;
import com.vCampus.entity.Teacher;
import com.vCampus.entity.User;
import com.vCampus.service.*;
import com.vCampus.util.RBACUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private final IUserService userService = ServiceFactory.getUserService();
    private final IStudentService studentService = ServiceFactory.getStudentService();
    private final ITeacherService teacherService = ServiceFactory.getTeacherService();
    private final IAdminService adminService = ServiceFactory.getAdminService();

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
    @FXML private void onClose() { ((javafx.stage.Stage) table.getScene().getWindow()).close(); }

    private void refresh() {
        List<User> all = userService.getAll();

        // 一次性加载三类详情，构建缓存，避免单元格重复触发数据库
        realNameByUserId.clear();
        typeInfoByUserId.clear();
        try {
            List<Student> studs = studentService.getAll();
            for (Student s : studs) {
                realNameByUserId.put(s.getUserId(), s.getStudentName() == null ? "" : s.getStudentName());
                typeInfoByUserId.put(s.getUserId(), "班级:" + (s.getClassName() == null ? "" : s.getClassName()));
            }
        } catch (Exception ignored) {}
        try {
            List<Teacher> tchs = teacherService.getAll();
            for (Teacher t : tchs) {
                realNameByUserId.put(t.getUserId(), t.getTeacherName() == null ? "" : t.getTeacherName());
                typeInfoByUserId.put(t.getUserId(), "部门:" + (t.getDepartmentId() == null ? "" : t.getDepartmentId()));
            }
        } catch (Exception ignored) {}
        try {
            List<Admin> adms = adminService.getAll();
            for (Admin a : adms) {
                realNameByUserId.put(a.getUserId(), a.getAdminName() == null ? "" : a.getAdminName());
                typeInfoByUserId.put(a.getUserId(), "工号:" + (a.getAdminId() == null ? "" : a.getAdminId()));
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
                Student s = studentService.getByUserId(sel.getUserId());
                // 先清理可能的外键引用（订单、借阅等）——这里演示：如有订单，给出阻断提示
                // 实际可在 Service 层增加“级联清理”接口
                ok = (s != null) && studentService.delete(s.getStudentId());
            } else if (rs.contains("TEACHER")) {
                Teacher t = teacherService.getByUserId(sel.getUserId());
                ok = (t != null) && teacherService.delete(t.getTeacherId());
            } else if (rs.contains("ADMIN")) {
                Admin a = adminService.getByUserId(sel.getUserId());
                ok = (a != null) && adminService.delete(a.getAdminId());
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
        var form = new javafx.scene.layout.VBox(10);
        form.setPadding(new javafx.geometry.Insets(16));
        form.setFillWidth(true);

        // 小工具：生成“标签在上，控件在下”的竖版字段块
        java.util.function.BiFunction<String, javafx.scene.Node, javafx.scene.layout.VBox> makeField = (text, node) -> {
            Label lbl = new Label(text);
            var box = new javafx.scene.layout.VBox(4, lbl, node);
            box.setFillWidth(true);
            if (node instanceof javafx.scene.control.Control) {
                ((javafx.scene.control.Control) node).setMaxWidth(Double.MAX_VALUE);
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
        var roleRow = new javafx.scene.layout.HBox(12, rbStu, rbTch, rbAdm);
        roleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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
        var groupStu = new javafx.scene.layout.VBox(8,
            makeField.apply("学号", tfStuId),
            makeField.apply("学生姓名", tfStuName),
            makeField.apply("班级", tfClass)
        );
        var groupTch = new javafx.scene.layout.VBox(8,
            makeField.apply("教师编号", tfTchId),
            makeField.apply("教师姓名", tfTchName),
            makeField.apply("性别", cbSex),
            makeField.apply("职称", tfTech),
            makeField.apply("部门ID", tfDept)
        );
        var groupAdm = new javafx.scene.layout.VBox(8,
            makeField.apply("管理员工号", tfAdmId),
            makeField.apply("管理员姓名", tfAdmName)
        );

        // 若编辑，回填明细
        if (isEdit) {
            Set<String> rs = originUser.getRoleSet();
            if (rs.contains("STUDENT")) {
                Student s = studentService.getByUserId(originUser.getUserId());
                if (s != null) {
                    tfStuId.setText(s.getStudentId()); tfStuId.setEditable(false);
                    tfStuName.setText(s.getStudentName()); tfClass.setText(s.getClassName());
                }
                rbStu.setSelected(true);
            } else if (rs.contains("TEACHER")) {
                Teacher t = teacherService.getByUserId(originUser.getUserId());
                if (t != null) {
                    tfTchId.setText(t.getTeacherId()); tfTchId.setEditable(false);
                    tfTchName.setText(t.getTeacherName()); cbSex.setValue(t.getSex());
                    tfTech.setText(t.getTechnical()); tfDept.setText(t.getDepartmentId());
                }
                rbTch.setSelected(true);
            } else if (rs.contains("ADMIN")) {
                Admin a = adminService.getByUserId(originUser.getUserId());
                if (a != null) {
                    tfAdmId.setText(a.getAdminId()); tfAdmId.setEditable(false);
                    tfAdmName.setText(a.getAdminName());
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
        var sp = new javafx.scene.control.ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(420);

        dlg.setResizable(true);
        dlg.getDialogPane().setPrefSize(520, 560);
        dlg.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
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
                    var res = userService.register(s);
                    if (res != IUserService.RegisterResult.SUCCESS) { showError("新增学生失败: " + res); return; }
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
                    var res = userService.register(t);
                    if (res != IUserService.RegisterResult.SUCCESS) { showError("新增教师失败: " + res); return; }
                } else if (chooseAdm) {
                    if (tfAdmId.getText().trim().isEmpty() || tfAdmName.getText().trim().isEmpty()) { showError("请填写管理员工号与姓名"); return; }
                    Admin a = new Admin();
                    a.setUsername(username); a.setPassword(password.isEmpty()?"123456":password);
                    a.setRole("admin");
                    a.setAdminId(tfAdmId.getText().trim());
                    a.setAdminName(tfAdmName.getText().trim());
                    var res = userService.register(a);
                    if (res != IUserService.RegisterResult.SUCCESS) { showError("新增管理员失败: " + res); return; }
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
            boolean okUser = userService.update(originUser);

            boolean okRole = true;
            try {
                if (!targetRole.equalsIgnoreCase(oldRole)) {
                    // 删除旧角色明细
                    if ("student".equals(oldRole)) {
                        Student sOld = studentService.getByUserId(originUser.getUserId());
                        if (sOld != null) studentService.deleteStudentOnly(sOld.getStudentId());
                    } else if ("teacher".equals(oldRole)) {
                        Teacher tOld = teacherService.getByUserId(originUser.getUserId());
                        if (tOld != null) teacherService.deleteTeacherOnly(tOld.getTeacherId());
                    } else if ("admin".equals(oldRole)) {
                        Admin aOld = adminService.getByUserId(originUser.getUserId());
                        if (aOld != null) adminService.deleteAdminOnly(aOld.getAdminId());
                    }
                    // 新建目标角色明细
                    if (rbStu.isSelected()) {
                        Student s = new Student(); s.setUserId(originUser.getUserId());
                        s.setStudentId(tfStuId.getText().trim()); s.setStudentName(tfStuName.getText().trim()); s.setClassName(tfClass.getText().trim());
                        okRole = studentService.add(s);
                    } else if (rbTch.isSelected()) {
                        Teacher t = new Teacher(); t.setUserId(originUser.getUserId());
                        t.setTeacherId(tfTchId.getText().trim()); t.setTeacherName(tfTchName.getText().trim()); t.setSex(cbSex.getValue()); t.setTechnical(tfTech.getText().trim()); t.setDepartmentId(tfDept.getText().trim());
                        okRole = teacherService.add(t);
                    } else {
                        Admin a = new Admin(); a.setUserId(originUser.getUserId());
                        a.setAdminId(tfAdmId.getText().trim()); a.setAdminName(tfAdmName.getText().trim());
                        okRole = adminService.add(a);
                    }
                } else {
                    // 角色未变，更新现有明细
                    if (rbStu.isSelected()) {
                        Student s = studentService.getByUserId(originUser.getUserId());
                        if (s != null) { s.setStudentName(tfStuName.getText().trim()); s.setClassName(tfClass.getText().trim()); okRole = studentService.updateStudentOnly(s); }
                    } else if (rbTch.isSelected()) {
                        Teacher t = teacherService.getByUserId(originUser.getUserId());
                        if (t != null) { t.setTeacherName(tfTchName.getText().trim()); t.setSex(cbSex.getValue()); t.setTechnical(tfTech.getText().trim()); t.setDepartmentId(tfDept.getText().trim()); okRole = teacherService.updateTeacherOnly(t); }
                    } else {
                        Admin a = adminService.getByUserId(originUser.getUserId());
                        if (a != null) { a.setAdminName(tfAdmName.getText().trim()); okRole = adminService.updateAdminOnly(a); }
                    }
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
}


