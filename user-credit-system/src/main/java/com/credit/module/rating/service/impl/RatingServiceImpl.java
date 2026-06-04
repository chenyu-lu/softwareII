package com.credit.module.rating.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.credit.module.rating.algorithm.CreditAlgorithm;
import com.credit.module.rating.entity.UserRating;
import com.credit.module.rating.mapper.RatingMapper;
import com.credit.module.rating.service.RatingService;
import com.credit.module.user.entity.User;
import com.credit.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, UserRating> implements RatingService {
    @Resource
    private RatingMapper ratingMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CreditAlgorithm algorithm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRatingAndUpdateCredit(UserRating rating) {
        save(rating);
        Long toUserId = rating.getToUserId();
        Double avg = ratingMapper.selectAvgScore(toUserId);
        if (avg == null) avg = 3.0;
        User user = userMapper.selectById(toUserId);
        if (user == null) return;
        int newScore = algorithm.calcNewScore(user.getCreditScore(), avg);
        user.setCreditScore(newScore);
        userMapper.updateById(user);
    }

    @Override
    public List<UserRating> listByToUserId(Long userId) {
        return ratingMapper.selectByToUserId(userId);
    }

    @Override
    public List<UserRating> listByFromUserId(Long userId) {
        return ratingMapper.selectByFromUserId(userId);
    }

    @Override
    public Integer getCreditScore(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? 0 : user.getCreditScore();
    }
}
