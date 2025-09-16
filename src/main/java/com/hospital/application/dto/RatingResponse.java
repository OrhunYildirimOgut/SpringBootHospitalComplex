// src/main/java/com/hospital/application/dto/RatingResponse.java
package com.hospital.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RatingResponse(
    UUID ratingId,
    UUID conversationId,
    UUID patientId,
    UUID doctorId,
    int score,
    LocalDateTime createdAt
) {}
