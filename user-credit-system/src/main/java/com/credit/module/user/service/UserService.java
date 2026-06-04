package com.credit.module.user.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.credit.module.user.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
