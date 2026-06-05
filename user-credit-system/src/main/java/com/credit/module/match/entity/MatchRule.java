package com.credit.module.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("match_rule")
public class MatchRule {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String ruleName;

    private Integer ruleType;

    private Integer minCreditScore;

    private Integer maxCreditScore;

    private Integer priority;

    private Integer status;

    private LocalDateTime createdAt;
}
