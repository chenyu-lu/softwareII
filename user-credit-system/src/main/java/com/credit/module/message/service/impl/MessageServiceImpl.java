package com.credit.module.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.credit.module.message.dto.SendMessageRequest;
import com.credit.module.message.entity.Conversation;
import com.credit.module.message.entity.Message;
import com.credit.module.message.mapper.ConversationMapper;
import com.credit.module.message.mapper.MessageMapper;
import com.credit.module.message.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(Long senderId, SendMessageRequest req) {
        Long receiverId = req.getReceiverId();

        // 查找或创建会话
        Conversation conversation;
        if (req.getOrderId() != null) {
            conversation = conversationMapper.selectByUserPair(senderId, receiverId, req.getOrderId());
        } else {
            conversation = conversationMapper.selectByUserPairNoOrder(senderId, receiverId);
        }

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUser1Id(Math.min(senderId, receiverId));
            conversation.setUser2Id(Math.max(senderId, receiverId));
            conversation.setOrderId(req.getOrderId());
            conversation.setCreatedAt(LocalDateTime.now());
            conversationMapper.insert(conversation);
        }

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setOrderId(req.getOrderId());
        message.setContent(req.getContent());
        message.setMsgType(req.getMsgType() != null ? req.getMsgType() : 1);
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        save(message);

        // 更新会话
        conversation.setLastMessage(req.getContent().length() > 100 ? req.getContent().substring(0, 100) : req.getContent());
        conversation.setLastTime(message.getCreatedAt());
        conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        conversationMapper.updateById(conversation);

        return message;
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        return conversationMapper.selectByUserId(userId);
    }

    @Override
    public List<Message> getMessages(Long conversationId, Long userId) {
        messageMapper.markAsRead(conversationId, userId);

        // 清除该会话对当前用户的未读数
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getUnreadCount() > 0) {
            conversation.setUnreadCount(0);
            conversationMapper.updateById(conversation);
        }

        return messageMapper.selectByConversationId(conversationId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.selectUnreadCount(userId);
    }
}
