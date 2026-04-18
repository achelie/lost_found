package com.campus.lostfound.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户管理器 - 维护WebSocket连接中的用户
 */
public class OnlineUserManager {
    
    private static final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * 用户上线
     */
    public static void userOnline(Long userId) {
        if (userId != null) {
            onlineUsers.add(userId);
        }
    }

    /**
     * 用户离线
     */
    public static void userOffline(Long userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isOnline(Long userId) {
        return userId != null && onlineUsers.contains(userId);
    }

    /**
     * 获取所有在线用户
     */
    public static Set<Long> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }

    /**
     * 获取在线人数
     */
    public static int getOnlineCount() {
        return onlineUsers.size();
    }

    /**
     * 清空所有用户
     */
    public static void clear() {
        onlineUsers.clear();
    }
}
