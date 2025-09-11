package com.vCampus.view;

//导入JavaFX的UI组件
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
//导入工具类Pair
import javafx.util.Pair;
//导入Service层，而不是DAO或直接操作数据库！
import com.vCampus.service.UserService;

/**
* 登录对话框 UI 类
* 职责：负责展示登录界面，处理用户输入和交互，并调用Service层完成业务功能。
* 注意：这个类不应该包含任何业务逻辑或数据库操作，只负责“显示”和“转发”。
*/
public class LoginDlg {

 /**
  * 显示一个模态登录对话框，并等待用户输入。
  * @return 一个Pair对象，包含成功登录的用户名和密码；如果登录失败或取消，返回null。
  * 
  * 设计思路：
  * 1. 这个方法纯粹负责UI的构建和事件处理。
  * 2. 当用户点击“登录”时，它收集输入的数据，然后交给Service层处理。
  * 3. 它根据Service层返回的结果，决定是关闭对话框还是显示错误信息。
  */
 public static Pair<String, String> showLoginDialog() {
     // 1. 创建对话框 - 纯粹的UI搭建
     Dialog<Pair<String, String>> dialog = new Dialog<>();
     dialog.setTitle("用户登录");
     dialog.setHeaderText("请输入用户名和密码");

     // 2. 设置按钮
     ButtonType loginButtonType = new ButtonType("登录", ButtonBar.ButtonData.OK_DONE);
     dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

     // 3. 创建输入框和布局 - 纯粹的UI搭建
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

     // 4. 设置结果转换器 - 这里是UI事件和业务逻辑的连接点
     dialog.setResultConverter(buttonType -> {
         // 判断用户点击的是否是“登录”按钮
         if (buttonType == loginButtonType) {
             // 获取UI上的输入
             String username = usernameField.getText();
             String password = passwordField.getText();

             // 【核心】调用Service层的业务方法进行登录验证
             // UI层不关心如何验证，只关心结果
             if (UserService.login(username, password)) {
                 // 业务逻辑成功，返回用户名和密码（后续可返回User对象更安全）
                 return new Pair<>(username, password);
             } else {
                 // 业务逻辑失败，显示错误信息（UI职责）
                 showErrorAlert("登录失败", "用户名或密码错误！");
                 // 返回null，表示对话框不关闭，让用户重新输入
                 return null;
             }
         }
         // 如果点击的是取消或其他按钮，也返回null
         return null;
     });

     // 5. 显示对话框并返回最终结果（会阻塞直到对话框关闭）
     // Optional是Java 8的类，用于处理可能为null的值，orElse(null)表示如果不存在则返回null
     return dialog.showAndWait().orElse(null);
 }

 /**
  * 辅助方法：显示错误提示框
  * @param title 窗口标题
  * @param content 错误内容
  * 职责：属于UI层的 helper 方法，只负责显示UI组件。
  */
 private static void showErrorAlert(String title, String content) {
     Alert alert = new Alert(Alert.AlertType.ERROR);
     alert.setTitle(title);
     alert.setHeaderText(null); // 不显示额外的标题头
     alert.setContentText(content);
     alert.showAndWait(); // 显示并等待用户关闭
 }
 
 // 【重要】旧的 validateCredentials 方法已被彻底删除！
 // 所有数据库操作都应该迁移到DAO层，并通过Service层调用。
}