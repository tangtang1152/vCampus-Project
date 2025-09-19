package com.vCampus.net;

import com.vCampus.common.ConfigManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 选课Socket客户端封装
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
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            out.println("PING");
            String resp = in.readLine();
            if (resp == null) return new CourseGrabResult(false, "无响应");
            return parse(resp);
        } catch (Exception e) {
            return new CourseGrabResult(false, "连接失败: " + e.getMessage());
        }
    }

    public CourseGrabResult choose(String studentId, String subjectId) {
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            out.println("CHOOSE|" + studentId + "|" + subjectId);
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


