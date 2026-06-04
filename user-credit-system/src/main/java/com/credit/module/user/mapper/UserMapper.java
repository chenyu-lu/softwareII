package com.credit.module.user.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.module.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where username=#{username}")
    User selectByUsername(@Param("username") String username);
}

