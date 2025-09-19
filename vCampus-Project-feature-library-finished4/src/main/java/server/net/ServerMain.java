package server.net;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//com.vCampus.server
public class ServerMain {
 public static void main(String[] args) {
     try (ServerSocket serverSocket = new ServerSocket(8888)) {
         System.out.println("服务端已启动，等待连接...");
         
         while (true) {
             // 接受客户端连接
             Socket clientSocket = serverSocket.accept();
             //是一个 阻塞方法，服务端在此等待，直到有客户端发起连接请求。
             new ClientThread(clientSocket).start();
             //调用ClientThread.java
             //这行代码是 Java 多线程服务端编程的核心操作，用于为每个客户端连接创建独立的处理线程。
         }
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}