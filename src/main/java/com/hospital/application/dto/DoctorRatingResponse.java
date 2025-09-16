// src/main/java/com/hospital/application/dto/DoctorRatingResponse.java
package com.hospital.application.dto;

import java.util.UUID;

public record DoctorRatingResponse(
    UUID doctorId,
    double averageScore,
    long ratingCount
) {}
