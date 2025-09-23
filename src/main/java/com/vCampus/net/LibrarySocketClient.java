package com.vCampus.net;

import com.vCampus.common.ConfigManager;
import com.vCampus.net.dto.SocketRequest;
import com.vCampus.net.dto.SocketResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LibrarySocketClient {

    private final String host;
    private final int port;

    public LibrarySocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static LibrarySocketClient fromConfig() {
        return new LibrarySocketClient(ConfigManager.getSocketServerHost(), ConfigManager.getSocketServerPort());
    }

    public CourseGrabResult borrow(String userId, Integer bookId, int days) {
        SocketRequest req = new SocketRequest("BORROW")
                .put("userId", userId)
                .put("bookId", String.valueOf(bookId))
                .put("days", String.valueOf(days));
        return send(req);
    }

    public CourseGrabResult renew(String userId, Integer recordId, int days) {
        SocketRequest req = new SocketRequest("RENEW")
                .put("userId", userId)
                .put("recordId", String.valueOf(recordId))
                .put("days", String.valueOf(days));
        return send(req);
    }

    public CourseGrabResult returnBook(String userId, Integer recordId, Integer bookId) {
        SocketRequest req = new SocketRequest("RETURN")
                .put("userId", userId)
                .put("recordId", String.valueOf(recordId))
                .put("bookId", String.valueOf(bookId));
        return send(req);
    }

    public CourseGrabResult reserve(String userId, Integer bookId) {
        SocketRequest req = new SocketRequest("RESERVE")
                .put("userId", userId)
                .put("bookId", String.valueOf(bookId));
        return send(req);
    }

    public CourseGrabResult payAllFines(String userId) {
        SocketRequest req = new SocketRequest("LIB_PAY_FINE").put("userId", userId);
        return send(req);
    }

    public CourseGrabResult payFineForRecord(String userId, Integer recordId) {
        SocketRequest req = new SocketRequest("LIB_PAY_FINE_FOR_RECORD")
                .put("userId", userId)
                .put("recordId", String.valueOf(recordId));
        return send(req);
    }

    private CourseGrabResult send(SocketRequest req) {
        try (Socket s = new Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), ConfigManager.getSocketConnectTimeoutMs());
            s.setSoTimeout(ConfigManager.getSocketSoTimeoutMs());
            try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
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


