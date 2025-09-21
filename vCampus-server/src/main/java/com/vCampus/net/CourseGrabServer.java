package com.vCampus.net;

import com.vCampus.service.IChooseService;
import com.vCampus.common.ConfigManager;
import com.vCampus.service.LibraryService;
import com.vCampus.service.ServiceResult; // 确保导入 ServiceResult
import com.vCampus.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component
public class CourseGrabServer {

    private final int port;
    private volatile boolean running = false;
    private volatile ServerSocket serverSocket;
    
    @Autowired private IChooseService chooseService; 
    @Autowired private ISubjectService subjectService; 
    @Autowired private LibraryService libraryService; 

    
    @Autowired
    public CourseGrabServer(@Value("${socket.serverPort:9090}") int port) {
        this.port = port;
    }
    
    // 每门课独立的公平锁，保证同一门课的请求按到达顺序串行处理
    private final ConcurrentHashMap<String, ReentrantLock> subjectLocks = new ConcurrentHashMap<>();

    private ReentrantLock getSubjectLock(String subjectId) {
        return subjectLocks.computeIfAbsent(subjectId, k -> new ReentrantLock(true));
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
                String[] parts = line.split("\\|", -1); // 固定使用 \\| 进行分割
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

  ReentrantLock lock = getSubjectLock(subjectId);
  lock.lock();
  try {
  // 直接使用 chooseService.chooseSubject 返回的 ServiceResult 来构建响应
  ServiceResult result = this.chooseService.chooseSubject(studentId, subjectId); 
  if (result.isSuccess()) {
  out.println("OK|" + result.getMessage());
  out.flush();
  } else {
  out.println("FAIL|" + result.getMessage());
  out.flush();
  }
  } finally {
  lock.unlock();
  }
  return;
                }

            // ====== Library commands ======
            LibraryService lib = this.libraryService;
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
        // 此处的 main 方法直接运行，不经过 Spring Boot 的上下文，需要手动初始化 Service
        // 对于 Spring Boot 应用，通常由 ServerApplication.java 中的 @Bean 方法启动 CourseGrabServer
        // 如果 CourseGrabServer 作为独立进程运行，需要手动注入或创建 Service 实例
        // 考虑到您是在运行Spring Boot应用，这个 main 方法实际可能不会被直接调用
        System.out.println("如果 CourseGrabServer 由 Spring Boot 启动，请忽略此main方法的警告。");
        // new CourseGrabServer(port).start(); // 注释掉此行，避免在Spring环境下重复启动
    }
}