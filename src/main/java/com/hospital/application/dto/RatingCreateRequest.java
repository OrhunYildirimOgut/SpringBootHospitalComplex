// src/main/java/com/hospital/application/dto/RatingCreateRequest.java
package com.hospital.application.dto;

import java.util.UUID;

public record RatingCreateRequest(
    @jakarta.validation.constraints.NotNull 
    UUID conversationId,

    @jakarta.validation.constraints.NotNull 
    UUID patientId,

    @jakarta.validation.constraints.NotNull
    UUID doctorId,

    @jakarta.validation.constraints.Min(1) 
    @jakarta.validation.constraints.Max(5)
    int score
) {}
