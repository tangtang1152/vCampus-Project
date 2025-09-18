package client.net;
//文件顶部添加
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
 private static final String SERVER_IP = "localhost";
 private static final int SERVER_PORT = 8888;
 
 public static String sendRequest(String request) throws IOException {
        System.out.println("[DEBUG] 发送请求: " + request); // 添加这行
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        	//创建了对象read 和write用于读写
        	
            out.println(request);//将字符串 request 写入缓冲区，通过 Socket 输出流发送到服务器（自动添加换行符 \n 或 \r\n）
            return in.readLine();//从 Socket 输入流中读取服务器返回的一行数据（直到遇到换行符或流结束）
        }
    }
}