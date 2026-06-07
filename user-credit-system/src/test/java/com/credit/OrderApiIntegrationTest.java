package com.credit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void createOrderShouldFailWithoutToken() {
        Map<String, Object> result = post("/api/order/create", orderRequest("no-token"), null);

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg").toString()).contains("未登录");
    }

    @Test
    void userCanCreateAndListOwnOrder() {
        AuthUser publisher = registerAndLogin("order_owner");

        Map<String, Object> createResult = createOrder(publisher.token, "owner-list");
        assertThat(createResult.get("code")).isEqualTo(200);
        Long orderId = idOf(createResult.get("data"));

        Map<String, Object> listResult = get("/api/order/list", publisher.token);
        assertThat(listResult.get("code")).isEqualTo(200);

        List<Map<String, Object>> orders = castList(listResult.get("data"));
        assertThat(orders).anySatisfy(order ->
                assertThat(idOf(order)).isEqualTo(orderId)
        );
    }

    @Test
    void orderShouldAppearInHallForOtherUser() {
        AuthUser publisher = registerAndLogin("hall_publisher");
        AuthUser taker = registerAndLogin("hall_taker");

        Map<String, Object> createResult = createOrder(publisher.token, "hall-visible");
        Long orderId = idOf(createResult.get("data"));

        Map<String, Object> hallResult = get("/api/order/hall", taker.token);
        assertThat(hallResult.get("code")).isEqualTo(200);

        List<Map<String, Object>> hallOrders = castList(hallResult.get("data"));
        assertThat(hallOrders).anySatisfy(order ->
                assertThat(idOf(order)).isEqualTo(orderId)
        );
    }

    @Test
    void alreadyClaimedOrderShouldNotBeClaimedAgain() {
        AuthUser publisher = registerAndLogin("dup_claim_publisher");
        AuthUser firstTaker = registerAndLogin("dup_claim_first");
        AuthUser secondTaker = registerAndLogin("dup_claim_second");

        Long orderId = idOf(createOrder(publisher.token, "dup-claim").get("data"));

        Map<String, Object> firstClaimResult = post("/api/order/claim/" + orderId, null, firstTaker.token);
        assertThat(firstClaimResult.get("code")).isEqualTo(200);

        Map<String, Object> secondClaimResult = post("/api/order/claim/" + orderId, null, secondTaker.token);
        assertThat(secondClaimResult.get("code")).isEqualTo(500);
        assertThat(secondClaimResult.get("msg").toString()).contains("任务已被接取");
    }

    @Test
    void submitAndConfirmShouldCheckUserRoleAndStatus() {
        AuthUser publisher = registerAndLogin("role_publisher");
        AuthUser taker = registerAndLogin("role_taker");
        AuthUser stranger = registerAndLogin("role_stranger");

        Long orderId = idOf(createOrder(publisher.token, "role-check").get("data"));

        Map<String, Object> claimResult = post("/api/order/claim/" + orderId, null, taker.token);
        assertThat(claimResult.get("code")).isEqualTo(200);

        Map<String, Object> wrongSubmitResult = post("/api/order/submit/" + orderId, null, stranger.token);
        assertThat(wrongSubmitResult.get("code")).isEqualTo(500);
        assertThat(wrongSubmitResult.get("msg").toString()).contains("你不是该任务的接取人");

        Map<String, Object> submitResult = post("/api/order/submit/" + orderId, null, taker.token);
        assertThat(submitResult.get("code")).isEqualTo(200);
        assertThat(castMap(submitResult.get("data")).get("status")).isEqualTo("SUBMITTED");

        Map<String, Object> wrongConfirmResult = post("/api/order/confirm/" + orderId, null, stranger.token);
        assertThat(wrongConfirmResult.get("code")).isEqualTo(500);
        assertThat(wrongConfirmResult.get("msg").toString()).contains("你不是该任务的发布人");

        Map<String, Object> confirmResult = post("/api/order/confirm/" + orderId, null, publisher.token);
        assertThat(confirmResult.get("code")).isEqualTo(200);
        Map<String, Object> confirmedOrder = castMap(confirmResult.get("data"));
        assertThat(confirmedOrder.get("status")).isEqualTo("COMPLETED");
        assertThat(asInt(confirmedOrder.get("isCompleted"))).isEqualTo(1);
    }

    private AuthUser registerAndLogin(String prefix) {
        String username = unique(prefix);

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "订单测试用户",
                "phone", "132" + randomEightDigits()
        ), null);

        Map<String, Object> loginResult = post("/api/user/login", Map.of(
                "username", username,
                "password", "123456"
        ), null);

        assertThat(loginResult.get("code")).isEqualTo(200);
        Map<String, Object> loginData = castMap(loginResult.get("data"));
        String token = loginData.get("token").toString();
        Map<String, Object> user = castMap(loginData.get("user"));
        Long userId = asLong(user.get("id"));
        return new AuthUser(userId, token);
    }

    private Map<String, Object> createOrder(String token, String suffix) {
        Map<String, Object> result = post("/api/order/create", orderRequest(suffix), token);
        assertThat(result.get("code")).isEqualTo(200);
        return result;
    }

    private Map<String, Object> orderRequest(String suffix) {
        return Map.of(
                "description", "帮忙取快递-" + suffix,
                "category", "生活服务",
                "orderType", "HELP",
                "amount", "0.00",
                "reward", "奶茶一杯",
                "due", "今天18:00前",
                "contact", "13800000000"
        );
    }

    private Map<String, Object> post(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("token", token);
        }
        ResponseEntity<Map> response = restTemplate.exchange(
                url(path),
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
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Long idOf(Object data) {
        return asLong(castMap(data).get("id"));
    }

    private Long idOf(Map<String, Object> data) {
        return asLong(data.get("id"));
    }

    private Long asLong(Object value) {
        return Long.valueOf(String.valueOf(value));
    }

    private Integer asInt(Object value) {
        return Integer.valueOf(String.valueOf(value));
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
