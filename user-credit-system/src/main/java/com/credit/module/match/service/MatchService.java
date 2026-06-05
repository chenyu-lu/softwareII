package com.credit.module.match.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.credit.module.match.dto.MatchRuleRequest;
import com.credit.module.match.entity.MatchResult;
import com.credit.module.match.entity.MatchRule;

import java.util.List;

public interface MatchService extends IService<MatchRule> {

    List<MatchResult> executeMatch(Long userId);

    List<MatchRule> getMatchRules();

    MatchRule saveRule(MatchRuleRequest req);

    MatchRule toggleRule(Integer ruleId);

    List<MatchResult> getMatchResults(Long userId);
}
