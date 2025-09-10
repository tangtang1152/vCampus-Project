

package com.vCampus.dao; // 声明包名，符合分层结构

//导入所需的类
import com.vCampus.entity.Student; // 导入实体类，用于传递数据
import com.vCampus.util.DBUtil;    // 导入数据库工具类，获取连接
import java.sql.*;                 // 导入JDBC相关类，用于数据库操作

/**
* 用户数据访问对象 (Data Access Object) 类
* 职责：负责所有与用户表(Students)相关的底层数据库操作（CRUD）。
* 注意：这个类只关心如何执行SQL，不包含任何业务逻辑（比如密码加密、规则判断等）。
*/
public class UserDao {

 /**
  * 验证用户凭据（用于登录）
  * @param username 用户名
  * @param password 密码
  * @return true 如果用户名和密码匹配数据库中的一条记录；否则返回 false。
  * @throws SQLException 如果数据库操作发生错误，将异常抛给调用者（Service层）处理。
  * 
  * 设计思路：
  * 1. 使用 PreparedStatement 防止SQL注入攻击。
  * 2. 使用 try-with-resources 语句自动关闭连接和语句，避免资源泄漏。
  * 3. 返回布尔值，只告诉调用者“是否存在”，不返回具体数据，保证安全性。
  */
 public static boolean validateUser(String username, String password) throws SQLException {
     // 定义SQL查询语句，使用占位符(?)
     String sql = "SELECT COUNT(*) FROM Students WHERE name = ? AND key = ?";
     
     // 获取数据库连接并创建预编译语句
     try (Connection conn = DBUtil.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
         // 设置SQL语句中的参数（替换占位符）
         pstmt.setString(1, username); // 第一个问号替换为用户名
         pstmt.setString(2, password); // 第二个问号替换为密码
         
         // 执行查询并获取结果集
         try (ResultSet rs = pstmt.executeQuery()) {
             // 移动结果集的光标到第一行
             if (rs.next()) {
                 // 获取第一列的值（COUNT(*)的结果）
                 int count = rs.getInt(1);
                 // 如果count大于0，说明找到了匹配的用户
                 return count > 0;
             }
         }
     }
     // 如果没找到记录或发生异常，返回false
     return false;
 }

 /**
  * 向数据库插入一个新用户（用于注册）
  * @param student 包含用户信息（id, name, password）的Student对象
  * @return true 如果插入成功（影响的行数>0）；否则返回 false。
  * @throws SQLException 如果数据库操作发生错误（如主键冲突、连接失败等）。
  * 
  * 设计思路：
  * 1. 接收一个完整的Student对象，使接口更清晰。
  * 2. 同样使用PreparedStatement防止注入。
  * 3. 返回操作是否成功，而不是抛出具体异常，让Service层决定如何向用户反馈。
  */
 public static boolean insertUser(Student student) throws SQLException {
     // SQL插入语句
     String sql = "INSERT INTO Students (studentId, studentName, key, gender) VALUES (?, ?, ?, ?)";
     
     try (Connection conn = DBUtil.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
         // 从Student对象中获取属性并设置到SQL中
         pstmt.setInt(1, student.getStudentId());       // 学号
         pstmt.setString(2, student.getStudentName());  // 姓名
         pstmt.setString(3, student.getKey());   // 密码（数据库中字段名为key）
         pstmt.setString(4, "男");               // 性别（这里写死了，应从界面或对象中获取）
         
         // 执行更新操作，并返回受影响的行数
         int affectedRows = pstmt.executeUpdate();
         // 如果受影响的行数大于0，说明插入成功
         return affectedRows > 0;
     }
 }
 
 // 后续可以在这里添加更多方法，如：
 // getUserById(int id), updateUser(Student student), deleteUser(int id) 等。
}