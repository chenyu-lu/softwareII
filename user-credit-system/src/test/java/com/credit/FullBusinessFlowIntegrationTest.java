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
class FullBusinessFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void completeFlowRegisterLoginPublishClaimSubmitConfirmAndRateShouldWork() {
        AuthUser publisher = registerAndLogin("flow_publisher");
        AuthUser helper = registerAndLogin("flow_helper");

        Map<String, Object> createResult = post("/api/order/create", Map.of(
                "description", "完整流程测试：帮忙取快递",
                "category", "生活服务",
                "orderType", "HELP",
                "amount", "0.00",
                "reward", "一杯奶茶",
                "due", "今天18:00前",
                "contact", "13800000000"
        ), publisher.token);

        assertThat(createResult.get("code")).isEqualTo(200);
        Long orderId = idOf(createResult.get("data"));

        Map<String, Object> hallResult = get("/api/order/hall", helper.token);
        assertThat(hallResult.get("code")).isEqualTo(200);
        List<Map<String, Object>> hallOrders = castList(hallResult.get("data"));
        assertThat(hallOrders).anySatisfy(order -> assertThat(idOf(order)).isEqualTo(orderId));

        Map<String, Object> claimResult = post("/api/order/claim/" + orderId, null, helper.token);
        assertThat(claimResult.get("code")).isEqualTo(200);
        Map<String, Object> claimedOrder = castMap(claimResult.get("data"));
        assertThat(claimedOrder.get("status")).isEqualTo("IN_PROGRESS");
        assertThat(asLong(claimedOrder.get("takerId"))).isEqualTo(helper.userId);

        Map<String, Object> myClaimedResult = get("/api/order/my-claimed", helper.token);
        assertThat(myClaimedResult.get("code")).isEqualTo(200);
        List<Map<String, Object>> myClaimedOrders = castList(myClaimedResult.get("data"));
        assertThat(myClaimedOrders).anySatisfy(order -> assertThat(idOf(order)).isEqualTo(orderId));

        Map<String, Object> submitResult = post("/api/order/submit/" + orderId, null, helper.token);
        assertThat(submitResult.get("code")).isEqualTo(200);
        assertThat(castMap(submitResult.get("data")).get("status")).isEqualTo("SUBMITTED");

        Map<String, Object> confirmResult = post("/api/order/confirm/" + orderId, null, publisher.token);
        assertThat(confirmResult.get("code")).isEqualTo(200);
        Map<String, Object> completedOrder = castMap(confirmResult.get("data"));
        assertThat(completedOrder.get("status")).isEqualTo("COMPLETED");
        assertThat(asInt(completedOrder.get("isCompleted"))).isEqualTo(1);

        Map<String, Object> rateHelperResult = post("/api/rating/add", Map.of(
                "toUserId", helper.userId,
                "score", 5,
                "content", "完整流程测试：接取人服务很好"
        ), publisher.token);
        assertThat(rateHelperResult.get("code")).isEqualTo(200);

        Map<String, Object> ratePublisherResult = post("/api/rating/add", Map.of(
                "toUserId", publisher.userId,
                "score", 5,
                "content", "完整流程测试：发布人沟通清楚"
        ), helper.token);
        assertThat(ratePublisherResult.get("code")).isEqualTo(200);

        Map<String, Object> helperReceivedRatings = get("/api/rating/received", helper.token);
        assertThat(helperReceivedRatings.get("code")).isEqualTo(200);
        List<Map<String, Object>> ratings = castList(helperReceivedRatings.get("data"));
        assertThat(ratings).anySatisfy(rating -> {
            assertThat(asLong(rating.get("fromUserId"))).isEqualTo(publisher.userId);
            assertThat(asLong(rating.get("toUserId"))).isEqualTo(helper.userId);
        });

        Map<String, Object> helperCreditResult = get("/api/rating/credit/" + helper.userId, publisher.token);
        assertThat(helperCreditResult.get("code")).isEqualTo(200);
        assertThat(asInt(helperCreditResult.get("data"))).isBetween(0, 200);
    }

    @Test
    void abnormalFlowShouldRejectNoTokenDuplicateClaimAndUnauthorizedConfirm() {
        AuthUser publisher = registerAndLogin("abnormal_publisher");
        AuthUser firstTaker = registerAndLogin("abnormal_first_taker");
        AuthUser secondTaker = registerAndLogin("abnormal_second_taker");

        Map<String, Object> noTokenCreateResult = post("/api/order/create", Map.of(
                "description", "未登录发布测试",
                "category", "生活服务",
                "orderType", "HELP"
        ), null);
        assertThat(noTokenCreateResult.get("code")).isEqualTo(500);
        assertThat(noTokenCreateResult.get("msg").toString()).contains("未登录");

        Long orderId = idOf(post("/api/order/create", Map.of(
                "description", "异常流程测试：重复接单与越权确认",
                "category", "生活服务",
                "orderType", "HELP",
                "amount", "0.00"
        ), publisher.token).get("data"));

        Map<String, Object> firstClaimResult = post("/api/order/claim/" + orderId, null, firstTaker.token);
        assertThat(firstClaimResult.get("code")).isEqualTo(200);

        Map<String, Object> duplicateClaimResult = post("/api/order/claim/" + orderId, null, secondTaker.token);
        assertThat(duplicateClaimResult.get("code")).isEqualTo(500);
        assertThat(duplicateClaimResult.get("msg").toString()).contains("任务已被接取");

        Map<String, Object> submitResult = post("/api/order/submit/" + orderId, null, firstTaker.token);
        assertThat(submitResult.get("code")).isEqualTo(200);

        Map<String, Object> unauthorizedConfirmResult = post("/api/order/confirm/" + orderId, null, secondTaker.token);
        assertThat(unauthorizedConfirmResult.get("code")).isEqualTo(500);
        assertThat(unauthorizedConfirmResult.get("msg").toString()).contains("你不是该任务的发布人");
    }

    private AuthUser registerAndLogin(String prefix) {
        String username = unique(prefix);

        post("/api/user/register", Map.of(
                "username", username,
                "password", "123456",
                "email", username + "@campus.com",
                "realName", "完整流程测试用户",
                "phone", "131" + randomEightDigits()
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
