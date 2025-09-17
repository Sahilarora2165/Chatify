package com.chatify.chat_backend.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {

    private String token;

    private Long userId;

    private String username;

    private String email;

    public AuthResponseDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public AuthResponseDTO() {}
}
