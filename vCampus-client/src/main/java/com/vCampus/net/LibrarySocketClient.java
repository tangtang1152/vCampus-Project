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
        String line = "BORROW|" + userId + "|" + bookId + "|" + days;
        return send(line, 8000);
    }

    public CourseGrabResult reserve(String userId, Integer bookId) {
        String line = "RESERVE|" + userId + "|" + bookId;
        return send(line, 5000);
    }

    public CourseGrabResult renew(String userId, Integer recordId, int days) {
        String line = "RENEW|" + userId + "|" + recordId + "|" + days;
        return send(line, 8000);
    }

    public CourseGrabResult returnBook(String userId, Integer recordId, Integer bookId) {
        String line = "RETURN|" + userId + "|" + recordId + "|" + bookId;
        return send(line, 5000);
    }

    private CourseGrabResult send(String payload, int timeoutMs) {
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            s.setSoTimeout(timeoutMs);
            out.println(payload);
            out.flush();
            String resp = in.readLine();
            if (resp == null) return new CourseGrabResult(false, "无响应");
            return parse(resp);
        } catch (Exception e) {
            return new CourseGrabResult(false, "连接失败: " + e.getMessage());
        }
    }

    private CourseGrabResult parse(String line) {
        String[] parts = line.split("\\|", 2);
        if (parts.length == 0) return new CourseGrabResult(false, "非法响应");
        String code = parts[0];
        String msg = parts.length > 1 ? parts[1] : "";
        return new CourseGrabResult("OK".equalsIgnoreCase(code), msg);
    }
}



