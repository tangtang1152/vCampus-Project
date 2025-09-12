package com.vCampus.common;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

/**
 * 所有控制器的基类
 * 提供通用的功能和方法
 */
public abstract class BaseController implements Initializable {
    
    /**
     * 显示成功消息
     */
    protected void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示错误消息
     */
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示警告消息
     */
    protected void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 验证输入是否为空
     */
    protected boolean validateInput(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}