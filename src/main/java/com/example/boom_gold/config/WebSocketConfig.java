package com.example.boom_gold.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {


    @Resource
    DefaultHandler defaultHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(defaultHandler,"/ws")
                .addInterceptors()
                .setAllowedOrigins("*");
    }
}
