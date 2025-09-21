package com.vCampus.util;

import javafx.application.Platform;

/**
 * 客户端轻量 TransactionManager：
 * - 提供 runLaterSafe 封装 JavaFX 主线程切换
 * - executeInTransaction 保持服务端相同签名，传入 null Connection
 */
public class TransactionManager {

    public static void runLaterSafe(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    public static <T> T executeInTransaction(TransactionCallback<T> cb) {
        try {
            return cb.doInTransaction(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(java.sql.Connection conn) throws Exception;
    }
}


