package com.credit.module.admin.dto;

import lombok.Data;

@Data
public class DashboardVO {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalOrders;
    private Long totalRatings;
    private Long todayNewUsers;

    public static DashboardVO empty() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalUsers(0L);
        vo.setActiveUsers(0L);
        vo.setTotalOrders(0L);
        vo.setTotalRatings(0L);
        vo.setTodayNewUsers(0L);
        return vo;
    }
}
