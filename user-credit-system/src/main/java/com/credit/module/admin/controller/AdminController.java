package com.credit.module.admin.controller;

import com.credit.common.result.Result;
import com.credit.module.admin.dto.UserManageRequest;
import com.credit.module.admin.service.AdminService;
import com.credit.module.user.entity.User;
import com.credit.module.user.mapper.UserMapper;
import com.credit.module.user.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "后台管理")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 校验当前用户是否为管理员
     */
    private User checkAdmin(String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User admin = userMapper.selectById(userId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new IllegalArgumentException("无管理员权限");
        }
        return admin;
    }

    @ApiOperation("数据看板")
    @GetMapping("/dashboard")
    public Result dashboard(@RequestHeader("token") String token) {
        checkAdmin(token);
        return Result.success(adminService.getDashboard());
    }

    @ApiOperation("用户列表")
    @GetMapping("/users")
    public Result users(@RequestHeader("token") String token,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int pageSize,
                        @RequestParam(required = false) String keyword) {
        checkAdmin(token);
        return Result.success(adminService.listUsers(page, pageSize, keyword));
    }

    @ApiOperation("封禁/解封用户")
    @PutMapping("/user/status")
    public Result updateStatus(@RequestHeader("token") String token,
                               @Valid @RequestBody UserManageRequest request) {
        checkAdmin(token);
        return Result.success(adminService.updateUserStatus(request.getUserId(), request.getStatus()));
    }

    @ApiOperation("修改用户角色")
    @PutMapping("/user/role")
    public Result updateRole(@RequestHeader("token") String token,
                             @Valid @RequestBody UserManageRequest request) {
        checkAdmin(token);
        return Result.success(adminService.updateUserRole(request.getUserId(), request.getRole()));
    }

    @ApiOperation("评价列表")
    @GetMapping("/ratings")
    public Result ratings(@RequestHeader("token") String token,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int pageSize) {
        checkAdmin(token);
        return Result.success(adminService.listRatings(page, pageSize));
    }
}
