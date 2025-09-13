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
        try {
            URL fxmlUrl = NavigationUtil.class.getResource("/fxml/" + fxmlPath);
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found: " + fxmlPath);
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.getPrimaryStage());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            // 设置对话框图标
            try {
                dialogStage.getIcons().add(new Image(
                    NavigationUtil.class.getResourceAsStream("/images/app-icon.png")));
            } catch (Exception e) {
                System.out.println("对话框图标加载失败");
            }
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to open dialog: " + e.getMessage(), e);
        }
    }
}