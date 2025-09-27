package com.chatify.chat_backend.config;

import com.chatify.chat_backend.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    // This method is called by spring at startup to register WebSocket / STOMP endpoints
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // This is the handshake URL where the HTTP request upgrades to a WebSocket connection.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


    @Override
    // Configure how messages are routed
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Prefix for messages from client to server (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Enable simple in-memory broker for broadcasting messages
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix for sending messages to specific users
        registry.setUserDestinationPrefix("/user");
    }


    @Override
// Configure inbound messages from clients before reaching controllers
    public void configureClientInboundChannel(ChannelRegistration registration) {

        // Intercept every incoming WebSocket/STOMP message
        registration.interceptors(new ChannelInterceptor() {

            @Override
            // Called before a message is actually sent to the message channel
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                // Access STOMP headers and command from the message
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                // Handle authentication only on CONNECT command (initial handshake)
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // Read the "Authorization" header (expects "Bearer <JWT>")
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {

                        // Extract token from header
                        String token = authHeader.substring(7);

                        // Extract email/username from token and validate it
                        String email = jwtUtil.extractUsername(token);
                        if (email != null && jwtUtil.isTokenValid(token, email)) {

                            // Create Spring Security Authentication object for this session
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(email, null, null);

                            // Set authenticated user in WebSocket session
                            accessor.setUser(auth);

                            // Allow connection to proceed
                            return message;
                        }
                    }

                    // Invalid or missing token → reject connection
                    return null;
                }

                // For other STOMP commands (SEND, SUBSCRIBE, etc.), allow message
                return message;
            }
        });
    }
}
