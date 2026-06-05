package com.credit.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserManageRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Integer status;

    private String role;
}
