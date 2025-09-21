package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Admin; // 确保导入 Admin 实体类
import com.vCampus.service.IAdminService; // 确保导入 IAdminService 接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 RESTful API 控制器
 * 处理与管理员信息相关的 HTTP 请求
 */
@RestController // 标记这是一个 RESTful 控制器
@RequestMapping("/api/v1/admins") // 定义所有接口的基路径
public class AdminController {

    // 自动注入 IAdminService 的实现类
    @Autowired
    private IAdminService adminService;

    /**
     * 获取所有管理员信息列表。
     * GET /api/v1/admins
     *
     * @return 包含所有管理员的 ApiResponse
     */
    @GetMapping // 对应客户端 AdminServiceImpl.doGetAll() -> /admins
    public ApiResponse<List<Admin>> listAll() {
        List<Admin> admins = adminService.getAll();
        return ApiResponse.ok(admins);
    }

    /**
     * 根据管理员工号 (adminId) 获取单个管理员的完整信息（包含关联的用户信息）。
     * GET /api/v1/admins/{adminId}
     *
     * @param adminId 管理员工号
     * @return 包含管理员信息的 ApiResponse，如果不存在则返回 null
     */
    @GetMapping("/{adminId}") // 对应客户端 AdminServiceImpl.doGetBySelfId() -> /admins/{adminId}
    public ApiResponse<Admin> get(@PathVariable String adminId) {
        Admin admin = adminService.getAdminFull(adminId); // 使用 getAdminFull 获取完整信息
        return ApiResponse.ok(admin);
    }

    /**
     * 检查管理员工号是否存在。
     * GET /api/v1/admins/exists/{adminId}
     *
     * @param adminId 管理员工号
     * @return 包含布尔值的 ApiResponse
     */
    @GetMapping("/exists/{adminId}") // 对应客户端 AdminServiceImpl.doExists() -> /admins/exists/{adminId}
    public ApiResponse<Boolean> exists(@PathVariable String adminId) {
        boolean exists = adminService.exists(adminId);
        return ApiResponse.ok(exists);
    }

    /**
     * 根据用户ID (userId) 获取管理员信息。
     * GET /api/v1/admins/by-user/{userId}
     *
     * @param userId 用户ID
     * @return 包含管理员信息的 ApiResponse，如果不存在则返回 null
     */
    @GetMapping("/by-user/{userId}") // 对应客户端 LoginController.onLogin() 中调用的 ServiceFactory.getAdminService().getByUserId()
    public ApiResponse<Admin> byUser(@PathVariable Integer userId) {
        Admin admin = adminService.getByUserId(userId);
        return ApiResponse.ok(admin);
    }

    /**
     * 创建新的管理员。
     * POST /api/v1/admins
     *
     * @param admin 待创建的管理员对象
     * @return 包含布尔值表示操作是否成功的 ApiResponse
     */
    @PostMapping // 对应客户端 AdminServiceImpl.doAdd() -> /admins
    public ApiResponse<Boolean> create(@RequestBody Admin admin) {
        // 注意：这里的 create 方法通常只用于创建 admin 记录，而用户账户的创建可能在 UserService 中进行。
        // 根据您的服务层实现，adminService.add(admin) 内部可能已经处理了用户表的关联创建。
        boolean success = adminService.add(admin);
        return ApiResponse.ok(success);
    }

    /**
     * 更新管理员信息。
     * PUT /api/v1/admins/{adminId}
     *
     * @param adminId 待更新管理员工号
     * @param admin   包含更新信息的管理员对象
     * @return 包含布尔值表示操作是否成功的 ApiResponse
     */
    @PutMapping("/{adminId}") // 对应客户端 AdminServiceImpl.doUpdate() -> /admins/{adminId}
    public ApiResponse<Boolean> update(@PathVariable String adminId, @RequestBody Admin admin) {
        admin.setAdminId(adminId); // 确保路径变量中的ID与请求体中的ID一致
        // 根据您服务层，adminService.update(admin) 可能同时更新 tbl_admin 和 tbl_user
        boolean success = adminService.update(admin);
        return ApiResponse.ok(success);
    }

    /**
     * 删除管理员。
     * DELETE /api/v1/admins/{adminId}
     *
     * @param adminId 待删除管理员工号
     * @return 包含布尔值表示操作是否成功的 ApiResponse
     */
    @DeleteMapping("/{adminId}") // 对应客户端 AdminServiceImpl.doDelete() -> /admins/{adminId}
    public ApiResponse<Boolean> delete(@PathVariable String adminId) {
        // 根据您服务层，adminService.delete(adminId) 应该同时删除 tbl_admin 和 tbl_user
        boolean success = adminService.delete(adminId);
        return ApiResponse.ok(success);
    }

    // --- 以下是 Client AdminServiceImpl 中未直接调用的接口，但 Server AdminDaoImpl 提供了对应方法 ---
    // 这些接口可能在其他地方被调用，或者作为管理面板的内部API
    // 如果不需要暴露，可以不写对应的 Controller 方法

    /**
     * 仅更新管理员表 (tbl_admin) 中的信息。
     * PUT /api/v1/admins/only/{adminId}
     *
     * @param adminId 待更新管理员工号
     * @param admin   包含更新信息的管理员对象
     * @return 包含布尔值表示操作是否成功的 ApiResponse
     */
    @PutMapping("/only/{adminId}") // 对应客户端 AdminServiceImpl.updateAdminOnly() -> /admins/only/{adminId}
    public ApiResponse<Boolean> updateOnly(@PathVariable String adminId, @RequestBody Admin admin) {
        admin.setAdminId(adminId);
        boolean success = adminService.updateAdminOnly(admin); // 只更新 admin 表
        return ApiResponse.ok(success);
    }

    /**
     * 仅删除管理员表 (tbl_admin) 中的记录。
     * DELETE /api/v1/admins/only/{adminId}
     *
     * @param adminId 待删除管理员工号
     * @return 包含布尔值表示操作是否成功的 ApiResponse
     */
    @DeleteMapping("/only/{adminId}") // 对应客户端 AdminServiceImpl.deleteAdminOnly() -> /admins/only/{adminId}
    public ApiResponse<Boolean> deleteOnly(@PathVariable String adminId) {
        boolean success = adminService.deleteAdminOnly(adminId); // 只删除 admin 表
        return ApiResponse.ok(success);
    }
}