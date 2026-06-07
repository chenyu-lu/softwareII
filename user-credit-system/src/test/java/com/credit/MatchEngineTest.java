package com.credit;

import com.credit.module.match.algorithm.MatchEngine;
import com.credit.module.match.entity.MatchResult;
import com.credit.module.match.entity.MatchRule;
import com.credit.module.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchEngineTest {

    private final MatchEngine matchEngine = new MatchEngine();

    @Test
    void executeShouldReturnMatchedUsersSortedByScoreDesc() {
        User target = user(1L, 100);
        User highCreditUser = user(2L, 150);
        User middleCreditUser = user(3L, 110);
        User lowCreditUser = user(4L, 60);

        MatchRule highCreditRule = rule(1, "high-credit", 1, 120, 200, 10);
        MatchRule middleCreditRule = rule(2, "middle-credit", 1, 80, 119, 5);

        List<MatchResult> results = matchEngine.execute(
                target,
                List.of(highCreditUser, middleCreditUser, lowCreditUser),
                List.of(highCreditRule, middleCreditRule),
                20
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTargetUserId()).isEqualTo(2L);
        assertThat(results.get(1).getTargetUserId()).isEqualTo(3L);
        assertThat(results.get(0).getMatchScore()).isGreaterThan(results.get(1).getMatchScore());
    }

    @Test
    void executeShouldRespectTopNLimit() {
        User target = user(1L, 100);
        MatchRule rule = rule(1, "credit-rule", 1, 80, 200, 5);

        List<MatchResult> results = matchEngine.execute(
                target,
                List.of(user(2L, 130), user(3L, 120), user(4L, 110)),
                List.of(rule),
                2
        );

        assertThat(results).hasSize(2);
    }

    @Test
    void executeShouldReturnEmptyWhenNoCandidateMatchesAnyRule() {
        User target = user(1L, 100);
        User lowCreditUser = user(2L, 50);
        MatchRule highCreditRule = rule(1, "high-credit", 1, 120, 200, 10);

        List<MatchResult> results = matchEngine.execute(
                target,
                List.of(lowCreditUser),
                List.of(highCreditRule),
                20
        );

        assertThat(results).isEmpty();
    }

    @Test
    void executeShouldReturnEmptyWhenCandidateListIsEmpty() {
        User target = user(1L, 100);
        MatchRule rule = rule(1, "credit-rule", 1, 80, 200, 5);

        List<MatchResult> results = matchEngine.execute(
                target,
                List.of(),
                List.of(rule),
                20
        );

        assertThat(results).isEmpty();
    }

    private User user(Long id, Integer creditScore) {
        User user = new User();
        user.setId(id);
        user.setCreditScore(creditScore);
        user.setStatus(1);
        return user;
    }

    private MatchRule rule(Integer id, String name, Integer type,
                           Integer minScore, Integer maxScore, Integer priority) {
        MatchRule rule = new MatchRule();
        rule.setId(id);
        rule.setRuleName(name);
        rule.setRuleType(type);
        rule.setMinCreditScore(minScore);
        rule.setMaxCreditScore(maxScore);
        rule.setPriority(priority);
        rule.setStatus(1);
        return rule;
    }
}
