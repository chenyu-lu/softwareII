package com.credit.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.message.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {

    @Select("select * from message where conversation_id=#{conversationId} order by created_at asc")
    List<Message> selectByConversationId(@Param("conversationId") Long conversationId);

    @Select("select count(*) from message where receiver_id=#{userId} and is_read=0")
    int selectUnreadCount(@Param("userId") Long userId);

    @Update("update message set is_read=1 where conversation_id=#{conversationId} and receiver_id=#{userId} and is_read=0")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
