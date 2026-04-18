package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<IPage<Notification>> list(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    @PostMapping("/read/{id}")
    public Result<Void> markRead(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markRead(userId, id);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationService.unreadCount(userId));
    }
}
