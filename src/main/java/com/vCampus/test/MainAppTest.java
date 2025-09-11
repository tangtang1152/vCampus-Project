package com.vCampus.test;

import com.vCampus.view.LoginDlg;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainAppTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("========== 主应用测试 ==========");
        
        // 测试登录对话框
        Pair<String, String> loginResult = LoginDlg.showLoginDialog();
        if (loginResult != null) {
            System.out.println("✅ 登录成功 - 用户名: " + loginResult.getKey());
            // 这里可以打开主界面
        } else {
            System.out.println("❌ 登录取消或失败");
        }
        
        System.out.println("========== 测试结束 ==========");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}