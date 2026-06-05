package com.credit.module.match.algorithm;

import com.credit.module.match.entity.MatchResult;
import com.credit.module.match.entity.MatchRule;
import com.credit.module.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MatchEngine {

    /**
     * 执行匹配：根据规则对候选用户打分，返回 Top N 匹配结果
     *
     * @param targetUser 被匹配的用户
     * @param candidates 候选用户列表（已排除自己）
     * @param rules      启用的匹配规则
     * @param topN       返回结果数上限
     * @return 匹配结果列表
     */
    public List<MatchResult> execute(User targetUser, List<User> candidates, List<MatchRule> rules, int topN) {
        List<MatchResult> results = new ArrayList<>();

        for (User candidate : candidates) {
            int totalScore = 0;
            int matchedRules = 0;

            for (MatchRule rule : rules) {
                int ruleScore = evaluateRule(targetUser, candidate, rule);
                if (ruleScore > 0) {
                    totalScore += ruleScore;
                    matchedRules++;
                }
            }

            if (matchedRules > 0) {
                MatchResult result = new MatchResult();
                result.setUserId(targetUser.getId());
                result.setTargetUserId(candidate.getId());
                result.setMatchScore(totalScore);
                result.setStatus(0);
                results.add(result);
            }
        }

        // 按匹配分数降序排列，取 Top N
        return results.stream()
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 根据单条规则评估匹配分数
     */
    private int evaluateRule(User targetUser, User candidate, MatchRule rule) {
        int baseScore = rule.getPriority() * 10;

        switch (rule.getRuleType()) {
            case 1: // 信用匹配
                int candidateScore = candidate.getCreditScore() != null ? candidate.getCreditScore() : 100;
                if (candidateScore >= rule.getMinCreditScore() && candidateScore <= rule.getMaxCreditScore()) {
                    return baseScore + (candidateScore - rule.getMinCreditScore()) / 10;
                }
                return 0;

            case 2: // 信用互补（需求方信用低时优先匹配高信用服务方）
                int targetScore = targetUser.getCreditScore() != null ? targetUser.getCreditScore() : 100;
                int serviceScore2 = candidate.getCreditScore() != null ? candidate.getCreditScore() : 100;
                if (targetScore < rule.getMinCreditScore() && serviceScore2 >= rule.getMinCreditScore()) {
                    return baseScore + serviceScore2 / 5;
                }
                return 0;

            default:
                return 0;
        }
    }
}
