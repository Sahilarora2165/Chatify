package com.chatify.chat_backend.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {

    private String username;

    private String email;

    private String password;
}
