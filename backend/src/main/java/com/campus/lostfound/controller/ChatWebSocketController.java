package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ChatMessageDTO;
import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ChatService;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.utils.OnlineUserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理私聊消息 - 使用@MessageMapping和@SendToUser
     * 前端发送到: /app/chat/private
     * 服务器发送到: /user/{userId}/queue/messages
     */
    @MessageMapping("/chat/private")
    @SendToUser("/queue/messages")
    public ChatMessageDTO handlePrivateMessage(@Payload ChatMessageDTO dto) {
        log.info("收到私聊消息: from={}, to={}, content={}", dto.getFromUserId(), dto.getToUserId(), dto.getContent());
        
        try {
            // 1. 保存消息到数据库
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setFromUserId(dto.getFromUserId());
            chatMsg.setToUserId(dto.getToUserId());
            chatMsg.setContent(dto.getContent());
            chatMsg.setIsRead(0);
            chatMsg.setCreatedAt(LocalDateTime.now());
            
            chatService.sendMessage(chatMsg);
            
            // 2. 补充返回的消息DTO信息
            User fromUser = userMapper.selectById(dto.getFromUserId());
            User toUser = userMapper.selectById(dto.getToUserId());
            
            String fromUserName = "用户" + dto.getFromUserId();
            String toUserName = "用户" + dto.getToUserId();
            
            if (fromUser != null) {
                if (fromUser.getNickname() != null && !fromUser.getNickname().isEmpty()) {
                    fromUserName = fromUser.getNickname();
                } else {
                    fromUserName = fromUser.getUsername();
                }
            }
            
            if (toUser != null) {
                if (toUser.getNickname() != null && !toUser.getNickname().isEmpty()) {
                    toUserName = toUser.getNickname();
                } else {
                    toUserName = toUser.getUsername();
                }
            }
            
            dto.setId(chatMsg.getId());
            dto.setFromUserName(fromUserName);
            dto.setToUserName(toUserName);
            dto.setTimestamp(System.currentTimeMillis());
            dto.setCreatedAt(chatMsg.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dto.setType(0);
            
            // 3. 发送给接收者
            messagingTemplate.convertAndSendToUser(
                    dto.getToUserId().toString(),
                    "/queue/messages",
                    dto
            );
            
            return dto;
        } catch (Exception e) {
            log.error("处理私聊消息异常", e);
            return dto;
        }
    }

    /**
     * 处理在线状态消息
     * 前端发送到: /app/chat/online
     */
    @MessageMapping("/chat/online")
    public void handleOnlineStatus(@Payload ChatMessageDTO dto) {
        log.info("用户上线: userId={}", dto.getFromUserId());
        
        // 记录用户在线状态
        OnlineUserManager.userOnline(dto.getFromUserId());
        log.info("当前在线人数: {}", OnlineUserManager.getOnlineCount());
        
        ChatMessageDTO onlineMsg = new ChatMessageDTO();
        onlineMsg.setFromUserId(dto.getFromUserId());
        onlineMsg.setType(1); // 系统消息
        onlineMsg.setContent("用户已上线");
        onlineMsg.setTimestamp(System.currentTimeMillis());
        
        // 广播到所有连接的用户
        messagingTemplate.convertAndSend("/topic/online", onlineMsg);
    }

    /**
     * 处理离线状态消息
     * 前端发送到: /app/chat/offline
     */
    @MessageMapping("/chat/offline")
    public void handleOfflineStatus(@Payload ChatMessageDTO dto) {
        log.info("用户离线: userId={}", dto.getFromUserId());
        
        // 记录用户离线状态
        OnlineUserManager.userOffline(dto.getFromUserId());
        log.info("当前在线人数: {}", OnlineUserManager.getOnlineCount());
        
        ChatMessageDTO offlineMsg = new ChatMessageDTO();
        offlineMsg.setFromUserId(dto.getFromUserId());
        offlineMsg.setType(1); // 系统消息
        offlineMsg.setContent("用户已离线");
        offlineMsg.setTimestamp(System.currentTimeMillis());
        
        // 广播到所有连接的用户
        messagingTemplate.convertAndSend("/topic/online", offlineMsg);
    }
}
