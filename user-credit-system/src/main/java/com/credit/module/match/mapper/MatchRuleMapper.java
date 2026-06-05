package com.credit.module.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.match.entity.MatchRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MatchRuleMapper extends BaseMapper<MatchRule> {

    @Select("select * from match_rule where status=1 order by priority desc")
    List<MatchRule> selectEnabledRules();

    @Select("select * from match_rule order by priority desc")
    List<MatchRule> selectAllOrderByPriority();
}
