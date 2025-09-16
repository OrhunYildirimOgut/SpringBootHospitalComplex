// src/main/java/com/hospital/domain/model/Conversation.java
package com.hospital.domain.model;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.hospital.domain.status.ConversationStatus;

public record ConversationModel(
    UUID conversationID,
    List<UUID> conversationUsersList,
    ConversationStatus conversationStatus,
    LocalDateTime conversationCreatedAt,
    LocalDateTime conversationClosedAt
) {}
