package com.vCampus.util; // 请确保这是你的实际包名

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    
    // 调试模式标志，控制详细输出
    private static final boolean DEBUG_MODE = false;
    
    // 数据库文件相对于项目根目录的路径
    private static final String DB_RELATIVE_PATH = "src\\main\\resources\\database\\vCampus.accdb";
    private static String connectionString;
    private static Properties connectionProperties;

    // 静态初始化块，在类加载时执行一次
    static {
        try {
            // 1. 动态获取项目在当前电脑上的绝对路径
            String projectRootPath = System.getProperty("user.dir");
            // 2. 拼接出数据库文件的绝对路径
            String dbAbsolutePath = projectRootPath + File.separator + DB_RELATIVE_PATH;
            
            // 3. 打印最终路径，这是调试的关键！
            System.out.println("尝试从以下路径连接数据库: " + dbAbsolutePath);
            
            // 4. 检查数据库文件是否存在
            File dbFile = new File(dbAbsolutePath);
            if (!dbFile.exists()) {
                System.err.println("错误: 数据库文件不存在！请检查路径。");
                System.err.println("请在src\\main\\resources下创建 'database' 文件夹，并放入 'vCampus.accdb' 文件。");
            } else {
                System.out.println("数据库文件存在，准备连接...");
            }
            
            // 5. 构造UCanAccess的连接字符串
            connectionString = "jdbc:ucanaccess://" + dbAbsolutePath;

            // 6. 配置连接属性来消除警告
            
            //// 1. 设置系统属性（最早执行）
            System.setProperty("ucanaccess.autocreate", "false");
            System.setProperty("ucanaccess.functions", "false");
            
            //// 2. 设置日志级别
            java.util.logging.Logger.getLogger("org.hsqldb").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("net.ucanaccess").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.healthmarketscience.jackcess").setLevel(java.util.logging.Level.SEVERE);
            
            //// 3. 配置连接属性
            connectionProperties = new Properties();
            connectionProperties.put("ignoreFunctions", "true");
            connectionProperties.put("showSchema", "false");
            connectionProperties.put("sysschema", "false");
            connectionProperties.put("autocreate", "false");

            // 7. 显式加载驱动（确保驱动已就绪）
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            System.out.println("UCanAccess驱动加载成功。");
            
        } catch (ClassNotFoundException e) {
            System.err.println("致命错误: UCanAccess驱动未找到！");
            System.err.println("请检查pom.xml中的Maven依赖配置是否正确。");
            e.printStackTrace();
        }
    }

    /**
     * 获取一个到Access数据库的连接
     * @return Connection 对象
     * @throws SQLException 如果连接失败则抛出异常
     */
    public static Connection getConnection() throws SQLException {
        // 减少重复输出，只在调试模式下输出
        if (DEBUG_MODE) {
            System.out.println("正在尝试建立数据库连接...");
        }
        Connection conn = DriverManager.getConnection(connectionString, connectionProperties);
        if (DEBUG_MODE) {
            System.out.println("数据库连接成功！");
        }
        return conn;
    }

    /**
     * 关闭数据库连接，释放资源
     * @param conn 要关闭的连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                if (DEBUG_MODE) {
                    System.out.println("数据库连接已关闭。");
                }
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时发生错误:");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 内置测试方法 - 直接运行这个类来测试连接
     */
    public static void main(String[] args) {
        System.out.println("========== 开始测试 DBUtil ==========");
        
        Connection testConn = null;
        try {
            // 尝试获取连接
            testConn = getConnection();
            // 如果走到这里，说明连接成功
            System.out.println("✅ DBUtil 测试成功！连接已建立。");
            
        } catch (SQLException e) {
            System.err.println("❌ DBUtil 测试失败！连接错误。");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            // 确保连接被关闭
            if (testConn != null) {
                closeConnection(testConn);
            }
        }
        System.out.println("========== 测试结束 ==========");
    }
}