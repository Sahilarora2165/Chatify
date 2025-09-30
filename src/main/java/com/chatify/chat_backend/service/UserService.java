package com.chatify.chat_backend.service;

import com.chatify.chat_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Boolean existsByEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }
}
