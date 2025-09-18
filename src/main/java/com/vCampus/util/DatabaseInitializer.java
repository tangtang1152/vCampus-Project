package com.vCampus.util;

import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库初始化工具类
 * 用于初始化RBAC权限控制系统的数据库表
 */
public class DatabaseInitializer {
    
    /**
     * 初始化RBAC数据库表
     */
    public static boolean initializeRBACTables() {
        try (Connection conn = DBUtil.getConnection()) {
            // 读取SQL脚本
            String sqlScript = readSQLScript("database/init_rbac_tables.sql");
            if (sqlScript == null) {
                System.err.println("无法读取RBAC初始化脚本");
                return false;
            }
            
            // 执行SQL脚本
            String[] statements = sqlScript.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty() && !statement.startsWith("--")) {
                        try {
                            stmt.execute(statement);
                            System.out.println("执行成功: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                        } catch (Exception e) {
                            // 忽略表已存在的错误
                            if (!e.getMessage().contains("already exists") && 
                                !e.getMessage().contains("already exists") &&
                                !e.getMessage().contains("user lacks privilege")) {
                                System.err.println("执行SQL语句失败: " + statement);
                                System.err.println("错误: " + e.getMessage());
                            } else {
                                System.out.println("跳过已存在的对象: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                            }
                        }
                    }
                }
            }
            
            // 初始化默认角色和权限
            System.out.println("开始初始化默认角色和权限...");
            IPermissionService permissionService = ServiceFactory.getPermissionService();
            permissionService.initializeDefaultRolesAndPermissions();
            
            System.out.println("RBAC数据库表初始化完成");
            return true;
            
        } catch (Exception e) {
            System.err.println("初始化RBAC数据库表失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 读取SQL脚本文件
     */
    private static String readSQLScript(String resourcePath) {
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            return script.toString();
            
        } catch (Exception e) {
            System.err.println("读取SQL脚本失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查RBAC表是否存在
     */
    public static boolean checkRBACTablesExist() {
        try (Connection conn = DBUtil.getConnection()) {
            String[] tables = {"roles", "permissions", "role_permissions", "user_roles"};
            
            for (String table : tables) {
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    // 如果查询成功，说明表存在
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
            
        } catch (Exception e) {
            System.err.println("检查RBAC表存在性失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 重置RBAC数据（删除所有数据并重新初始化）
     */
    public static boolean resetRBACData() {
        try (Connection conn = DBUtil.getConnection()) {
            String[] tables = {"user_roles", "role_permissions", "permissions", "roles"};
            
            try (var stmt = conn.createStatement()) {
                for (String table : tables) {
                    try {
                        stmt.execute("DELETE FROM " + table);
                    } catch (Exception e) {
                        System.err.println("清空表 " + table + " 失败: " + e.getMessage());
                    }
                }
            }
            
            // 重新初始化
            return initializeRBACTables();
            
        } catch (Exception e) {
            System.err.println("重置RBAC数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
