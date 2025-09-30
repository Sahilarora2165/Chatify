package com.chatify.chat_backend.controller;

import com.chatify.chat_backend.entity.User;
import com.chatify.chat_backend.service.UserService;
import com.chatify.chat_backend.dto.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;

    public ChatController(SimpMessageSendingOperations messagingTemplate, UserService userService){
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/sendMessage")
    @Transactional
    public void sendMessage(@Payload Message message) {/*
        if (!userService.existsByEmail(message.getRecipientEmail())) {
            throw new IllegalArgumentException("Recipient does not exist: " + message.getRecipientEmail());
        }*/
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(message.getRecipientEmail(), "/queue/messages", message);
    }
}
