package com.credit.module.match.controller;

import com.credit.common.result.Result;
import com.credit.module.match.dto.MatchRuleRequest;
import com.credit.module.match.service.MatchService;
import com.credit.module.user.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "智能匹配")
@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Resource
    private MatchService matchService;

    @Resource
    private JwtUtil jwtUtil;

    @ApiOperation("执行匹配")
    @PostMapping("/execute")
    public Result execute(@RequestHeader("token") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(matchService.executeMatch(userId));
    }

    @ApiOperation("匹配规则列表")
    @GetMapping("/rules")
    public Result rules() {
        return Result.success(matchService.getMatchRules());
    }

    @ApiOperation("新增匹配规则")
    @PostMapping("/rule")
    public Result addRule(@Valid @RequestBody MatchRuleRequest request) {
        return Result.success(matchService.saveRule(request));
    }

    @ApiOperation("启用/禁用规则")
    @PutMapping("/rule/{ruleId}/toggle")
    public Result toggle(@PathVariable Integer ruleId) {
        return Result.success(matchService.toggleRule(ruleId));
    }

    @ApiOperation("查询匹配结果")
    @GetMapping("/results")
    public Result results(@RequestHeader("token") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(matchService.getMatchResults(userId));
    }
}
