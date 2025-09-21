package com.vCampus.view;

import com.vCampus.common.BaseController;
import com.vCampus.entity.Student;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class StudentDetailController extends BaseController {
    @FXML private Label lbStudentId;
    @FXML private Label lbStudentName;
    @FXML private Label lbClassName;
    @FXML private Label lbSex;
    @FXML private Label lbEnrollDate;
    @FXML private Label lbEmail;
    @FXML private Label lbIdCard;
    @FXML private Label lbStatus;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public void setStudent(Student s) {
        if (s == null) return;
        lbStudentId.setText(s.getStudentId());
        lbStudentName.setText(s.getStudentName());
        lbClassName.setText(s.getClassName());
        lbSex.setText(s.getSex());
        lbEnrollDate.setText(s.getEnrollDate() == null ? "" : sdf.format(s.getEnrollDate()));
        lbEmail.setText(s.getEmail());
        lbIdCard.setText(s.getIdCard());
        lbStatus.setText(s.getStatus());
    }

    @FXML
    private void onClose() {
        try { ((javafx.stage.Stage) lbStudentId.getScene().getWindow()).close(); } catch (Exception ignored) {}
    }
}


