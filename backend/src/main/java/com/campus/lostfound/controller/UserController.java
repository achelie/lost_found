package com.campus.lostfound.controller;

import com.campus.lostfound.dto.Result;
import com.campus.lostfound.dto.ChangePasswordDTO;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<User> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody User user) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateProfile(userId, user);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, dto);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }
}
