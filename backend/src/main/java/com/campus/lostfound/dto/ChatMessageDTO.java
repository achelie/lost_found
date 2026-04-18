package com.campus.lostfound.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private Long id;
    
    private Long fromUserId;
    
    private String fromUserName;
    
    private Long toUserId;
    
    private String toUserName;
    
    private String content;
    
    private Integer type; // 0: 普通消息 1: 系统消息
    
    private Long timestamp;
    
    private String createdAt;
}
