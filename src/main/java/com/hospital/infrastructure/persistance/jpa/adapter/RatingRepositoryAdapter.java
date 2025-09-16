// src/main/java/com/hospital/infrastructure/persistance/jpa/adapter/RatingRepositoryAdapter.java
package com.hospital.infrastructure.persistance.jpa.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.application.port.RatingRepositoryPort;
import com.hospital.domain.model.RatingModel;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.RatingEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaRatingRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;

@Component
public class RatingRepositoryAdapter implements RatingRepositoryPort {

    private final JpaRatingRepository ratingRepo;
    private final JpaConversationRepository convRepo;
    private final JpaUserRepository userRepo;

    public RatingRepositoryAdapter(
        JpaRatingRepository ratingRepo,
        JpaConversationRepository convRepo,
        JpaUserRepository userRepo
    ) {
        this.ratingRepo = ratingRepo;
        this.convRepo = convRepo;
        this.userRepo = userRepo;
    }

    // Save RatingEntity for this 
    // with using use conversationId, patientId, doctorId, 
    // take ConversationEntity, UserEntity(patient), UserEntity(doctor) from db
    @Override
    public RatingModel save(RatingModel model) {
        ConversationEntity conv = convRepo.findById(model.conversationId())
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + model.conversationId()));

        UserEntity patient = userRepo.findById(model.patientId())
            .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + model.patientId()));

        UserEntity doctor = userRepo.findById(model.doctorId())
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + model.doctorId()));

        RatingEntity ratingEntity = new RatingEntity();
        ratingEntity.setRatingId(model.ratingId());
        ratingEntity.setConversation(conv);
        ratingEntity.setPatient(patient);
        ratingEntity.setDoctor(doctor);
        ratingEntity.setScore(model.score());
        ratingEntity.setCreatedAt(model.createdAt());

        RatingEntity saved = ratingRepo.save(ratingEntity);

        return new RatingModel(
            saved.getRatingId(),
            saved.getConversation().getConversationID(),
            saved.getPatient().getUserID(),
            saved.getDoctor().getUserID(),
            saved.getScore(),
            saved.getCreatedAt()
        );
    }

    // For one patient vote just ones in one conversation
    @Override
    public Optional<RatingModel> findByConversationAndPatient(UUID conversationId, UUID patientId) {
        return ratingRepo.findByConversation_ConversationIDAndPatient_UserID(conversationId, patientId)
            .map(saved -> new RatingModel(
                saved.getRatingId(),
                saved.getConversation().getConversationID(),
                saved.getPatient().getUserID(),
                saved.getDoctor().getUserID(),
                saved.getScore(),
                saved.getCreatedAt()
            ));
    }

    // Return avarage vote calculation for doctor
    @Override
    public Double averageForDoctor(UUID doctorId) {
        var list = ratingRepo.findByDoctor_UserID(doctorId);
        if (list.isEmpty()) return 0.0;
        return list.stream().mapToInt(RatingEntity::getScore).average().orElse(0.0);
    }

    // Return count of votes for doctor
    @Override
    public long countForDoctor(UUID doctorId) {
        return ratingRepo.countByDoctor_UserID(doctorId);
    }
}
