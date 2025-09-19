package com.vCampus.net;

import com.vCampus.service.IChooseService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.common.ConfigManager;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 简单的Socket服务器：处理选课与图书馆请求
 * 协议：单行文本，使用'|'分隔
 * 选课：   CHOOSE|studentId|subjectId\n
 * 借书：   BORROW|userId|bookId|days?\n   （days可选，默认30）
 * 续借：   RENEW|userId|recordId|days?\n（days可选，默认30）
 * 还书：   RETURN|userId|recordId|bookId\n
 * 预约：   RESERVE|userId|bookId\n
 * 响应：OK|message 或 FAIL|message
 */
public class CourseGrabServer {

    private final int port;
    private volatile boolean running = false;

    public CourseGrabServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        running = true;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[CourseGrabServer] Listening on port " + port);
            while (running) {
                Socket client = server.accept();
                new Thread(() -> handleClient(client), "grab-client-" + client.getPort()).start();
            }
        }
    }

    private void handleClient(Socket client) {
        try (Socket c = client;
             BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8)), true)) {

            String line = in.readLine();
            if (line == null || line.isBlank()) {
                out.println("FAIL|空请求");
                return;
            }
            String[] parts = line.split("\\|", -1);
            String cmd = parts[0];
            if ("PING".equalsIgnoreCase(cmd)) {
                out.println("OK|PONG");
                return;
            }
            if ("CHOOSE".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) {
                    out.println("FAIL|参数不足");
                    return;
                }
                String studentId = parts[1];
                String subjectId = parts[2];
                IChooseService chooseService = ServiceFactory.getChooseService();
                boolean ok = chooseService.chooseSubject(studentId, subjectId);
                out.println(ok ? "OK|选课成功" : "FAIL|选课失败：可能已满或已选过");
                return;
            }

            // ====== Library commands ======
            LibraryService lib = ServiceFactory.getLibraryService();
            if ("BORROW".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer bookId = parseIntSafe(parts[2]);
                int days = parts.length >= 4 ? parseIntSafe(parts[3], 30) : 30;
                ServiceResult res = lib.borrowBookWithReason(userId, bookId, days);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                return;
            }
            if ("RENEW".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer recordId = parseIntSafe(parts[2]);
                int days = parts.length >= 4 ? parseIntSafe(parts[3], 30) : 30;
                ServiceResult res = lib.renewBorrowWithReason(userId, recordId, days, 1);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                return;
            }
            if ("RETURN".equalsIgnoreCase(cmd)) {
                if (parts.length < 4) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer recordId = parseIntSafe(parts[2]);
                Integer bookId = parseIntSafe(parts[3]);
                ServiceResult res = lib.returnBookWithReason(userId, recordId, bookId);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                return;
            }
            if ("RESERVE".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer bookId = parseIntSafe(parts[2]);
                ServiceResult res = lib.reserveBookWithReason(userId, bookId);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                return;
            }

            out.println("FAIL|未知指令");
        } catch (Exception e) {
            System.err.println("[CourseGrabServer] 处理客户端异常: " + e.getMessage());
        }
    }

    private Integer parseIntSafe(String s) {
        try { return Integer.valueOf(s); } catch (Exception e) { return null; }
    }
    private int parseIntSafe(String s, int defVal) {
        try { return Integer.parseInt(s); } catch (Exception e) { return defVal; }
    }

    public static void main(String[] args) throws Exception {
        int port = ConfigManager.getSocketServerPort();
        new CourseGrabServer(port).start();
    }
}


