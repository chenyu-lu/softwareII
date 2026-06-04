package com.credit.module.rating.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.credit.module.rating.entity.UserRating;
import java.util.List;

public interface RatingService extends IService<UserRating> {
    void saveRatingAndUpdateCredit(UserRating rating);
    List<UserRating> listByToUserId(Long userId);
    List<UserRating> listByFromUserId(Long userId);
    Integer getCreditScore(Long userId);
}
