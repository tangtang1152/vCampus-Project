package com.vCampus.net;

import com.vCampus.common.ConfigManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
        String payload = "BORROW|" + userId + "|" + bookId + "|" + days;
        return send(payload);
    }

    public CourseGrabResult renew(String userId, Integer recordId, int days) {
        String payload = "RENEW|" + userId + "|" + recordId + "|" + days;
        return send(payload);
    }

    public CourseGrabResult returnBook(String userId, Integer recordId, Integer bookId) {
        String payload = "RETURN|" + userId + "|" + recordId + "|" + bookId;
        return send(payload);
    }

    public CourseGrabResult reserve(String userId, Integer bookId) {
        String payload = "RESERVE|" + userId + "|" + bookId;
        return send(payload);
    }

    private CourseGrabResult send(String line) {
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            out.println(line);
            String resp = in.readLine();
            if (resp == null) return new CourseGrabResult(false, "无响应");
            String[] parts = resp.split("\\|", 2);
            if (parts.length == 0) return new CourseGrabResult(false, "非法响应");
            String code = parts[0];
            String msg = parts.length > 1 ? parts[1] : "";
            return new CourseGrabResult("OK".equalsIgnoreCase(code), msg);
        } catch (Exception e) {
            return new CourseGrabResult(false, "连接失败: " + e.getMessage());
        }
    }
}


