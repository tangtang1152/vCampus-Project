

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.vCampus.entity.Student;
import javafx.geometry.Insets;
import java.util.Optional;

public class RegisterDlg {
    public static Student showAndWait() {
        // 1. 修改泛型为 Student
        Dialog<Student> dialog = new Dialog<>(); //返回类型：Student
        dialog.setTitle("学生注册");
        dialog.setHeaderText("请输入学生信息（带*为必填项）");

        // 2. 设置按钮
        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        // 3. 创建输入字段
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("*学号");

        TextField nameField = new TextField();
        nameField.setPromptText("*姓名");

        PasswordField keyField = new PasswordField();
        keyField.setPromptText("*密码");

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        genderCombo.setValue("男"); // 设置默认值

        // 添加控件到网格
        grid.add(new Label("*学号:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("*姓名:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("*密码:"), 0, 2);
        grid.add(keyField, 1, 2);
        grid.add(new Label("性别:"), 0, 3);
        grid.add(genderCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // 4. 修改 ResultConverter 直接返回 Student
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {//如果点解了“注册”按钮
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() || keyField.getText().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "请填写所有必填项！").show();//创建一个Alert对象对话框
                    return null;
                }//如果没有填写所有必选项目，创建一个Alert对象对话框，并且返回Null
                
                //else:
                try {
                    int id = Integer.parseInt(idField.getText());
                    return new Student(id, nameField.getText(), keyField.getText()); // 直接返回学生对象
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "学号必须是数字！").show();
                    return null;//如果学号是数字，则创建一个Alert对象对话框，dialog对象返回Null
                }
            }
            return null;
        });//创建对话框结束

        // 5. 正确接收结果
        Optional<Student> result = dialog.showAndWait();//打开对话框并等待用户输入。这个showAndWait是JFX自带的
        //Optional 是什么：Java 8引入的容器类，表示结果可能有值（Student对象）或为空
        return result.orElse(null);
        //将 Optional 容器中的值提取出来，并以传统方式（返回 Student 对象或 null）结束方法调用
    }
}