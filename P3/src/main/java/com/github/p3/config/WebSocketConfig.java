package com.github.p3.config;

import com.github.p3.controller.ChatWebSocketHandler;
import com.github.p3.security.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/chat/room/{transactionId}") // WebSocket 엔드포인트 설정
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("http://localhost:3000", "https://auction-deploy-kappa.vercel.app");
    }

}
