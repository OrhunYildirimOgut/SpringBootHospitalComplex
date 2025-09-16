// src/main/java/com/hospital/application/dto/MessageResponse.java
package com.hospital.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID messageID,
    UUID messageAuthorId,
    String messageContent,
    LocalDateTime messageCreatedAt
) {}
