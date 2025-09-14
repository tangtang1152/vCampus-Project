package util; // 请确保这是你的实际包名

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement; // 添加这行
import java.sql.ResultSet;         // 添加这行
import java.sql.Statement;


public class DBUtil {
    
    // 数据库文件相对于项目根目录的路径
    private static final String DB_RELATIVE_PATH = "database/vCampus.accdb";
    private static String connectionString;

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
                System.err.println("请在项目根目录下创建 'database' 文件夹，并放入 'vCampus.accdb' 文件。");
            } else {
                System.out.println("数据库文件存在，准备连接...");
            }
            
            // 5. 构造UCanAccess的连接字符串
            connectionString = "jdbc:ucanaccess://" + dbAbsolutePath;

            // 6. 显式加载驱动（确保驱动已就绪）
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
        System.out.println("正在尝试建立数据库连接...");
        Connection conn = DriverManager.getConnection(connectionString);
        System.out.println("数据库连接成功！");
        return conn;
    }

    
    /**
     * 关闭数据库连接，释放资源
     * @param conn 要关闭的连接
     */
    
    
    public static void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                System.out.println("ResultSet 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 ResultSet 时发生错误:");
            e.printStackTrace();
        }
        
        try {
            if (pstmt != null) {
                pstmt.close();
                System.out.println("PreparedStatement 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 PreparedStatement 时发生错误:");
            e.printStackTrace();
        }
        
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Connection 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 Connection 时发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 关闭 Connection 和 PreparedStatement 资源
     * @param conn 数据库连接
     * @param pstmt 预处理语句
     */
    public static void closeResources(Connection conn, PreparedStatement pstmt) {
        closeResources(conn, pstmt, null);
    }

    /**
     * 仅关闭 Connection 资源
     * @param conn 数据库连接
     */
    public static void closeResources(Connection conn) {
        closeResources(conn, null, null);
    }
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("数据库连接已关闭。");
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时发生错误:");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 关闭 Connection、Statement 和 ResultSet 资源
     * @param conn 数据库连接
     * @param stmt 语句对象
     * @param rs 结果集
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                System.out.println("ResultSet 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 ResultSet 时发生错误:");
            e.printStackTrace();
        }
        
        try {
            if (stmt != null) {
                stmt.close();
                System.out.println("Statement 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 Statement 时发生错误:");
            e.printStackTrace();
        }
        
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Connection 已关闭。");
            }
        } catch (SQLException e) {
            System.err.println("关闭 Connection 时发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 关闭 Connection 和 Statement 资源
     * @param conn 数据库连接
     * @param stmt 语句对象
     */
    public static void closeResources(Connection conn, Statement stmt) {
        closeResources(conn, stmt, null);
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