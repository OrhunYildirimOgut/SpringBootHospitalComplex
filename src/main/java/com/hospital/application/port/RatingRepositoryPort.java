// src/main/java/com/hospital/application/port/RatingRepositoryPort.java
package com.hospital.application.port;

import java.util.Optional;
import java.util.UUID;

import com.hospital.domain.model.RatingModel;
// Interface for rating
public interface RatingRepositoryPort {
    RatingModel save(RatingModel rating);
    Optional<RatingModel> findByConversationAndPatient(UUID conversationId, UUID patientId);
    Double averageForDoctor(UUID doctorId);
    long countForDoctor(UUID doctorId);
}
