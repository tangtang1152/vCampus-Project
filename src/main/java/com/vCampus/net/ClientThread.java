package com.vCampus.net;

import com.vCampus.net.dto.SocketRequest;
import com.vCampus.net.dto.SocketResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 每个客户端连接的处理线程。
 * 负责从对象流读取请求并写回响应；由服务器端线程池调度执行。
 */
public class ClientThread implements Runnable {
    private final Socket socket;
    private final String clientKey;
    private final CourseRequestDispatcher dispatcher;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public ClientThread(Socket socket, String clientKey, CourseRequestDispatcher dispatcher) {
        this.socket = socket;
        this.clientKey = clientKey;
        this.dispatcher = dispatcher;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void close() {
        active.set(false);
        try { socket.close(); } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try (Socket s = socket;
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

            while (active.get()) {
                Object obj = in.readObject();
                if (obj == null) break;
                if (!(obj instanceof SocketRequest req)) {
                    out.writeObject(new SocketResponse(false, "非法请求对象"));
                    out.flush();
                    continue;
                }
                SocketResponse resp = dispatcher.handle(req);
                out.writeObject(resp);
                out.flush();
            }
        } catch (Exception e) {
            // 简单记录，详细日志由上层控制
        } finally {
            dispatcher.onClientClosed(clientKey);
        }
    }
}


