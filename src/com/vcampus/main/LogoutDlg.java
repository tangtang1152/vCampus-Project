package com.vcampus.main;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import javafx.geometry.Insets;

public class LogoutDlg {

    public static Pair<String, String> showAndWait() {
        // 1. 创建对话框
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("安全注销");
        dialog.setHeaderText("请输入用户名和密码");

        // 2. 设置按钮
        ButtonType confirmButton = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        // 3. 创建输入控件
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密码");

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("密码:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 4. 返回输入结果（不验证）
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButton) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        // 5. 显示对话框并返回结果
        return dialog.showAndWait().orElse(null);
    }
}

