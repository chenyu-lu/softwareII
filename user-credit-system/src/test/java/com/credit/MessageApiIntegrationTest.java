package com.credit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageApiIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    void sendMessageShouldFailWhenReceiverIsSelf() {
        AuthUser user = registerAndLogin("msg_self");

        Map<String, Object> result = post("/api/message/send", Map.of(
                "receiverId", user.userId,
                "content", "不能给自己发消息",
                "msgType", 1
        ), user.token);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("不能给自己发消息");
    }

    @Test
    void sendMessageShouldFailWhenContentIsBlank() {
        AuthUser sender = registerAndLogin("msg_blank_sender");
        AuthUser receiver = registerAndLogin("msg_blank_receiver");

        Map<String, Object> result = post("/api/message/send", Map.of(
                "receiverId", receiver.userId,
                "content", "",
                "msgType", 1
        ), sender.token);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("消息内容不能为空");
    }

    @Test
    void existingConversationMessageFlowShouldWork() {
        AuthUser sender = registerAndLogin("msg_sender");
        AuthUser receiver = registerAndLogin("msg_receiver");
        Long conversationId = createConversation(sender.userId, receiver.userId);
        String content = "测试消息-" + System.currentTimeMillis();

        Map<String, Object> sendResult = post("/api/message/send", Map.of(
                "receiverId", receiver.userId,
                "content", content,
                "msgType", 1
        ), sender.token);

        assertThat(sendResult.get("code")).isEqualTo(200);

        Map<String, Object> unreadResult = get("/api/message/unread", receiver.token);
        assertThat(unreadResult.get("code")).isEqualTo(200);
        assertThat(((Number) unreadResult.get("data")).intValue()).isGreaterThanOrEqualTo(1);

        Map<String, Object> conversationsResult = get("/api/message/conversations", receiver.token);
        assertThat(conversationsResult.get("code")).isEqualTo(200);
        assertThat(castList(conversationsResult.get("data"))).isNotEmpty();

        Map<String, Object> messagesResult = get("/api/message/" + conversationId, receiver.token);
        assertThat(messagesResult.get("code")).isEqualTo(200);
        List<Map<String, Object>> messages = castList(messagesResult.get("data"));
        assertThat(messages).anySatisfy(message -> assertThat(message.get("content")).isEqualTo(content));
    }

    @Test
    void messageApiShouldFailWithoutToken() {
        Map<String, Object> result = get("/api/message/unread", null);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("未登录");
    }

    private Long createConversation(Long userA, Long userB) {
        Long user1 = Math.min(userA, userB);
        Long user2 = Math.max(userA, userB);
        jdbcTemplate.update(
                "insert into conversation (user1_id, user2_id, last_message, last_time, unread_count) values (?, ?, ?, now(), 0)",
                user1, user2, ""
        );
        return jdbcTemplate.queryForObject(
                "select id from conversation where user1_id=? and user2_id=? and order_id is null order by id desc limit 1",
                Long.class,
                user1,
                user2
        );
    }

    private AuthUser registerAndLogin(String prefix) {
        String username = unique(prefix);
        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "消息测试用户",
                "phone", "132" + randomEightDigits()
        ), null);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "123456"
        ), null);

        assertThat(loginResult.get("code")).isEqualTo(200);
        Map<String, Object> data = castMap(loginResult.get("data"));
        Map<String, Object> user = castMap(data.get("user"));
        return new AuthUser(((Number) user.get("id")).longValue(), data.get("token").toString());
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object object) {
        return (List<Map<String, Object>>) object;
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
