package com.campus.lostfound.controller;

import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 从SecurityContext获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage saved = chatService.sendMessage(message);
        return Result.success(saved);
    }

    @GetMapping("/messages/{otherUserId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long otherUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未认证用户");
        }
        List<ChatMessage> messages = chatService.getConversationMessages(currentUserId, otherUserId);
        return Result.success(messages);
    }

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未认证用户");
        }
        List<Map<String, Object>> conversations = chatService.getConversationList(userId);
        return Result.success(conversations);
    }

    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("未认证用户");
        }
        int count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/mark-read/{fromUserId}")
    public Result<Void> markAsRead(@PathVariable Long fromUserId) {
        Long toUserId = getCurrentUserId();
        if (toUserId == null) {
            return Result.error("未认证用户");
        }
        chatService.markConversationAsRead(fromUserId, toUserId);
        return Result.success();
    }
}