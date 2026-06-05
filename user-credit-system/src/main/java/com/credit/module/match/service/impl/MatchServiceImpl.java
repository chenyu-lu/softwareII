package com.credit.module.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.credit.module.match.algorithm.MatchEngine;
import com.credit.module.match.dto.MatchRuleRequest;
import com.credit.module.match.entity.MatchResult;
import com.credit.module.match.entity.MatchRule;
import com.credit.module.match.mapper.MatchResultMapper;
import com.credit.module.match.mapper.MatchRuleMapper;
import com.credit.module.match.service.MatchService;
import com.credit.module.user.entity.User;
import com.credit.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchServiceImpl extends ServiceImpl<MatchRuleMapper, MatchRule> implements MatchService {

    @Resource
    private MatchRuleMapper matchRuleMapper;

    @Resource
    private MatchResultMapper matchResultMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MatchEngine matchEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MatchResult> executeMatch(Long userId) {
        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 获取启用的规则
        List<MatchRule> rules = matchRuleMapper.selectEnabledRules();
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("没有启用的匹配规则");
        }

        // 获取候选用户（排除自己）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ne("id", userId).eq("status", 1);
        // 按 rating 模块的角色字段和信用分
        List<User> candidates = userMapper.selectList(wrapper);

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 清除旧匹配结果
        matchResultMapper.deleteByUserId(userId);

        // 执行匹配，最多返回 20 条
        List<MatchResult> results = matchEngine.execute(targetUser, candidates, rules, 20);

        // 保存
        for (MatchResult result : results) {
            result.setCreatedAt(LocalDateTime.now());
            matchResultMapper.insert(result);
        }

        return results;
    }

    @Override
    public List<MatchRule> getMatchRules() {
        return matchRuleMapper.selectAllOrderByPriority();
    }

    @Override
    public MatchRule saveRule(MatchRuleRequest req) {
        MatchRule rule = new MatchRule();
        rule.setRuleName(req.getRuleName());
        rule.setRuleType(req.getRuleType());
        rule.setMinCreditScore(req.getMinCreditScore());
        rule.setMaxCreditScore(req.getMaxCreditScore());
        rule.setPriority(req.getPriority() != null ? req.getPriority() : 0);
        rule.setStatus(1);
        rule.setCreatedAt(LocalDateTime.now());
        save(rule);
        return rule;
    }

    @Override
    public MatchRule toggleRule(Integer ruleId) {
        MatchRule rule = getById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在");
        }
        rule.setStatus(rule.getStatus() == 1 ? 0 : 1);
        updateById(rule);
        return rule;
    }

    @Override
    public List<MatchResult> getMatchResults(Long userId) {
        return matchResultMapper.selectByUserId(userId);
    }
}
