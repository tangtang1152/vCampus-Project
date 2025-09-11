package com.vCampus.service; // 声明包名

// 导入DAO层和Entity层
import com.vCampus.dao.UserDao;    // 导入DAO，用于访问数据库
import com.vCampus.entity.Student; // 导入实体，作为数据传输载体
import java.sql.SQLException;      // 导入异常类

/**
 * 用户服务 (Service) 类
 * 职责：处理用户相关的核心业务逻辑。它是UI层和DAO层之间的桥梁。
 * 注意：这一层包含“做什么”的逻辑，但不关心“怎么做”（怎么做是DAO的事）。
 */
public class UserService {

    /**
     * 用户登录业务逻辑
     * @param username 用户名
     * @param password 密码
     * @return true 如果登录成功；false 如果用户名或密码错误。
     * 
     * 设计思路：
     * 1. 调用DAO层的方法进行纯粹的数据库验证。
     * 2. 在此处捕获DAO抛出的SQLException，并将其转换为更简单的布尔值或自定义业务异常。
     * 3. UI层不需要知道底层是数据库错误还是密码错误，它只需要知道“成功”或“失败”。
     * 4. 这里是未来可以添加更多逻辑的地方，比如：记录登录日志、检查账户是否被锁定等。
     */
    public static boolean login(String username, String password) {
        try {
            // 纯粹的委托：调用DAO完成验证，并返回结果
            return UserDao.validateUser(username, password);
        } catch (SQLException e) {
            // 记录异常（在实际项目中应使用日志框架如Log4j）
            e.printStackTrace();
            // 将数据库异常转换为业务逻辑的失败结果
            return false;
        }
    }

    /**
     * 用户注册业务逻辑
     * @param student 包含注册信息的Student对象
     * @return true 如果注册成功；false 如果注册失败（如学号已存在）。
     * 
     * 设计思路：
     * 1. 在调用DAO插入数据之前，这里可以添加业务规则校验。
     * 2. 例如：检查学号格式、密码强度、用户名是否已存在等。
     * 3. 目前直接调用DAO，后续可以扩展。
     */
    public static boolean register(Student student) {
        // 未来可以在这里添加业务校验逻辑
        // if (!isStudentIdValid(student.getId())) { ... }
        
        try {
            // 调用DAO执行实际的插入操作
            return UserDao.insertUser(student);
        } catch (SQLException e) {
            e.printStackTrace();
            // 注册失败，返回false
            return false;
        }
    }
    
    // 示例：一个业务规则校验方法（未来可实现）
    // private static boolean isStudentIdValid(int id) { ... }
}