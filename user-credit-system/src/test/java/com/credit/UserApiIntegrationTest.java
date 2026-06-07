package com.credit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import javax.annotation.Resource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void registerShouldSucceedWithValidUser() {
        String username = unique("user_ok");

        Map<String, Object> result = post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "test user",
                "phone", "139" + randomEightDigits()
        ), null);

        assertThat(result.get("code")).isEqualTo(200);
        assertThat(result.get("msg").toString()).contains("注册成功");
    }

    @Test
    void registerShouldFailWhenUsernameDuplicated() {
        String username = unique("dup_user");

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "duplicate user",
                "phone", "138" + randomEightDigits()
        ), null);

        Map<String, Object> secondResult = post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "_2@campus.com",
                "realName", "duplicate user 2",
                "phone", "137" + randomEightDigits()
        ), null);

        assertThat(secondResult.get("code")).isEqualTo(500);
        assertThat(secondResult.get("msg").toString()).contains("用户名已存在");
    }

    @Test
    void loginShouldReturnTokenAndProfileShouldReturnCurrentUser() {
        String username = unique("login_user");

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "login test user",
                "phone", "136" + randomEightDigits()
        ), null);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "123456"
        ), null);

        assertThat(loginResult.get("code")).isEqualTo(200);

        Map<String, Object> loginData = castMap(loginResult.get("data"));
        String token = loginData.get("token").toString();
        assertThat(token).isNotBlank();

        Map<String, Object> profileResult = get("/api/user/profile", token);
        assertThat(profileResult.get("code")).isEqualTo(200);

        Map<String, Object> profile = castMap(profileResult.get("data"));
        assertThat(profile.get("username")).isEqualTo(username);
        assertThat(profile.get("password")).isNull();
    }

    @Test
    void loginShouldFailWithWrongPassword() {
        String username = unique("wrong_pwd_user");

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com"
        ), null);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "wrong-password"
        ), null);

        assertThat(loginResult.get("code")).isEqualTo(500);
        assertThat(loginResult.get("msg").toString()).contains("账号或密码错误");
    }

    @Test
    void profileShouldFailWithoutToken() {
        Map<String, Object> result = get("/api/user/profile", null);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("未登录");
    }

    private Map<String, Object> post(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("token", token);
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private Map<String, Object> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("token", token);
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

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
}
