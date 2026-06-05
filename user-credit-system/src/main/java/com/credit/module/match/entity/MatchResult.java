package com.credit.module.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("match_result")
public class MatchResult {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetUserId;

    private Integer ruleId;

    private Integer matchScore;

    private Integer status;

    private LocalDateTime createdAt;
}
