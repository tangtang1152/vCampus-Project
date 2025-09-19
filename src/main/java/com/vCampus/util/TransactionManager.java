package com.vCampus.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理器
 * 统一管理数据库事务
 */
public class TransactionManager {
    
    /**
     * 在事务中执行操作
     */
    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        final int maxRetries = 3;
        int attempt = 0;
        while (true) {
            Connection conn = null;
            try {
                conn = DBUtil.getConnection();
                System.out.println("获取数据库连接成功");
                conn.setAutoCommit(false);
                System.out.println("开始事务");
                // 高隔离级别，配合条件更新，保障并发一致性
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

                T result = callback.doInTransaction(conn);
                conn.commit();
                System.out.println("事务提交成功");
                return result;
            } catch (SQLException e) {
                System.err.println("=== 数据库操作异常 ===");
                System.err.println("错误信息: " + e.getMessage());
                System.err.println("SQL状态: " + e.getSQLState());
                System.err.println("错误代码: " + e.getErrorCode());
                if (conn != null) {
                    try {
                        System.out.println("开始回滚事务");
                        conn.rollback();
                        System.out.println("事务回滚成功");
                    } catch (SQLException ex) {
                        System.err.println("回滚事务失败: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                // 对序列化冲突进行有限重试
                if (isSerializationConflict(e) && attempt < maxRetries) {
                    attempt++;
                    long backoff = (long) (50L * Math.pow(2, attempt));
                    System.err.println("检测到并发冲突，准备重试 第 " + attempt + " 次，延迟 " + backoff + "ms");
                    try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    // 进入下一轮重试
                    continue;
                }
                throw new RuntimeException("数据库操作失败", e);
            } catch (Exception e) {
                System.err.println("=== 业务操作异常 ===");
                System.err.println("错误信息: " + e.getMessage());
                if (conn != null) {
                    try {
                        System.out.println("开始回滚事务");
                        conn.rollback();
                        System.out.println("事务回滚成功");
                    } catch (SQLException ex) {
                        System.err.println("回滚事务失败: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                throw new RuntimeException("业务操作失败", e);
            } finally {
                if (conn != null) {
                    try {
                        if (!conn.isClosed()) {
                            conn.setAutoCommit(true);
                            conn.close();
                            System.out.println("数据库连接已关闭");
                        }
                    } catch (SQLException e) {
                        System.err.println("关闭连接失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static boolean isSerializationConflict(SQLException e) {
        if (e == null) return false;
        if ("40001".equals(e.getSQLState())) return true; // SQLState: serialization failure
        if (e.getErrorCode() == -4861) return true;        // UCanAccess 常见并发冲突码
        SQLException next = e.getNextException();
        while (next != null) {
            if ("40001".equals(next.getSQLState()) || next.getErrorCode() == -4861) return true;
            next = next.getNextException();
        }
        return false;
    }
    
    /**
     * 安全地在JavaFX线程中执行操作
     */
    public static void runLaterSafe(Runnable runnable) {
        try {
            if (javafx.application.Platform.isFxApplicationThread()) {
                runnable.run();
            } else {
                javafx.application.Platform.runLater(() -> {
                    try {
                        runnable.run();
                    } catch (IllegalStateException e) {
                        if (e.getMessage().contains("Timer already cancelled")) {
                            System.err.println("忽略Timer already cancelled错误，UI可能已关闭");
                        } else {
                            System.err.println("Platform.runLater执行失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        System.err.println("Platform.runLater执行异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (IllegalStateException e) {
            // JavaFX平台未启动或已关闭
            System.err.println("JavaFX平台不可用，无法执行UI更新: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("执行Platform.runLater失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 事务回调接口
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(Connection conn) throws Exception;
    }
}