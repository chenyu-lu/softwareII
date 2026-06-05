package com.credit.module.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.match.entity.MatchResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MatchResultMapper extends BaseMapper<MatchResult> {

    @Select("select * from match_result where user_id=#{userId} order by created_at desc")
    List<MatchResult> selectByUserId(@Param("userId") Long userId);

    @Select("delete from match_result where user_id=#{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
