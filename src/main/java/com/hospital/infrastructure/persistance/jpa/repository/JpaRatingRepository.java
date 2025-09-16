// src/main/java/com/hospital/infrastructure/persistance/jpa/repository/JpaRatingRepository.java
package com.hospital.infrastructure.persistance.jpa.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hospital.infrastructure.persistance.jpa.entity.RatingEntity;

public interface JpaRatingRepository extends JpaRepository<RatingEntity, UUID> {
    Optional<RatingEntity> findByConversation_ConversationIDAndPatient_UserID(UUID conversationId, UUID patientId);
    long countByDoctor_UserID(UUID doctorId);
    List<RatingEntity> findByDoctor_UserID(UUID doctorId);
}
