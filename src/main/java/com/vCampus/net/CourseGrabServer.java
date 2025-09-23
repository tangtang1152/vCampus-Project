package com.vCampus.net;

import com.vCampus.common.ConfigManager;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用线程池与对象流的 Socket 服务器，维护在线客户端映射。
 */
public class CourseGrabServer {

    private final int port;
    private volatile boolean running = false;

    // 线程池：CPU*2 的固定线程池，可根据需要调整为可配置
    private final ExecutorService pool = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

    // 在线客户端映射：key 可使用远端地址:端口，或后续登录后的 userId
    private final Map<String, ClientThread> clients = new ConcurrentHashMap<>();

    private final CourseRequestDispatcher dispatcher = new CourseRequestDispatcher(this::onClientClosed);

    public CourseGrabServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        running = true;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[CourseGrabServer] Listening on port " + port);
            while (running) {
                Socket client = server.accept();
                String key = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                ClientThread handler = new ClientThread(client, key, dispatcher);
                clients.put(key, handler);
                pool.submit(handler);
            }
        }
    }

    private void onClientClosed(String key) {
        clients.remove(key);
    }

    public static void main(String[] args) throws Exception {
        int port = ConfigManager.getSocketServerPort();
        new CourseGrabServer(port).start();
    }
}


