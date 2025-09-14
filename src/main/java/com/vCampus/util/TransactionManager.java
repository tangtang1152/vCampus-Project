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
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            T result = callback.doInTransaction(conn);
            
            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("回滚事务失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            System.err.println("事务执行失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库操作失败", e);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("回滚事务失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            System.err.println("事务执行失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("业务操作失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("关闭连接失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
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