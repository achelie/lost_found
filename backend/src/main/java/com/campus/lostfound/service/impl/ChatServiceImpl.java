package com.campus.lostfound.service.impl;

import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ChatMessageRepository;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getConversationMessages(Long userId1, Long userId2) {
        return chatMessageRepository.findConversationMessages(userId1, userId2);
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId) {
        List<ChatMessage> allMessages = chatMessageRepository.findAllUserMessages(userId);
        Map<Long, Map<String, Object>> conversationMap = new LinkedHashMap<>();

        for (ChatMessage msg : allMessages) {
            Long otherUserId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            
            if (!conversationMap.containsKey(otherUserId)) {
                User otherUser = userMapper.selectById(otherUserId);
                String displayName = "用户" + otherUserId;
                if (otherUser != null) {
                    if (otherUser.getNickname() != null && !otherUser.getNickname().isEmpty()) {
                        displayName = otherUser.getNickname();
                    } else {
                        displayName = otherUser.getUsername();
                    }
                }
                
                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", otherUserId);
                conv.put("nickName", displayName);
                conv.put("lastMessage", msg.getContent());
                conv.put("lastTime", msg.getCreatedAt());
                conv.put("unread", 0);
                
                // 只有当这条消息是对方发给我的时候，才显示这条消息
                if (!msg.getFromUserId().equals(userId)) {
                    conversationMap.put(otherUserId, conv);
                } else {
                    // 如果最新的消息是我发的，那就先暂时放入，但会被对方发给我的消息覆盖
                    conv.put("lastMessage", "[我] " + msg.getContent());
                    conversationMap.put(otherUserId, conv);
                }
            }

            // 统计未读消息
            if (msg.getToUserId().equals(userId) && msg.getIsRead() == 0) {
                Map<String, Object> conv = conversationMap.get(otherUserId);
                conv.put("unread", (int) conv.get("unread") + 1);
            }
        }

        return new ArrayList<>(conversationMap.values());
    }

    @Override
    public int getUnreadCount(Long userId) {
        return chatMessageRepository.countByToUserIdAndIsRead(userId, 0);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long fromUserId, Long toUserId) {
        chatMessageRepository.markAsRead(fromUserId, toUserId);
    }
}