package com.vCampus.util;

import java.sql.*;

/**
 * 数据库验证工具
 */
public class DBValidator {
    
    /**
     * 验证表是否存在（通过尝试查询表来判断，而不是查询系统表）
     */
    public static boolean tableExists(String tableName) {
        // 改为使用一个简单的查询，如果表不存在会抛出异常
        String sql = "SELECT COUNT(*) FROM " + tableName;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 尝试执行查询，如果表不存在会抛出异常
            stmt.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            // 如果抛出异常，说明表可能不存在
            return false;
        }
    }
    
    /**
     * 获取表中的记录数
     */
    public static int getRecordCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("获取记录数时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * 打印表结构
     */
    public static void printTableStructure(String tableName) {
        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 0"; // 不返回数据，只返回元数据
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            System.out.println("表 " + tableName + " 的结构:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("列 " + i + ": " + metaData.getColumnName(i) + 
                                 " (" + metaData.getColumnTypeName(i) + ")");
            }
        } catch (SQLException e) {
            System.err.println("获取表结构时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印表中的所有数据（用于调试）
     */
    public static void printTableData(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            System.out.println("表 " + tableName + " 的数据:");
            
            // 打印列名
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();
            
            // 打印数据
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("获取表数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试数据库功能
     */
    public static void testDatabase() {
        System.out.println("========== 数据库验证测试 ==========");
        
        // 检查表是否存在
        System.out.println("tbl_user 表存在: " + tableExists("tbl_user"));
        System.out.println("tbl_student 表存在: " + tableExists("tbl_student"));
        
        // 获取记录数
        System.out.println("tbl_user 记录数: " + getRecordCount("tbl_user"));
        System.out.println("tbl_student 记录数: " + getRecordCount("tbl_student"));
        
        // 打印表结构
        printTableStructure("tbl_user");
        printTableStructure("tbl_student");
        
        // 打印表数据（用于调试）
        printTableData("tbl_user");
        printTableData("tbl_student");
        
        System.out.println("========== 验证测试结束 ==========");
    }
    
    public static void main(String[] args) {
        testDatabase();
    }
}