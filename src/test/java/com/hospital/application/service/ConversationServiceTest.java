// src/main/java/com/hospital/application/service/ConversationService.java
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
import com.hospital.application.common.exceptions.ForbiddenException;
import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.ConversationResponse;
import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;

class ConversationServiceTest {

    ConversationRepositoryPort conversationRepositoryPort 
        = mock(ConversationRepositoryPort.class);
    UserRepositoryPort userRepositoryPort 
        = mock(UserRepositoryPort.class);
    ConversationService conversationService 
        = new ConversationService(conversationRepositoryPort, userRepositoryPort);

    @Test
    void listByUser_maps_models_to_dtos() {
        var userId = UUID.randomUUID();
        var conversationModel = new ConversationModel(UUID.randomUUID(), List.of(userId), ConversationStatus.ACTIVE, LocalDateTime.now(), null);

        when(conversationRepositoryPort.findAllByUserId(userId)).thenReturn(List.of(conversationModel));

        List<ConversationResponse> list = conversationService.listByUser(userId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).conversationID())
            .isEqualTo(conversationModel.conversationID());
        assertThat(list.get(0).conversationStatus())
            .isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void closeConversation_happy_path() {
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();
        var active = new ConversationModel(conversationID, List.of(patientId, doctorId), ConversationStatus.ACTIVE, LocalDateTime.now().minusHours(1), null);

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(active));
        when(userRepositoryPort.findById(patientId)).thenReturn(Optional.of(new UserModel(patientId, "Hasta", Set.of(UserRole.PATIENT))));
        when(conversationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = conversationService.closeConversation(conversationID, patientId);

        assertThat(resp.conversationStatus()).isEqualTo(ConversationStatus.CLOSED);
        assertThat(resp.conversationClosedAt()).isNotNull();
    }

    @Test
    void closeConversation_not_found_throws() {
        var conversationID = UUID.randomUUID();
        var actorId = UUID.randomUUID();

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> conversationService.closeConversation(conversationID, actorId))
            .isInstanceOf(NotFoundException.class);

        var active = new ConversationModel(
            conversationID, 
            List.of(actorId), 
            ConversationStatus.ACTIVE, 
            LocalDateTime.now(), 
            null
        );

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(active));
        when(userRepositoryPort.findById(actorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.closeConversation(conversationID, actorId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void closeConversation_only_participant_and_patient() {
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var strangerId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();
        var active = new ConversationModel(
            conversationID, 
            List.of(patientId, doctorId), 
            ConversationStatus.ACTIVE,
            LocalDateTime.now(), 
            null
        );

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(active));
        when(userRepositoryPort.findById(strangerId)).thenReturn(Optional.of(new UserModel(strangerId, "X", Set.of(UserRole.PATIENT))));

        assertThatThrownBy(() -> conversationService.closeConversation(conversationID, strangerId))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not a participant");

        when(userRepositoryPort.findById(doctorId)).thenReturn(Optional.of(new UserModel(doctorId, "D", Set.of(UserRole.DOCTOR))));
        assertThatThrownBy(() -> conversationService.closeConversation(conversationID, doctorId))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("Only the patient");
    }

    @Test
    void closeConversation_already_closed_bad_request() {
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var closed = new ConversationModel(conversationID, List.of(patientId), ConversationStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());

        when(conversationRepositoryPort.findById(conversationID)).thenReturn(Optional.of(closed));
        when(userRepositoryPort.findById(patientId)).thenReturn(Optional.of(new UserModel(patientId, "P", Set.of(UserRole.PATIENT))));

        assertThatThrownBy(() -> conversationService.closeConversation(conversationID, patientId))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("already closed");
    }
}