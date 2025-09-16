// src/main/java/com/hospital/application/port/ConversationRepositoryPort.java
package com.hospital.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hospital.domain.model.ConversationModel;
// Interface for conversation
public interface ConversationRepositoryPort {
    ConversationModel save(ConversationModel conversation);
    Optional<ConversationModel> findById(UUID conversationID);
    List<ConversationModel> findAllByUserId(UUID userID);
    Optional<ConversationModel> findActiveBetween(UUID userA, UUID userB);
    void deleteAll();
}
