package com.chatify.chat_backend.repository;

import com.chatify.chat_backend.entity.ChatMessage;
import com.chatify.chat_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    // Get all messages sent by a specific user
    List<ChatMessage> findBySender(User sender);

    // Get all messages received by a specific user
    List<ChatMessage> findByReceiver(User receiver);

    // Get the conversation between two users, ordered by timestamp
    List<ChatMessage> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestamp(
            User sender1, User receiver1,
            User sender2, User receiver2
    );
}
