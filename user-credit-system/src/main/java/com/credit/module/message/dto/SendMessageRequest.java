package com.credit.module.message.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SendMessageRequest {
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private Long orderId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Integer msgType;
}
