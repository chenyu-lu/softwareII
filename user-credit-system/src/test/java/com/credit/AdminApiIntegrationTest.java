package com.credit;

import com.credit.module.user.entity.User;
import com.credit.module.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void adminCanAccessDashboard() {
        AuthUser admin = createUserAndLogin("admin_dash", "ADMIN");

        Map<String, Object> result = get("/api/admin/dashboard", admin.token);

        assertThat(result.get("code")).isEqualTo(200);
        Map<String, Object> data = castMap(result.get("data"));
        assertThat(data).containsKeys("totalUsers", "activeUsers", "totalRatings", "totalOrders", "todayNewUsers");
    }

    @Test
    void normalUserCannotAccessDashboard() {
        AuthUser user = createUserAndLogin("normal_dash", "USER");

        Map<String, Object> result = get("/api/admin/dashboard", user.token);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("无管理员权限");
    }

    @Test
    void adminCanDisableAndEnableUser() {
        AuthUser admin = createUserAndLogin("admin_status", "ADMIN");
        AuthUser target = createUserAndLogin("target_status", "USER");

        Map<String, Object> disableResult = put("/api/admin/user/status", Map.of(
                "userId", target.userId,
                "status", 0
        ), admin.token);

        assertThat(disableResult.get("code")).isEqualTo(200);
        Map<String, Object> disabledUser = castMap(disableResult.get("data"));
        assertThat(((Number) disabledUser.get("status")).intValue()).isEqualTo(0);

        Map<String, Object> enableResult = put("/api/admin/user/status", Map.of(
                "userId", target.userId,
                "status", 1
        ), admin.token);

        assertThat(enableResult.get("code")).isEqualTo(200);
        Map<String, Object> enabledUser = castMap(enableResult.get("data"));
        assertThat(((Number) enabledUser.get("status")).intValue()).isEqualTo(1);
    }

    @Test
    void adminApiShouldFailWithoutToken() {
        Map<String, Object> result = get("/api/admin/dashboard", null);

        assertThat(result.get("code")).isEqualTo(500);
    }

    private AuthUser createUserAndLogin(String prefix, String role) {
        String username = unique(prefix);

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode("123456"));
        user.setEmail(username + "@campus.com");
        user.setRealName("admin test user");
        user.setPhone("133" + randomEightDigits());
        user.setRole(role);
        user.setStatus(1);
        user.setCreditScore(100);
        userMapper.insert(user);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "123456"
        ), null);

        assertThat(loginResult.get("code")).isEqualTo(200);
        Map<String, Object> data = castMap(loginResult.get("data"));
        Map<String, Object> loginUser = castMap(data.get("user"));
        return new AuthUser(((Number) loginUser.get("id")).longValue(), data.get("token").toString());
    }

    private Map<String, Object> post(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("token", token);
        }
        ResponseEntity<Map> response = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private Map<String, Object> put(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("token", token);
        }
        ResponseEntity<Map> response = restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers), Map.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private Map<String, Object> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("token", token);
        }
        ResponseEntity<Map> response = restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String unique(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    private String randomEightDigits() {
        return String.valueOf(10000000 + (int) (Math.random() * 89999999));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object object) {
        return (Map<String, Object>) object;
    }

    private static class AuthUser {
        private final Long userId;
        private final String token;

        private AuthUser(Long userId, String token) {
            this.userId = userId;
            this.token = token;
        }
    }
}
