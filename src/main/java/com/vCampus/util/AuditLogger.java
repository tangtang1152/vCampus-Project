package com.vCampus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单审计日志：将关键操作记录到 logs/audit.log
 */
public final class AuditLogger {
    private static final Path LOG_DIR = Path.of("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("audit.log");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AuditLogger() {}

    public static synchronized void log(String event, String details) {
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }
            String line = String.format("%s | %s | %s%n", LocalDateTime.now().format(TS), safe(event), safe(details));
            Files.writeString(LOG_FILE, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("\r|\n", " ");
    }
}


