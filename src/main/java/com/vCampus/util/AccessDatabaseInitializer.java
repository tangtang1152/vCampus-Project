package com.vCampus.util;

import com.vCampus.service.IPermissionService;
import com.vCampus.service.ServiceFactory;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Access数据库初始化工具类
 * 专门处理Access数据库的兼容性问题
 */
public class AccessDatabaseInitializer {
    
    /**
     * 初始化RBAC数据库表（Access数据库专用）
     */
    public static boolean initializeRBACTables() {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("开始创建RBAC数据库表...");
            
            // 创建角色表
            createTableIfNotExists(conn, "roles", 
                "CREATE TABLE roles (" +
                "role_id AUTOINCREMENT PRIMARY KEY, " +
                "role_name VARCHAR(50) NOT NULL, " +
                "role_code VARCHAR(50) NOT NULL, " +
                "description MEMO, " +
                "created_at DATETIME, " +
                "updated_at DATETIME" +
                ")"
            );
            
            // 创建权限表
            createTableIfNotExists(conn, "permissions", 
                "CREATE TABLE permissions (" +
                "permission_id AUTOINCREMENT PRIMARY KEY, " +
                "permission_name VARCHAR(100) NOT NULL, " +
                "permission_code VARCHAR(100) NOT NULL, " +
                "resource VARCHAR(50) NOT NULL, " +
                "action VARCHAR(50) NOT NULL, " +
                "description MEMO, " +
                "created_at DATETIME, " +
                "updated_at DATETIME" +
                ")"
            );
            
            // 创建角色权限关联表
            createTableIfNotExists(conn, "role_permissions", 
                "CREATE TABLE role_permissions (" +
                "role_id INTEGER NOT NULL, " +
                "permission_id INTEGER NOT NULL, " +
                "created_at DATETIME, " +
                "PRIMARY KEY (role_id, permission_id)" +
                ")"
            );
            
            // 创建用户角色关联表
            createTableIfNotExists(conn, "user_roles", 
                "CREATE TABLE user_roles (" +
                "user_role_id AUTOINCREMENT PRIMARY KEY, " +
                "user_id INTEGER NOT NULL, " +
                "role_id INTEGER NOT NULL, " +
                "role_code VARCHAR(50) NOT NULL, " +
                "assigned_date DATETIME, " +
                "expire_date DATETIME, " +
                "is_active YESNO DEFAULT TRUE, " +
                "assigned_by VARCHAR(100), " +
                "created_at DATETIME, " +
                "updated_at DATETIME" +
                ")"
            );
            
            // 注意：UCanAccess驱动不支持某些索引创建功能，跳过索引创建
            // 对于小型应用，索引不是必需的，查询性能仍然可以接受
            System.out.println("跳过索引创建（UCanAccess驱动限制）");
            
            System.out.println("RBAC数据库表创建完成");
            
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
     * 检查表是否存在
     */
    private static boolean tableExists(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 如果表不存在则创建
     */
    private static void createTableIfNotExists(Connection conn, String tableName, String createSQL) {
        try {
            if (!tableExists(conn, tableName)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createSQL);
                    System.out.println("创建表成功: " + tableName);
                }
            } else {
                System.out.println("表已存在，跳过: " + tableName);
            }
        } catch (Exception e) {
            System.err.println("创建表失败: " + tableName + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查索引是否存在
     */
    private static boolean indexExists(Connection conn, String indexName) {
        try (Statement stmt = conn.createStatement()) {
            // Access数据库检查索引的方法
            stmt.executeQuery("SELECT * FROM MSysObjects WHERE Name='" + indexName + "' AND Type=5");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 如果索引不存在则创建
     */
    private static void createIndexIfNotExists(Connection conn, String indexName, String createSQL) {
        try {
            if (!indexExists(conn, indexName)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createSQL);
                    System.out.println("创建索引成功: " + indexName);
                }
            } else {
                System.out.println("索引已存在，跳过: " + indexName);
            }
        } catch (Exception e) {
            System.err.println("创建索引失败: " + indexName + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 检查RBAC表是否存在
     */
    public static boolean checkRBACTablesExist() {
        try (Connection conn = DBUtil.getConnection()) {
            String[] tables = {"roles", "permissions", "role_permissions", "user_roles"};
            
            for (String table : tables) {
                if (!tableExists(conn, table)) {
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
}
