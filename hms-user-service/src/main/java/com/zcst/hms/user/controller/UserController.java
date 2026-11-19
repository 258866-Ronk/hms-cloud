package com.zcst.hms.user.controller;

import com.zcst.hms.common.constants.HmsConstants;
import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.result.Result;
import com.zcst.hms.user.entity.User;
import com.zcst.hms.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户 / 认证接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        Map<String, Object> data = userService.login(body.get("username"), body.get("password"));
        return Result.ok("登录成功", data);
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody User user) {
        return Result.ok("注册成功", userService.register(user));
    }

    /**
     * 服务内部调用：按主键查询
     */
    @GetMapping("/internal/{id}")
    public Result<UserDTO> getById(@PathVariable("id") Long id) {
        return Result.ok(userService.getById(id));
    }

    /**
     * 服务内部调用：按工号 / 用户名查询
     */
    @GetMapping("/internal/username/{username}")
    public Result<UserDTO> getByUsername(@PathVariable("username") String username) {
        return Result.ok(userService.getByUsername(username));
    }

    /**
     * 服务内部调用：医生列表
     */
    @GetMapping("/internal/doctors")
    public Result<List<UserDTO>> doctors(@RequestParam(required = false) String deptCode,
                                         @RequestParam(required = false) String keyword) {
        return Result.ok(userService.listDoctors(deptCode, keyword));
    }

    /**
     * 当前登录人信息（userId 由网关透传）
     */
    @GetMapping("/profile")
    public Result<UserDTO> profile(HttpServletRequest request) {
        String userId = request.getHeader(HmsConstants.HEADER_USER_ID);
        return Result.ok(userService.getById(Long.valueOf(userId)));
    }

    @PutMapping("/{id}")
    public Result<UserDTO> update(@PathVariable("id") Long id, @RequestBody User user) {
        user.setId(id);
        return Result.ok(userService.update(user));
    }
}
