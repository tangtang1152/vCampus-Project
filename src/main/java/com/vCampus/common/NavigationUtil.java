package com.vCampus.common;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/" + fxmlPath));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("对话框显示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}