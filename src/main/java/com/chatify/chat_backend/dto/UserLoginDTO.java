package com.chatify.chat_backend.dto;

import lombok.Data;

@Data
public class UserLoginDTO {

    private String email;

    private String password;
}
