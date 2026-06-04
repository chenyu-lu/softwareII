package com.credit.module.rating.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class RatingRequest {
    @NotNull(message = "被评分人ID不能为空")
    private Long toUserId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer score;

    private String content;
}
