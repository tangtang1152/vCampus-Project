package com.vCampus.test;

import com.vCampus.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        System.out.println("========== 数据库连接测试 ==========");
        try {
            Connection conn = DBUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ 数据库连接成功！");
                System.out.println("数据库URL: " + conn.getMetaData().getURL());
                conn.close();
            } else {
                System.out.println("❌ 数据库连接失败");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接异常: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========== 测试结束 ==========");
    }
}