package com.vCampus.net;

import com.vCampus.common.ConfigManager;
import com.vCampus.net.dto.SocketRequest;
import com.vCampus.net.dto.SocketResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 选课Socket客户端封装（对象流）。
 */
public class CourseGrabClient {

    private final String host;
    private final int port;

    public CourseGrabClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static CourseGrabClient fromConfig() {
        return new CourseGrabClient(ConfigManager.getSocketServerHost(), ConfigManager.getSocketServerPort());
    }

    public CourseGrabResult ping() {
        try (Socket s = new Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(ConfigManager.getSocketSoTimeoutMs());
            try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.writeObject(new SocketRequest("PING"));
            out.flush();
            Object obj = in.readObject();
            if (!(obj instanceof SocketResponse resp)) return new CourseGrabResult(false, "非法响应");
            return new CourseGrabResult(resp.isSuccess(), resp.getMessage());
            }
        } catch (Exception e) {
            return new CourseGrabResult(false, "连接失败: " + e.getMessage());
        }
    }

    public CourseGrabResult choose(String studentId, String subjectId) {
        try (Socket s = new Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(ConfigManager.getSocketSoTimeoutMs());
            try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            SocketRequest req = new SocketRequest("CHOOSE")
                    .put("studentId", studentId)
                    .put("subjectId", subjectId);
            out.writeObject(req);
            out.flush();
            Object obj = in.readObject();
            if (!(obj instanceof SocketResponse resp)) return new CourseGrabResult(false, "非法响应");
            return new CourseGrabResult(resp.isSuccess(), resp.getMessage());
            }
        } catch (Exception e) {
            return new CourseGrabResult(false, "连接失败: " + e.getMessage());
        }
    }
}


