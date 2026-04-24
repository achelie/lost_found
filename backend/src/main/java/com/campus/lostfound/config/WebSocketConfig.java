package com.campus.lostfound.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 开启简单消息代理：负责转发聊天和在线状态消息
        // /topic：广播（一对多）
        // /user：私聊（一对一）
        config.enableSimpleBroker("/topic", "/user");
        
        // 客户端发消息时统一走 /app 前缀，交给 @MessageMapping 方法处理
        config.setApplicationDestinationPrefixes("/app");
        
        // 一对一消息的目标前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 对外暴露 WebSocket 连接入口，前端会通过 SockJS 连接这里
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}