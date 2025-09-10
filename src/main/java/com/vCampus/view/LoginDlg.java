package com.vCampus.view;


import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import javafx.util.Pair;
import java.sql.*;

public class LoginDlg {
    // 数据库连接信息（根据你的实际情况修改）
    private static final String DB_URL = "jdbc:ucanaccess://C:/path/to/your/database.accdb";

    public static Pair<String, String> showLoginDialog() {
        // 1. 创建对话框
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("用户登录");
        dialog.setHeaderText("请输入用户名和密码");

        // 2. 设置按钮（登录 & 取消）
        ButtonType loginButtonType = new ButtonType("登录", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // 3. 创建输入框
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

        // 4. 转换结果为 Pair<用户名, 密码>（带数据库验证）
        dialog.setResultConverter(buttonType -> {
            if (buttonType == loginButtonType) {//如果摁了登录按钮
                String username = usernameField.getText();
                String password = passwordField.getText();

                if (validateCredentials(username, password)) {//检查用户名密码是否在数据库中匹配。
                	//validateCredentials():此函数之后会定义
                    System.out.println("登录成功！");
                    return new Pair<>(username, password);
                } else {
                    // 显示错误提示
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("登录失败");
                    alert.setHeaderText(null);
                    alert.setContentText("用户名或密码错误！");
                    alert.showAndWait();
                    return null; // 返回null表示登录失败
                }
            }
            return null;
        });

        // 5. 显示对话框并返回结果
        Optional<Pair<String, String>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 验证用户名和密码是否匹配数据库中的记录
     */
    private static boolean validateCredentials(String username, String password) {
        //  Access 数据库路径
        String DB_URL = "jdbc:ucanaccess://C:/your_database_path.accdb";
        
        // SQL 查询（假设表名是 Students，字段是 name 和 key）
        String sql = "SELECT COUNT(*) FROM Students WHERE name = ? AND key = ?";
        
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            // 设置参数
            pstmt.setString(1, username);  // 用户名
            pstmt.setString(2, password);  // 密码（key）
            
            // 执行查询
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;  // 如果找到匹配记录，返回 true
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库验证错误: " + e.getMessage());
            e.printStackTrace();  // 打印详细错误信息
        }
        return false;  // 默认返回 false（验证失败）
    }
}
