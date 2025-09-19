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

        // 角色多选
        CheckBox cbRoleStu = new CheckBox("学生");
        CheckBox cbRoleTch = new CheckBox("教师");
        CheckBox cbRoleAdm = new CheckBox("管理员");
        var roleRow = new javafx.scene.layout.HBox(12, cbRoleStu, cbRoleTch, cbRoleAdm);
        roleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        java.util.Set<String> existingRoles = isEdit
            ? originUser.getRoleSet().stream().map(String::toUpperCase).collect(java.util.stream.Collectors.toSet())
            : java.util.Collections.emptySet();
        if (isEdit) {
            cbRoleStu.setSelected(existingRoles.contains("STUDENT"));
            cbRoleTch.setSelected(existingRoles.contains("TEACHER"));
            cbRoleAdm.setSelected(existingRoles.contains("ADMIN"));
        } else {
            cbRoleStu.setSelected(true); // 新增默认勾选学生
        }

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
            Set<String> rs = originUser.getRoleSet().stream().map(String::toUpperCase).collect(java.util.stream.Collectors.toSet());
            if (rs.contains("STUDENT")) {
                Student s = studentService.getByUserId(originUser.getUserId());
                if (s != null) {
                    tfStuId.setText(s.getStudentId()); tfStuId.setEditable(false);
                    tfStuName.setText(s.getStudentName()); tfClass.setText(s.getClassName());
                }
            }
            if (rs.contains("TEACHER")) {
                Teacher t = teacherService.getByUserId(originUser.getUserId());
                if (t != null) {
                    tfTchId.setText(t.getTeacherId()); tfTchId.setEditable(false);
                    tfTchName.setText(t.getTeacherName()); cbSex.setValue(t.getSex());
                    tfTech.setText(t.getTechnical()); tfDept.setText(t.getDepartmentId());
                }
            }
            if (rs.contains("ADMIN")) {
                Admin a = adminService.getByUserId(originUser.getUserId());
                if (a != null) {
                    tfAdmId.setText(a.getAdminId()); tfAdmId.setEditable(false);
                    tfAdmName.setText(a.getAdminName());
                }
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

        // 角色切换联动（多选可见性 + 调试输出）
        Runnable updateRoleVisibility = () -> {
            boolean s = cbRoleStu.isSelected();
            boolean t = cbRoleTch.isSelected();
            boolean a = cbRoleAdm.isSelected();
            groupStu.setManaged(s); groupStu.setVisible(s);
            groupTch.setManaged(t); groupTch.setVisible(t);
            groupAdm.setManaged(a); groupAdm.setVisible(a);
            System.out.println("[UserMgmt] 角色区域切换 => S=" + s + ", T=" + t + ", A=" + a);
        };
        updateRoleVisibility.run();
        cbRoleStu.setOnAction(e -> updateRoleVisibility.run());
        cbRoleTch.setOnAction(e -> updateRoleVisibility.run());
        cbRoleAdm.setOnAction(e -> updateRoleVisibility.run());

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
            if (username.isEmpty()) { showError("用户名不能为空"); return; }

            if (!isEdit) {
                // 新增（支持多角色）：至少选择一个角色
                boolean chooseStu = cbRoleStu.isSelected();
                boolean chooseTch = cbRoleTch.isSelected();
                boolean chooseAdm = cbRoleAdm.isSelected();
                if (!chooseStu && !chooseTch && !chooseAdm) { showError("请至少选择一个角色"); return; }

                // 针对选择的角色进行字段校验
                if (chooseStu && (tfStuId.getText().trim().isEmpty() || tfStuName.getText().trim().isEmpty())) { showError("请填写学号与姓名"); return; }
                if (chooseTch && (tfTchId.getText().trim().isEmpty() || tfTchName.getText().trim().isEmpty())) { showError("请填写教师编号与姓名"); return; }
                if (chooseAdm && (tfAdmId.getText().trim().isEmpty() || tfAdmName.getText().trim().isEmpty())) { showError("请填写管理员工号与姓名"); return; }

                // 先用一个“主角色”完成用户主表插入
                IUserService.RegisterResult res;
                if (chooseStu) {
                    Student s = new Student();
                    s.setUsername(username); s.setPassword(password.isEmpty()?"123456":password);
                    s.setRole("student");
                    s.setStudentId(tfStuId.getText().trim());
                    s.setStudentName(tfStuName.getText().trim());
                    s.setClassName(tfClass.getText().trim());
                    res = userService.register(s);
                } else if (chooseTch) {
                    Teacher t = new Teacher();
                    t.setUsername(username); t.setPassword(password.isEmpty()?"123456":password);
                    t.setRole("teacher");
                    t.setTeacherId(tfTchId.getText().trim());
                    t.setTeacherName(tfTchName.getText().trim());
                    t.setSex(cbSex.getValue());
                    t.setTechnical(tfTech.getText().trim());
                    t.setDepartmentId(tfDept.getText().trim());
                    res = userService.register(t);
                } else {
                    Admin a = new Admin();
                    a.setUsername(username); a.setPassword(password.isEmpty()?"123456":password);
                    a.setRole("admin");
                    a.setAdminId(tfAdmId.getText().trim());
                    a.setAdminName(tfAdmName.getText().trim());
                    res = userService.register(a);
                }
                if (res != IUserService.RegisterResult.SUCCESS) { showError("新增失败: " + res); return; }

                // 获取刚创建的用户，并写入多角色集合
                User created = userService.getByUsername(username);
                if (created == null) { showError("新增失败：未找到新建用户"); return; }
                java.util.LinkedHashSet<String> rolesUpper = new java.util.LinkedHashSet<>();
                if (chooseStu) rolesUpper.add("STUDENT");
                if (chooseTch) rolesUpper.add("TEACHER");
                if (chooseAdm) rolesUpper.add("ADMIN");
                created.setRoleSet(rolesUpper);
                boolean okUpdateUser = userService.update(created);
                if (!okUpdateUser) { showError("新增失败：更新用户角色集失败"); return; }

                // 为额外角色补充明细记录
                boolean okDetails = true;
                int uid = created.getUserId();
                // 唯一性预检查：避免主键或唯一索引冲突
                if (chooseStu) {
                    Student existByStuId = studentService.getBySelfId(tfStuId.getText().trim());
                    if (existByStuId != null && !uidEquals(existByStuId.getUserId(), uid)) { showError("学号已被其他用户占用"); return; }
                }
                if (chooseTch) {
                    Teacher existByTchId = teacherService.getBySelfId(tfTchId.getText().trim());
                    if (existByTchId != null && !uidEquals(existByTchId.getUserId(), uid)) { showError("教师编号已被其他用户占用"); return; }
                }
                if (chooseAdm) {
                    Admin existByAdmId = adminService.getBySelfId(tfAdmId.getText().trim());
                    if (existByAdmId != null && !uidEquals(existByAdmId.getUserId(), uid)) { showError("管理员工号已被其他用户占用"); return; }
                }
                if (chooseStu) {
                    Student s = studentService.getByUserId(uid);
                    if (s == null) { s = new Student(); s.setUserId(uid); s.setStudentId(tfStuId.getText().trim()); s.setStudentName(tfStuName.getText().trim()); s.setClassName(tfClass.getText().trim()); okDetails &= studentService.add(s); }
                }
                if (chooseTch) {
                    Teacher t = teacherService.getByUserId(uid);
                    if (t == null) { t = new Teacher(); t.setUserId(uid); t.setTeacherId(tfTchId.getText().trim()); t.setTeacherName(tfTchName.getText().trim()); t.setSex(cbSex.getValue()); t.setTechnical(tfTech.getText().trim()); t.setDepartmentId(tfDept.getText().trim()); okDetails &= teacherService.add(t); }
                }
                if (chooseAdm) {
                    Admin a = adminService.getByUserId(uid);
                    if (a == null) { a = new Admin(); a.setUserId(uid); a.setAdminId(tfAdmId.getText().trim()); a.setAdminName(tfAdmName.getText().trim()); okDetails &= adminService.add(a); }
                }

                if (!okDetails) { showError("新增失败：角色明细保存出错"); return; }
                showInformation("提示", "新增成功"); refresh();
                return;
            }

            // 编辑：允许切换为多角色，并按选择增删改对应明细
            originUser.setUsername(username);
            if (!password.isEmpty()) originUser.setPassword(password);

            java.util.LinkedHashSet<String> selectedRoles = new java.util.LinkedHashSet<>();
            if (cbRoleStu.isSelected()) selectedRoles.add("STUDENT");
            if (cbRoleTch.isSelected()) selectedRoles.add("TEACHER");
            if (cbRoleAdm.isSelected()) selectedRoles.add("ADMIN");
            if (selectedRoles.isEmpty()) { showError("至少选择一个角色"); return; }

            // 计算角色变化集
            java.util.Set<String> oldRoles = existingRoles;
            java.util.Set<String> toAdd = new java.util.LinkedHashSet<>(selectedRoles); toAdd.removeAll(oldRoles);
            java.util.Set<String> toRemove = new java.util.LinkedHashSet<>(oldRoles); toRemove.removeAll(selectedRoles);

            // 唯一性预检查：对于新增的角色，验证自增ID/工号未被其他用户占用
            if (toAdd.contains("STUDENT")) {
                if (tfStuId.getText().trim().isEmpty() || tfStuName.getText().trim().isEmpty()) { showError("请填写学号与姓名"); return; }
                Student existByStuId = studentService.getBySelfId(tfStuId.getText().trim());
                if (existByStuId != null && !uidEquals(existByStuId.getUserId(), originUser.getUserId())) { showError("学号已被其他用户占用"); return; }
            }
            if (toAdd.contains("TEACHER")) {
                if (tfTchId.getText().trim().isEmpty() || tfTchName.getText().trim().isEmpty()) { showError("请填写教师编号与姓名"); return; }
                Teacher existByTchId = teacherService.getBySelfId(tfTchId.getText().trim());
                if (existByTchId != null && !uidEquals(existByTchId.getUserId(), originUser.getUserId())) { showError("教师编号已被其他用户占用"); return; }
            }
            if (toAdd.contains("ADMIN")) {
                if (tfAdmId.getText().trim().isEmpty() || tfAdmName.getText().trim().isEmpty()) { showError("请填写管理员工号与姓名"); return; }
                Admin existByAdmId = adminService.getBySelfId(tfAdmId.getText().trim());
                if (existByAdmId != null && !uidEquals(existByAdmId.getUserId(), originUser.getUserId())) { showError("管理员工号已被其他用户占用"); return; }
            }

            // 预检通过后再更新用户角色集
            originUser.setRoleSet(selectedRoles);
            boolean okUser = userService.update(originUser);

            boolean okRole = true;
            try {
                // 已有 toAdd/toRemove

                // 删除未选中的旧角色明细
                if (toRemove.contains("STUDENT")) {
                    Student sOld = studentService.getByUserId(originUser.getUserId());
                    if (sOld != null) okRole &= studentService.deleteStudentOnly(sOld.getStudentId());
                }
                if (toRemove.contains("TEACHER")) {
                    Teacher tOld = teacherService.getByUserId(originUser.getUserId());
                    if (tOld != null) okRole &= teacherService.deleteTeacherOnly(tOld.getTeacherId());
                }
                if (toRemove.contains("ADMIN")) {
                    Admin aOld = adminService.getByUserId(originUser.getUserId());
                    if (aOld != null) okRole &= adminService.deleteAdminOnly(aOld.getAdminId());
                }

                // 新增选中的新角色明细
                if (toAdd.contains("STUDENT")) {
                    if (tfStuId.getText().trim().isEmpty() || tfStuName.getText().trim().isEmpty()) { showError("请填写学号与姓名"); return; }
                    Student s = new Student(); s.setUserId(originUser.getUserId());
                    s.setStudentId(tfStuId.getText().trim()); s.setStudentName(tfStuName.getText().trim()); s.setClassName(tfClass.getText().trim());
                    okRole &= studentService.add(s);
                }
                if (toAdd.contains("TEACHER")) {
                    if (tfTchId.getText().trim().isEmpty() || tfTchName.getText().trim().isEmpty()) { showError("请填写教师编号与姓名"); return; }
                    Teacher t = new Teacher(); t.setUserId(originUser.getUserId());
                    t.setTeacherId(tfTchId.getText().trim()); t.setTeacherName(tfTchName.getText().trim()); t.setSex(cbSex.getValue()); t.setTechnical(tfTech.getText().trim()); t.setDepartmentId(tfDept.getText().trim());
                    okRole &= teacherService.add(t);
                }
                if (toAdd.contains("ADMIN")) {
                    if (tfAdmId.getText().trim().isEmpty() || tfAdmName.getText().trim().isEmpty()) { showError("请填写管理员工号与姓名"); return; }
                    Admin a = new Admin(); a.setUserId(originUser.getUserId());
                    a.setAdminId(tfAdmId.getText().trim()); a.setAdminName(tfAdmName.getText().trim());
                    okRole &= adminService.add(a);
                }

                // 更新现有角色明细
                if (selectedRoles.contains("STUDENT") && oldRoles.contains("STUDENT")) {
                    Student s = studentService.getByUserId(originUser.getUserId());
                    if (s != null) { s.setStudentName(tfStuName.getText().trim()); s.setClassName(tfClass.getText().trim()); okRole &= studentService.updateStudentOnly(s); }
                }
                if (selectedRoles.contains("TEACHER") && oldRoles.contains("TEACHER")) {
                    Teacher t = teacherService.getByUserId(originUser.getUserId());
                    if (t != null) { t.setTeacherName(tfTchName.getText().trim()); t.setSex(cbSex.getValue()); t.setTechnical(tfTech.getText().trim()); t.setDepartmentId(tfDept.getText().trim()); okRole &= teacherService.updateTeacherOnly(t); }
                }
                if (selectedRoles.contains("ADMIN") && oldRoles.contains("ADMIN")) {
                    Admin a = adminService.getByUserId(originUser.getUserId());
                    if (a != null) { a.setAdminName(tfAdmName.getText().trim()); okRole &= adminService.updateAdminOnly(a); }
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

    private boolean uidEquals(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.intValue() == b.intValue();
    }
}


