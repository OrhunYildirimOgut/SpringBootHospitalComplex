// src/main/java/com/hospital/application/service/RatingService.java
package com.hospital.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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

class RatingServiceTest {

    RatingRepositoryPort ratingRepositoryPort 
        = mock(RatingRepositoryPort.class);
    ConversationRepositoryPort conversationRepositoryPort 
        = mock(ConversationRepositoryPort.class);
    UserRepositoryPort userRepositoryPort 
        = mock(UserRepositoryPort.class);
    RatingService ratingService 
        = new RatingService(ratingRepositoryPort, conversationRepositoryPort, userRepositoryPort);

    @Test
    void create_happy_path() {
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();
        var request = new RatingCreateRequest(conversationID, patientId, doctorId, 5);

        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(new ConversationModel(
                conversationID, 
                List.of(patientId, doctorId), 
                ConversationStatus.CLOSED, 
                LocalDateTime.now(), 
                LocalDateTime.now()
        )));
        when(userRepositoryPort.findById(patientId))
            .thenReturn(Optional.of(new UserModel(patientId, "P", Set.of(UserRole.PATIENT))));
        when(userRepositoryPort.findById(doctorId))
            .thenReturn(Optional.of(new UserModel(doctorId, "D", Set.of(UserRole.DOCTOR))));
        when(ratingRepositoryPort.findByConversationAndPatient(conversationID, patientId))
            .thenReturn(Optional.empty());
        when(ratingRepositoryPort.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.create(request);

        assertThat(response.score()).isEqualTo(5);
        assertThat(response.conversationId()).isEqualTo(conversationID);
    }

    @Test
    void create_validations_and_rules() {
        
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();

        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 0)))
            .isInstanceOf(BadRequestException.class);

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(NotFoundException.class);

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(
            new ConversationModel(conversationID, List.of(patientId, doctorId), ConversationStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now())));
        when(userRepositoryPort.findById(patientId))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(NotFoundException.class);

        when(userRepositoryPort.findById(patientId))
            .thenReturn(Optional.of(new UserModel(patientId, "P", Set.of(UserRole.PATIENT))));
        when(userRepositoryPort.findById(doctorId))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(NotFoundException.class);

        when(userRepositoryPort.findById(doctorId))
        .thenReturn(Optional.of(new UserModel(doctorId, "X", Set.of(UserRole.PATIENT))));
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("not a doctor");

        when(userRepositoryPort.findById(doctorId))
            .thenReturn(Optional.of(new UserModel(doctorId, "D", Set.of(UserRole.DOCTOR))));
        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(new ConversationModel(conversationID, List.of(UUID.randomUUID(), doctorId), ConversationStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now())));
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("participants");

        //  CLOSED 
        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(
            new ConversationModel(conversationID, List.of(patientId, doctorId), ConversationStatus.ACTIVE, LocalDateTime.now(), null)));
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("until the messaging has ended");

        // duplicate
        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(
            new ConversationModel(conversationID, List.of(patientId, doctorId), ConversationStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now())));
        when(ratingRepositoryPort.findByConversationAndPatient(conversationID, patientId))
            .thenReturn(Optional.of(
            new RatingModel(UUID.randomUUID(), conversationID, patientId, doctorId, 5, LocalDateTime.now())));
        assertThatThrownBy(() -> ratingService.create(new RatingCreateRequest(conversationID, patientId, doctorId, 5)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void doctorRating_handles_null_average_and_rounding() {
        var doctorID = UUID.randomUUID();

        // doctor not found
        when(userRepositoryPort.findById(doctorID))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> ratingService.doctorRating(doctorID))
            .isInstanceOf(NotFoundException.class);

        // null avg -> 0.0
        when(userRepositoryPort.findById(doctorID))
            .thenReturn(Optional.of(new UserModel(doctorID, "D", Set.of(UserRole.DOCTOR))));
        when(ratingRepositoryPort.averageForDoctor(doctorID))
            .thenReturn(null);
        when(ratingRepositoryPort.countForDoctor(doctorID))
            .thenReturn(0L);

        DoctorRatingResponse doctorRatingResponse0 = ratingService.doctorRating(doctorID);
        assertThat(doctorRatingResponse0.averageScore()).isEqualTo(0.0);
        assertThat(doctorRatingResponse0.ratingCount()).isZero();

        // rounding
        when(ratingRepositoryPort.averageForDoctor(doctorID)).thenReturn(4.6666667);
        when(ratingRepositoryPort.countForDoctor(doctorID)).thenReturn(3L);

        DoctorRatingResponse r1 = ratingService.doctorRating(doctorID);

        assertThat(r1.averageScore()).isEqualTo(4.67);
        assertThat(r1.ratingCount()).isEqualTo(3L);
    }
}