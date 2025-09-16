// src/main/java/com/hospital/domain/model/RatingModel.java
package com.hospital.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record RatingModel(
    UUID ratingId,
    UUID conversationId,
    UUID patientId,
    UUID doctorId,
    int score,
    LocalDateTime createdAt
) {}