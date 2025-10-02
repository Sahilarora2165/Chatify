package com.chatify.chat_backend.controller;

import com.chatify.chat_backend.entity.ChatMessage;
import com.chatify.chat_backend.entity.User;
import com.chatify.chat_backend.repository.ChatMessageRepository;
import com.chatify.chat_backend.repository.UserRepository;
import com.chatify.chat_backend.service.UserService;
import com.chatify.chat_backend.dto.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatController(
            SimpMessageSendingOperations messagingTemplate,
            UserService userService,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/sendMessage")
    @Transactional
    public void sendMessage(@Payload Message message, Principal principal) {

        // ✅ Validate if principal exists (when auth is enabled)
        if (principal != null) {
            String authenticatedEmail = principal.getName();

            // Validate sender email matches authenticated user
            if (!authenticatedEmail.equals(message.getSenderEmail())) {
                throw new SecurityException("Sender email does not match authenticated user");
            }
        }

        // Validate recipient exists
        if (!userService.existsByEmail(message.getRecipientEmail())) {
            throw new IllegalArgumentException("Recipient does not exist: " + message.getRecipientEmail());
        }

        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(
                message.getRecipientEmail(),
                "/queue/messages",
                message
        );
    }
}
