package client.net;
import javafx.application.Platform;  // 用于JavaFX线程安全操作

public class ResponseHandler {
    public static void handle(String response) {
        if (response.startsWith("REGISTER_SUCCESS")) {//检查字符串是否以REGISTER_SUCCESS开头
            Platform.runLater(() -> {
                String username = response.split("\\|")[1];//提取按斜线分割后的第二部分:username
                System.out.println("注册成功，用户名: " + username);
            });
        } else if (response.startsWith("ERROR")) {
            Platform.runLater(() -> {
                System.err.println("错误: " + response.split("\\|")[1]);
            });
        }
    }
}
