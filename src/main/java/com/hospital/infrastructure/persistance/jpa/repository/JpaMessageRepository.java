// src/main/java/com/hospital/infrastructure/persistence/jpa/repository/JpaMessageRepository.java
package com.hospital.infrastructure.persistance.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.infrastructure.persistance.jpa.entity.MessageEntity;

public interface JpaMessageRepository extends JpaRepository<MessageEntity, UUID> {
    List<MessageEntity> findByMessageConversation_ConversationIDOrderByMessageCreatedAtAsc(UUID conversationID);
}
