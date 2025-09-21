package com.vCampus.net;

import com.vCampus.service.IChooseService;
import com.vCampus.service.ServiceFactory;
import com.vCampus.common.ConfigManager;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceResult;
import com.vCampus.service.ISubjectService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
    private volatile ServerSocket serverSocket;

    // 每门课独立的公平锁，保证同一门课的请求按到达顺序串行处理
    private final ConcurrentHashMap<String, ReentrantLock> subjectLocks = new ConcurrentHashMap<>();

    private ReentrantLock getSubjectLock(String subjectId) {
        return subjectLocks.computeIfAbsent(subjectId, k -> new ReentrantLock(true));
    }

    public CourseGrabServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        running = true;
        serverSocket = new ServerSocket(port);
        System.out.println("[CourseGrabServer] Listening on port " + port);
        try {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    if (!running) { try { client.close(); } catch (Exception ignore) {} break; }
                    new Thread(() -> handleClient(client), "grab-client-" + client.getPort()).start();
                } catch (java.net.SocketException se) {
                    if (running) {
                        System.err.println("[CourseGrabServer] Socket exception: " + se.getMessage());
                    }
                    break;
                }
            }
        } finally {
            try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); } catch (Exception ignore) {}
            serverSocket = null;
        }
    }

    private void handleClient(Socket client) {
        try (Socket c = client;
             BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8)), true)) {

            try {
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
                if ("SHUTDOWN".equalsIgnoreCase(cmd)) {
                    if (parts.length >= 2 && ConfigManager.getSocketAdminToken().equals(parts[1])) {
                        out.println("OK|Server shutting down");
                        out.flush();
                        new Thread(() -> {
                            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                            running = false;
                            try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignore) {}
                            try { c.close(); } catch (Exception ignore) {}
                        }, "shutdown-thread").start();
                    } else {
                        out.println("FAIL|拒绝：无效令牌");
                    }
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
                    ISubjectService subjectService = ServiceFactory.getSubjectService();

                    ReentrantLock lock = getSubjectLock(subjectId);
                    lock.lock();
                    try {
                        boolean ok = chooseService.chooseSubject(studentId, subjectId);
                        if (ok) {
                            out.println("OK|选课成功");
                            out.flush();
                        } else {
                            String msg;
                            try {
                                var subject = subjectService.getSubjectById(subjectId);
                                if (subject != null && subject.getSubjectNum() != null && subject.getSubjectNum() <= 0) {
                                    msg = "选课失败：课程已满";
                                } else if (chooseService.isSubjectChosen(studentId, subjectId)) {
                                    msg = "选课失败：已选过该课";
                                } else {
                                    msg = "选课失败";
                                }
                            } catch (Exception ex) {
                                msg = "选课失败";
                            }
                            out.println("FAIL|" + msg);
                            out.flush();
                        }
                    } finally {
                        lock.unlock();
                    }
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
                out.flush();
                return;
            }
            if ("RENEW".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer recordId = parseIntSafe(parts[2]);
                int days = parts.length >= 4 ? parseIntSafe(parts[3], 30) : 30;
                ServiceResult res = lib.renewBorrowWithReason(userId, recordId, days, 1);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                out.flush();
                return;
            }
            if ("RETURN".equalsIgnoreCase(cmd)) {
                if (parts.length < 4) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer recordId = parseIntSafe(parts[2]);
                Integer bookId = parseIntSafe(parts[3]);
                ServiceResult res = lib.returnBookWithReason(userId, recordId, bookId);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                out.flush();
                return;
            }
            if ("RESERVE".equalsIgnoreCase(cmd)) {
                if (parts.length < 3) { out.println("FAIL|参数不足"); return; }
                String userId = parts[1];
                Integer bookId = parseIntSafe(parts[2]);
                ServiceResult res = lib.reserveBookWithReason(userId, bookId);
                out.println(res.isSuccess() ? ("OK|" + res.getMessage()) : ("FAIL|" + res.getMessage()));
                out.flush();
                return;
            }

                out.println("FAIL|未知指令");
                out.flush();
            } catch (Exception cmdEx) {
                try { out.println("FAIL|服务端异常: " + (cmdEx.getMessage() == null ? "unknown" : cmdEx.getMessage())); out.flush(); } catch (Exception ignore) {}
            }
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


