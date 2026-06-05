package com.credit.module.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.credit.module.message.dto.SendMessageRequest;
import com.credit.module.message.entity.Conversation;
import com.credit.module.message.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {

    Message sendMessage(Long senderId, SendMessageRequest req);

    List<Conversation> getConversations(Long userId);

    List<Message> getMessages(Long conversationId, Long userId);

    int getUnreadCount(Long userId);
}
