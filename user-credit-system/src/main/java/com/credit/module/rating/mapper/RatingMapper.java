package com.credit.module.rating.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.rating.entity.UserRating;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface RatingMapper extends BaseMapper<UserRating> {
    @Select("select avg(score) from user_rating where to_user_id=#{toUserId}")
    Double selectAvgScore(@Param("toUserId") Long toUserId);

    @Select("select * from user_rating where to_user_id=#{toUserId}")
    List<UserRating> selectByToUserId(@Param("toUserId") Long toUserId);

    @Select("select * from user_rating where from_user_id=#{fromUserId}")
    List<UserRating> selectByFromUserId(@Param("fromUserId") Long fromUserId);
}
