package com.credit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RatingApiIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void userCanRateAnotherUserAndQueryCreditScore() {
        AuthUser fromUser = registerAndLogin("rating_from");
        AuthUser toUser = registerAndLogin("rating_to");

        Map<String, Object> addResult = post("/api/rating/add", Map.of(
                "toUserId", toUser.userId,
                "score", 5,
                "content", "good helper"
        ), fromUser.token);

        assertThat(addResult.get("code")).isEqualTo(200);
        assertThat(addResult.get("msg").toString()).contains("评价成功");

        Map<String, Object> creditResult = get("/api/rating/credit/" + toUser.userId, fromUser.token);
        assertThat(creditResult.get("code")).isEqualTo(200);
        assertThat(((Number) creditResult.get("data")).intValue()).isBetween(0, 200);
    }

    @Test
    void userCannotRateSelf() {
        AuthUser user = registerAndLogin("rating_self");

        Map<String, Object> result = post("/api/rating/add", Map.of(
                "toUserId", user.userId,
                "score", 5,
                "content", "self rating"
        ), user.token);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("不能给自己评分");
    }

    @Test
    void ratingScoreGreaterThanFiveShouldFail() {
        AuthUser fromUser = registerAndLogin("rating_invalid_from");
        AuthUser toUser = registerAndLogin("rating_invalid_to");

        Map<String, Object> result = post("/api/rating/add", Map.of(
                "toUserId", toUser.userId,
                "score", 6,
                "content", "invalid score"
        ), fromUser.token);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("评分最高为5");
    }

    @Test
    void ratingShouldFailWithoutToken() {
        AuthUser toUser = registerAndLogin("rating_no_token_to");

        Map<String, Object> result = post("/api/rating/add", Map.of(
                "toUserId", toUser.userId,
                "score", 5,
                "content", "no token"
        ), null);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("未登录");
    }

    @Test
    void sentAndReceivedRatingListsShouldContainCreatedRating() {
        AuthUser fromUser = registerAndLogin("rating_list_from");
        AuthUser toUser = registerAndLogin("rating_list_to");

        post("/api/rating/add", Map.of(
                "toUserId", toUser.userId,
                "score", 4,
                "content", "list check"
        ), fromUser.token);

        Map<String, Object> sentResult = get("/api/rating/sent", fromUser.token);
        assertThat(sentResult.get("code")).isEqualTo(200);
        List<Map<String, Object>> sentRatings = castList(sentResult.get("data"));
        assertThat(sentRatings).anySatisfy(rating ->
                assertThat(((Number) rating.get("toUserId")).longValue()).isEqualTo(toUser.userId)
        );

        Map<String, Object> receivedResult = get("/api/rating/received", toUser.token);
        assertThat(receivedResult.get("code")).isEqualTo(200);
        List<Map<String, Object>> receivedRatings = castList(receivedResult.get("data"));
        assertThat(receivedRatings).anySatisfy(rating ->
                assertThat(((Number) rating.get("fromUserId")).longValue()).isEqualTo(fromUser.userId)
        );
    }

    private AuthUser registerAndLogin(String prefix) {
        String username = unique(prefix);

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "rating user",
                "phone", "135" + randomEightDigits()
        ), null);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "123456"
        ), null);

        Map<String, Object> data = castMap(loginResult.get("data"));
        String token = data.get("token").toString();
        Map<String, Object> user = castMap(data.get("user"));
        Long userId = ((Number) user.get("id")).longValue();
        return new AuthUser(userId, token);
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
