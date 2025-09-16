// src/main/java/com/hospital/application/dto/ConversationResponse.java
package com.hospital.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.hospital.domain.status.ConversationStatus;


public record ConversationResponse(
    UUID conversationID,
    List<UUID> participantIdList,
    ConversationStatus conversationStatus,          
    LocalDateTime conversationCreatedAt,
    LocalDateTime conversationClosedAt
) {}
