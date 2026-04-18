package com.campus.lostfound.service;

import com.campus.lostfound.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatService {

    ChatMessage sendMessage(ChatMessage message);

    List<ChatMessage> getConversationMessages(Long userId1, Long userId2);

    List<Map<String, Object>> getConversationList(Long userId);

    int getUnreadCount(Long userId);

    void markConversationAsRead(Long fromUserId, Long toUserId);
}