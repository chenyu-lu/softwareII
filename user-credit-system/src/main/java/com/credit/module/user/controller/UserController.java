package com.credit.module.user.controller;
import com.credit.common.result.Result;
import com.credit.module.user.dto.LoginRequest;
import com.credit.module.user.dto.RegisterRequest;
import com.credit.module.user.entity.User;
import com.credit.module.user.service.UserService;
import com.credit.module.user.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return Result.fail("用户名已存在");
        }
        if (request.getEmail() != null && userService.existsByEmail(request.getEmail())) {
            return Result.fail("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setCreditScore(100);
        user.setRole("USER");
        user.setStatus(1);

        userService.save(user);
        return Result.success("注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        User dbUser = userService.getByUsername(request.getUsername());
        if (dbUser == null || !encoder.matches(request.getPassword(), dbUser.getPassword())) {
            return Result.fail("账号或密码错误");
        }
        if (dbUser.getStatus() == 0) {
            return Result.fail("账号已被禁用");
        }

        String token = jwtUtil.generateToken(dbUser.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);

        User safeUser = new User();
        safeUser.setId(dbUser.getId());
        safeUser.setUsername(dbUser.getUsername());
        safeUser.setEmail(dbUser.getEmail());
        safeUser.setRealName(dbUser.getRealName());
        safeUser.setPhone(dbUser.getPhone());
        safeUser.setAvatar(dbUser.getAvatar());
        safeUser.setCreditScore(dbUser.getCreditScore());
        safeUser.setRole(dbUser.getRole());

        map.put("user", safeUser);
        return Result.success(map);
    }

    @ApiOperation("获取个人资料")
    @GetMapping("/profile")
    public Result profile(@RequestHeader("token") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setPassword(null);
        return Result.success(user);
    }

    @ApiOperation("更新个人资料")
    @PutMapping("/profile")
    public Result update(@RequestHeader("token") String token, @RequestBody User user) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }

        user.setId(userId);
        user.setUsername(dbUser.getUsername());
        user.setPassword(dbUser.getPassword());
        user.setCreditScore(dbUser.getCreditScore());
        user.setRole(dbUser.getRole());
        user.setStatus(dbUser.getStatus());

        userService.updateById(user);
        return Result.success("修改成功");
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result changePassword(@RequestHeader("token") String token, @RequestBody Map<String, String> params) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (!encoder.matches(oldPassword, dbUser.getPassword())) {
            return Result.fail("原密码错误");
        }

        dbUser.setPassword(encoder.encode(newPassword));
        userService.updateById(dbUser);
        return Result.success("密码修改成功");
    }
}

