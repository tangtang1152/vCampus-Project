package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    
    // UCanAccess 的 JDBC 连接字符串模板
    // 格式：jdbc:ucanaccess://<path_to_access_file>
    // !! 请将以下路径替换为你本地数据库文件的绝对路径 !!
    // 注意：使用正斜杠 `/` 或双反斜杠 `\\`
    // 例如： "D:/Projects/vCampus/database/vCampus.accdb"
    // 或者： "D:\\Projects\\vCampus\\database\\vCampus.accdb"
    private static final String DB_PATH = "D:\\Java\\vCampus-Project\\database\\vCampus.accdb"; // <-- 修改这行！
    private static final String CONNECTION_STRING = "jdbc:ucanaccess://" + DB_PATH;

    // 静态块：在类加载时自动执行，用于加载数据库驱动
    static {
        try {
            // 告诉Java：“请去lib文件夹里找到并加载UCanAccess的驱动类”
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            System.out.println("UCanAccess驱动加载成功。");
        } catch (ClassNotFoundException e) {
            // 如果找不到驱动，多半是JAR包没导入Build Path
            System.err.println("致命错误：UCanAccess驱动未找到！");
            System.err.println("请检查lib文件夹中的jar包是否已添加到Build Path。");
            e.printStackTrace(); // 打印详细的错误信息，这是调试的黄金法则
        }
    }

    /**
     * 获取一个到Access数据库的连接
     * @return Connection 对象，如果获取失败则返回null
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // DriverManager会根据上面加载的驱动，尝试建立连接
            conn = DriverManager.getConnection(CONNECTION_STRING);
            System.out.println("恭喜！数据库连接成功！");
        } catch (SQLException e) {
            System.err.println("数据库连接失败！请检查：");
            System.err.println("1. 数据库文件路径是否正确？当前路径: " + DB_PATH);
            System.err.println("2. 数据库文件是否存在？");
            System.err.println("3. 数据库文件是否被其他程序（如Access本身）独占打开？");
            e.printStackTrace(); // 打印详细的错误栈，它会告诉你问题出在哪一行
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
                System.out.println("数据库连接已关闭。");
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时发生错误！");
                e.printStackTrace();
            }
        }
    }

    // 测试主方法
    public static void main(String[] args) {
        System.out.println("开始测试数据库连接...");
        System.out.println("连接字符串: " + CONNECTION_STRING);
        
        // 测试流程：获取连接 -> 进行各种数据库操作 -> 关闭连接
        Connection testConn = getConnection(); // 获取连接
        
        // 这里将来会进行：查询、插入、删除等操作...
        // if (testConn != null) { ... }
        
        closeConnection(testConn); // 关闭连接
        System.out.println("测试结束。");
    }
}