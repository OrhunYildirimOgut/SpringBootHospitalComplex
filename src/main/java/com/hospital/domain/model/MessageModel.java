// src/main/java/com/hospital/domain/model/Message.java
package com.hospital.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageModel(
    UUID messageID,
    UUID authorID,        
    String messageContext,
    LocalDateTime messageCreatedAt
) {}
