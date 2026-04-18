package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.dto.*;
import com.campus.lostfound.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getProfile(Long userId);
    void updateProfile(Long userId, User user);
}
