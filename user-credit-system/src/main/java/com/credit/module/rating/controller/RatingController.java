package com.credit.module.rating.controller;
import com.credit.common.result.Result;
import com.credit.config.JwtInterceptor;
import com.credit.module.rating.dto.RatingRequest;
import com.credit.module.rating.entity.UserRating;
import com.credit.module.rating.service.RatingService;
import com.credit.module.user.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "评分管理")
@RestController
@RequestMapping("/api/rating")
public class RatingController {
    @Resource
    private RatingService ratingService;
    @Resource
    private JwtUtil jwtUtil;

    @ApiOperation("添加评分")
    @PostMapping("/add")
    public Result add(HttpServletRequest request, @Valid @RequestBody RatingRequest ratingReq) {
        String token = request.getHeader("token");
        Long fromUserId = jwtUtil.getUserIdFromToken(token);

        if (fromUserId.equals(ratingReq.getToUserId())) {
            return Result.fail("不能给自己评分");
        }

        UserRating rating = new UserRating();
        rating.setFromUserId(fromUserId);
        rating.setToUserId(ratingReq.getToUserId());
        rating.setScore(ratingReq.getScore());
        rating.setContent(ratingReq.getContent());

        ratingService.saveRatingAndUpdateCredit(rating);
        return Result.success("评价成功，信用已更新");
    }

    @ApiOperation("查询收到的评分")
    @GetMapping("/received")
    public Result listReceived(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(ratingService.listByToUserId(userId));
    }

    @ApiOperation("查询发出的评分")
    @GetMapping("/sent")
    public Result listSent(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(ratingService.listByFromUserId(userId));
    }

    @ApiOperation("查询指定用户信用分")
    @GetMapping("/credit/{userId}")
    public Result credit(@PathVariable Long userId) {
        return Result.success(ratingService.getCreditScore(userId));
    }
}
