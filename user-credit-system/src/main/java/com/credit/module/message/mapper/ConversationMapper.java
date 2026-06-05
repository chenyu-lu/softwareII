package com.credit.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.message.entity.Conversation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("select * from conversation where (user1_id=#{userId} or user2_id=#{userId}) order by last_time desc")
    List<Conversation> selectByUserId(@Param("userId") Long userId);

    @Select("select * from conversation where ((user1_id=#{user1Id} and user2_id=#{user2Id}) or (user1_id=#{user2Id} and user2_id=#{user1Id})) and order_id=#{orderId}")
    Conversation selectByUserPair(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("orderId") Long orderId);

    @Select("select * from conversation where ((user1_id=#{user1Id} and user2_id=#{user2Id}) or (user1_id=#{user2Id} and user2_id=#{user1Id})) and order_id is null limit 1")
    Conversation selectByUserPairNoOrder(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}
