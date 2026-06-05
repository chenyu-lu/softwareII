package com.credit.module.message.controller;

import com.credit.common.result.Result;
import com.credit.module.message.dto.SendMessageRequest;
import com.credit.module.message.service.MessageService;
import com.credit.module.user.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "消息通知")
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private JwtUtil jwtUtil;

    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result send(@RequestHeader("token") String token,
                       @Valid @RequestBody SendMessageRequest request) {
        Long senderId = jwtUtil.getUserIdFromToken(token);

        if (senderId.equals(request.getReceiverId())) {
            return Result.fail("不能给自己发消息");
        }

        return Result.success(messageService.sendMessage(senderId, request));
    }

    @ApiOperation("会话列表")
    @GetMapping("/conversations")
    public Result conversations(@RequestHeader("token") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(messageService.getConversations(userId));
    }

    @ApiOperation("获取会话消息")
    @GetMapping("/{conversationId}")
    public Result messages(@RequestHeader("token") String token,
                           @PathVariable Long conversationId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(messageService.getMessages(conversationId, userId));
    }

    @ApiOperation("未读消息数")
    @GetMapping("/unread")
    public Result unread(@RequestHeader("token") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(messageService.getUnreadCount(userId));
    }
}
