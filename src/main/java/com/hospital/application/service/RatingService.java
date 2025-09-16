// src/main/java/com/hospital/application/service/RatingService.java
package com.hospital.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hospital.application.common.exceptions.BadRequestException;
import com.hospital.application.common.exceptions.ConflictException;
import com.hospital.application.common.exceptions.ForbiddenException;
import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.DoctorRatingResponse;
import com.hospital.application.dto.RatingCreateRequest;
import com.hospital.application.dto.RatingResponse;
import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.application.port.RatingRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.model.RatingModel;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;

@org.springframework.transaction.annotation.Transactional
@Service
public class RatingService {

    private final RatingRepositoryPort ratingRepositoryPort;
    private final ConversationRepositoryPort conversationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public RatingService(
        RatingRepositoryPort ratingRepositoryPort,
        ConversationRepositoryPort conversationRepositoryPort,
        UserRepositoryPort userRepositoryPort
    ) {
        this.ratingRepositoryPort = ratingRepositoryPort;
        this.conversationRepositoryPort = conversationRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    // Patient create rating for doctor between 1-5
    public RatingResponse create(RatingCreateRequest ratingCreateRequest) {

        if (ratingCreateRequest.score() < 1 || ratingCreateRequest.score() > 5)
            throw new BadRequestException("The score must be between 1 and 5.");

        ConversationModel conversationModel = conversationRepositoryPort.findById(ratingCreateRequest.conversationId())
            .orElseThrow(() -> new NotFoundException("Conversation not found"));

        // Katılımcı ve rol kontrolleri
        UserModel patient = userRepositoryPort.findById(ratingCreateRequest.patientId())
            .orElseThrow(() -> new NotFoundException("Patient not found"));

        UserModel doctor = userRepositoryPort.findById(ratingCreateRequest.doctorId())
            .orElseThrow(() -> new NotFoundException("Doctor not found"));

        if (!patient.userRoles().contains(UserRole.PATIENT))
            throw new ForbiddenException("Only the patient can give a score.");
        if (!doctor.userRoles().contains(UserRole.DOCTOR))
            throw new BadRequestException("The target user is not a doctor.");

        if (!conversationModel.conversationUsersList().contains(ratingCreateRequest.patientId()) ||
            !conversationModel.conversationUsersList().contains(ratingCreateRequest.doctorId()))
            throw new ForbiddenException("The patient and doctor should be participants in this conversation.");

        if (conversationModel.conversationStatus() != ConversationStatus.CLOSED)
            throw new BadRequestException("Points cannot be awarded until the messaging has ended.");

        ratingRepositoryPort.findByConversationAndPatient(ratingCreateRequest.conversationId(), ratingCreateRequest.patientId())
            .ifPresent(r -> { throw new ConflictException("You have already given points for this conversation."); });

        RatingModel saved = ratingRepositoryPort.save(
            new RatingModel(
                UUID.randomUUID(),
                ratingCreateRequest.conversationId(),
                ratingCreateRequest.patientId(),
                ratingCreateRequest.doctorId(),
                ratingCreateRequest.score(),
                LocalDateTime.now()
            )
        );

        return new RatingResponse(
            saved.ratingId(), 
            saved.conversationId(), 
            saved.patientId(),
            saved.doctorId(), 
            saved.score(), 
            saved.createdAt()
        );
    }

    // Return avarage socre for doctor
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DoctorRatingResponse doctorRating(UUID doctorId) {
        userRepositoryPort.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        Double avg = ratingRepositoryPort.averageForDoctor(doctorId);
        long count = ratingRepositoryPort.countForDoctor(doctorId);
        double rounded = (avg == null) ? 0.0 :
            BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new DoctorRatingResponse(doctorId, rounded, count);
    }
}
