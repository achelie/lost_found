package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Notification;

public interface NotificationService extends IService<Notification> {
    void send(Long userId, String title, String content, Integer type, Long relatedId);
    IPage<Notification> getUserNotifications(Long userId, int page, int size);
    void markRead(Long userId, Long id);
    long unreadCount(Long userId);
}
