package com.credit.module.match.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MatchRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;

    @NotNull(message = "最低信用分不能为空")
    private Integer minCreditScore;

    @NotNull(message = "最高信用分不能为空")
    private Integer maxCreditScore;

    private Integer priority;
}
