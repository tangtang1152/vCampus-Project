package com.vCampus.common;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import com.vCampus.app.MainApp;
import com.vCampus.entity.Student;
import com.vCampus.view.StudentDetailController;
import com.vCampus.view.StudentFormController; // 导入 StudentFormController
import com.vCampus.view.StatusChangeController; // 导入 StatusChangeController


/**
 * 页面导航工具类
 * 统一管理页面跳转
 */
public class NavigationUtil {

    /**
     * 跳转到新页面
     */
    public static void navigateTo(Stage currentStage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/" + fxmlPath));
            Parent root = loader.load();

            Stage stage = currentStage != null ? currentStage : new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(true);

            if (currentStage == null) {
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("页面跳转失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示对话框
     */
    public static void showDialog(String fxmlPath, String title) {
        System.out.println("🔍 正在打开对话框: " + fxmlPath);

        try {
            // 检查类加载器
            System.out.println("🔍 当前类加载器: " + NavigationUtil.class.getClassLoader());

            // 尝试不同的路径格式
            String[] possiblePaths = {
  "/fxml/" + fxmlPath,
  "fxml/" + fxmlPath,
  "/com/vCampus/view/" + fxmlPath,
  "com/vCampus/view/" + fxmlPath
            };

            URL fxmlUrl = null;
            for (String path : possiblePaths) {
                fxmlUrl = NavigationUtil.class.getResource(path);
                System.out.println("🔍 尝试路径: " + path + " -> " + (fxmlUrl != null ? "找到" : "未找到"));
                if (fxmlUrl != null) break;
            }

            if (fxmlUrl == null) {
                System.err.println("❌ 错误: 所有路径都未找到FXML文件: " + fxmlPath);
                // 列出资源目录内容
                try {
  java.util.Enumeration<URL> resources = NavigationUtil.class.getClassLoader().getResources("fxml");
  while (resources.hasMoreElements()) {
  URL resource = resources.nextElement();
  System.out.println("🔍 资源目录: " + resource);
  }
                } catch (Exception e) {
  System.err.println("❌ 无法列出资源目录: " + e.getMessage());
                }
                throw new RuntimeException("FXML file not found: " + fxmlPath);
            }

            System.out.println("✅ FXML文件找到: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("✅ FXML加载成功");

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.getPrimaryStage());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // 设置对话框图标
            try {
                URL iconUrl = NavigationUtil.class.getResource("/images/app-icon.png");
                if (iconUrl != null) {
  dialogStage.getIcons().add(new Image(iconUrl.toString()));
  System.out.println("✅ 对话框图标加载成功");
                } else {
  System.out.println("⚠️ 图标文件未找到");
                }
            } catch (Exception e) {
                System.out.println("⚠️ 对话框图标加载失败: " + e.getMessage());
            }

            System.out.println("✅ 准备显示对话框");
            dialogStage.showAndWait();
            System.out.println("✅ 对话框已关闭");

        } catch (Exception e) {
            System.err.println("❌ 打开对话框失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to open dialog: " + e.getMessage(), e);
        }
    }

    //--

    /**
     * 显示学籍管理界面
     */
    public static void showStudentManagement() {
        showDialog("student-management-view.fxml", "学籍管理");
    }

    /**
     * 显示学生详情对话框
     */
    public static void showStudentDetail(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/student-detail-view.fxml"));
            Parent root = loader.load();

            // 传递学生数据给控制器
            StudentDetailController controller = loader.getController();
            controller.setStudent(student);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("学生详情 - " + student.getStudentName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.getPrimaryStage());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            System.err.println("打开学生详情失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 修复：显示学生表单对话框，并传递学生对象（用于编辑）
     * @param student 要编辑的学生对象，如果为null则为新增模式
     * @param title 对话框标题
     */
    public static void showStudentFormDialog(Student student, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/student-form-view.fxml"));
            Parent root = loader.load();

            StudentFormController controller = loader.getController();
            if (student != null) {
                controller.setStudent(student); // 传递学生对象给控制器
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.getPrimaryStage());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            System.err.println("打开学生表单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 修复：显示学籍状态变更对话框，并传递学生对象
     * @param student 要变更状态的学生对象
     * @param title 对话框标题
     */
    public static void showStatusChangeDialog(Student student, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/status-change-view.fxml"));
            Parent root = loader.load();

            StatusChangeController controller = loader.getController();
            controller.setStudent(student); // 传递学生对象给控制器

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.getPrimaryStage());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            System.err.println("打开状态变更对话框失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}