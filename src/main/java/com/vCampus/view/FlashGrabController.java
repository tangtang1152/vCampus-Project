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
	private javafx.animation.Timeline autoRefresh;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        table.setItems(tableData);
		loadData();
		startAutoRefresh();
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

    private final java.util.concurrent.atomic.AtomicBoolean loading = new java.util.concurrent.atomic.AtomicBoolean(false);
    private void loadData() {
        if (!loading.compareAndSet(false, true)) return;
		String kw = keywordField == null ? "" : keywordField.getText();
        startDaemon(() -> {
			java.util.List<Subject> list = null;
			if (ConfigManager.isSocketEnabled()) {
				// 走服务器 SUBJECT_LIST，保持与其它客户端一致
				try {
					var req = new com.vCampus.net.dto.SocketRequest("SUBJECT_LIST").put("keyword", kw == null ? "" : kw);
					java.net.Socket s = new java.net.Socket();
					s.connect(new java.net.InetSocketAddress(ConfigManager.getSocketServerHost(), ConfigManager.getSocketServerPort()), ConfigManager.getSocketConnectTimeoutMs());
					s.setSoTimeout(ConfigManager.getSocketSoTimeoutMs());
					try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
						 java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream())) {
						out.writeObject(req); out.flush();
						Object obj = in.readObject();
						if (obj instanceof com.vCampus.net.dto.SocketResponse resp && resp.isSuccess() && resp.getData() instanceof java.util.Map<?,?> m && m.get("rows") instanceof java.util.List<?> rows) {
							list = new java.util.ArrayList<>();
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
						}
					} finally { s.close(); }
				} catch (Exception e) {
					list = null;
				}
			}
			if (!ConfigManager.isSocketEnabled()) {
				list = (kw == null || kw.isBlank()) ? subjectService.getAllSubjects() : subjectService.getSubjectsByName(kw);
			}
			final java.util.List<Subject> flist = list;
            TransactionManager.runLaterSafe(() -> {
                if (flist == null) {
                    if (lbStatus != null) lbStatus.setText("服务器不可用或连接中断");
                    return;
                }
                tableData.setAll(flist);
                lbStatus.setText("共 " + tableData.size() + " 门课");
                table.refresh();
            });
            loading.set(false);
        }, "flash-load");
	}

    private void startAutoRefresh() {
        autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(8), e -> loadData())
        );
		autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
		autoRefresh.play();
		table.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.windowProperty().addListener((o, ov, nv) -> {
					if (nv != null) nv.setOnHidden(evt -> { if (autoRefresh != null) autoRefresh.stop(); });
				});
			}
		});
	}

    @Override
    public void onUnload() {
        if (autoRefresh != null) autoRefresh.stop();
    }
}


