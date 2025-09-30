package com.chatify.chat_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private String senderEmail;
    private String recipientEmail;
    private String content;
    private LocalDateTime timestamp;
}
