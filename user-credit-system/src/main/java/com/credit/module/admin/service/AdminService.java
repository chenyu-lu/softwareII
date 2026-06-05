package com.credit.module.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.module.admin.dto.DashboardVO;
import com.credit.module.rating.entity.UserRating;
import com.credit.module.user.entity.User;

public interface AdminService {

    DashboardVO getDashboard();

    Page<User> listUsers(int page, int pageSize, String keyword);

    User updateUserStatus(Long userId, Integer status);

    User updateUserRole(Long userId, String role);

    Page<UserRating> listRatings(int page, int pageSize);
}
