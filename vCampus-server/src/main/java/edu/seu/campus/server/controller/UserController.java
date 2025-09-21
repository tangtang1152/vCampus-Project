package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.User;
import com.vCampus.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestParam String username, @RequestParam String password) {
        return ApiResponse.ok(userService.login(username, password));
    }

    @GetMapping("/by-username/{username}")
    public ApiResponse<User> getByUsername(@PathVariable String username) {
        return ApiResponse.ok(userService.getByUsername(username));
    }

    @GetMapping("/by-id/{userId}")
    public ApiResponse<User> getById(@PathVariable Integer userId) {
        return ApiResponse.ok(userService.getBySelfId(userId));
    }

    @GetMapping
    public ApiResponse<List<User>> listAll() {
        return ApiResponse.ok(userService.getAll());
    }

    @GetMapping("/exists/{userId}")
    public ApiResponse<Boolean> exists(@PathVariable Integer userId) {
        return ApiResponse.ok(userService.exists(userId));
    }

    @GetMapping("/exists/by-username/{username}")
    public ApiResponse<Boolean> existsByUsername(@PathVariable String username) {
        User u = userService.getByUsername(username);
        return ApiResponse.ok(u != null);
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody User user) {
        return ApiResponse.ok(userService.add(user));
    }

    @PutMapping("/{userId}")
    public ApiResponse<Boolean> update(@PathVariable Integer userId, @RequestBody User user) {
        user.setUserId(userId);
        return ApiResponse.ok(userService.update(user));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Boolean> delete(@PathVariable Integer userId) {
        return ApiResponse.ok(userService.delete(userId));
    }

    @PostMapping("/validate")
    public ApiResponse<Boolean> validate(@RequestBody User payload) {
        return ApiResponse.ok(userService.validateUser(payload.getUsername(), payload.getPassword()));
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        var res = userService.register(user);
        return ApiResponse.ok(res.name());
    }

    @PostMapping("/{userId}/change-password")
    public ApiResponse<Boolean> changePassword(@PathVariable Integer userId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword")));
    }

    @PostMapping("/{userId}/reset-password")
    public ApiResponse<Boolean> resetPassword(@PathVariable Integer userId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(userService.resetPassword(userId, body.get("newPassword")));
    }
}


