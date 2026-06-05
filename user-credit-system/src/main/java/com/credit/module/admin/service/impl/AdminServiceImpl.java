package com.credit.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.module.admin.dto.DashboardVO;
import com.credit.module.admin.service.AdminService;
import com.credit.module.rating.entity.UserRating;
import com.credit.module.rating.mapper.RatingMapper;
import com.credit.module.user.entity.User;
import com.credit.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RatingMapper ratingMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 总用户数
        vo.setTotalUsers(userMapper.selectCount(null));

        // 活跃用户（status=1）
        QueryWrapper<User> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("status", 1);
        vo.setActiveUsers(userMapper.selectCount(activeWrapper));

        // 总评价数
        vo.setTotalRatings(ratingMapper.selectCount(null));

        // 今日新注册用户
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        QueryWrapper<User> todayWrapper = new QueryWrapper<>();
        todayWrapper.between("created_at", todayStart, todayEnd);
        vo.setTodayNewUsers(userMapper.selectCount(todayWrapper));

        // 订单模块尚未实现，暂时返回 0
        vo.setTotalOrders(0L);

        return vo;
    }

    @Override
    public Page<User> listUsers(int page, int pageSize, String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("username", keyword)
                   .or()
                   .like("real_name", keyword)
                   .or()
                   .like("email", keyword);
        }
        wrapper.orderByDesc("created_at");

        Page<User> pageObj = new Page<>(page, pageSize);
        return userMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public User updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setRole(role);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public Page<UserRating> listRatings(int page, int pageSize) {
        QueryWrapper<UserRating> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");

        Page<UserRating> pageObj = new Page<>(page, pageSize);
        return ratingMapper.selectPage(pageObj, wrapper);
    }
}
