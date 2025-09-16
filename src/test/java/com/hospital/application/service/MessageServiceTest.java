// src/main/java/com/hospital/application/service/MessageService.java
package com.hospital.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.hospital.application.common.exceptions.BadRequestException;
import com.hospital.application.common.exceptions.ForbiddenException;
import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.MessageSendRequest;
import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.application.port.MessageRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.model.MessageModel;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;

class MessageServiceTest {

    MessageRepositoryPort messageRepositoryPort = mock(MessageRepositoryPort.class);
    ConversationRepositoryPort conversationRepositoryPort = mock(ConversationRepositoryPort.class);
    UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    MessageService messageService = new MessageService(messageRepositoryPort, conversationRepositoryPort, userRepositoryPort);

    // --- sendByPatientToDoctorName ---
    @Test
    void sendByPatientToDoctorName_happy_path_reuses_existing_active_conversation() {
        var doctorName = "Doktor";
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();

        var patient = new UserModel(patientId, "P", Set.of(UserRole.PATIENT));
        var doctor = new UserModel(doctorId, "D", Set.of(UserRole.DOCTOR));

        when(userRepositoryPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(userRepositoryPort.findByNameAndRole(doctorName, UserRole.DOCTOR)).thenReturn(List.of(doctor));

        var existing = new ConversationModel(UUID.randomUUID(), List.of(patientId, doctorId), ConversationStatus.ACTIVE, LocalDateTime.now(), null);
        when(conversationRepositoryPort.findActiveBetween(patientId, doctorId)).thenReturn(Optional.of(existing));

        ArgumentCaptor<MessageModel> cap = ArgumentCaptor.forClass(MessageModel.class);
        when(messageRepositoryPort.save(cap.capture(), eq(existing.conversationID()))).thenAnswer(i -> i.getArgument(0));

        var resp = messageService.sendByPatientToDoctorName(doctorName, patientId, "hi");

        assertThat(resp.messageAuthorId()).isEqualTo(patientId);
        assertThat(resp.messageContent()).isEqualTo("hi");

        verify(conversationRepositoryPort, never()).save(any());
    }

    @Test
    void sendByPatientToDoctorName_creates_new_conversation_when_absent() {
        var doctorName = "Doktor";
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();

        var patient = new UserModel(patientId, "P", Set.of(UserRole.PATIENT));
        var doctor = new UserModel(doctorId, "D", Set.of(UserRole.DOCTOR));
        when(userRepositoryPort.findById(patientId)).thenReturn(Optional.of(patient));
        when(userRepositoryPort.findByNameAndRole(doctorName, UserRole.DOCTOR)).thenReturn(List.of(doctor));

        when(conversationRepositoryPort.findActiveBetween(patientId, doctorId)).thenReturn(Optional.empty());
        when(conversationRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
        when(messageRepositoryPort.save(any(), any())).thenAnswer(i -> i.getArgument(0));

        var response = messageService.sendByPatientToDoctorName(doctorName, patientId, "hey");

        assertThat(response.messageContent()).isEqualTo("hey");
        verify(conversationRepositoryPort).save(any(ConversationModel.class)); 
    }

    @Test
    void sendByPatientToDoctorName_validations_and_lookup_errors() {
        var patientID = UUID.randomUUID();

        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("", patientID, "x"))
            .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("D", patientID, ""))
            .isInstanceOf(BadRequestException.class);

        when(userRepositoryPort.findById(patientID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("D", patientID, "x"))
            .isInstanceOf(NotFoundException.class);

        when(userRepositoryPort.findById(patientID)).thenReturn(Optional.of(new UserModel(patientID, "X", Set.of(UserRole.DOCTOR))));
        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("D", patientID, "x"))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("Only the patient");

        when(userRepositoryPort.findById(patientID)).thenReturn(Optional.of(new UserModel(patientID, "P", Set.of(UserRole.PATIENT))));
        when(userRepositoryPort.findByNameAndRole("D", UserRole.DOCTOR)).thenReturn(List.of());
        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("D", patientID, "x"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("No doctor found");

        when(userRepositoryPort.findByNameAndRole("D", UserRole.DOCTOR)).thenReturn(List.of(
            new UserModel(UUID.randomUUID(), "D1", Set.of(UserRole.DOCTOR)),
            new UserModel(UUID.randomUUID(), "D2", Set.of(UserRole.DOCTOR))
        ));
        assertThatThrownBy(() -> messageService.sendByPatientToDoctorName("D", patientID, "x"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("more than one doctor");
    }

    // --- sendMessage ---
    @Test
    void sendMessage_happy_path() {
        var conversationID = UUID.randomUUID();
        var patientId = UUID.randomUUID();
        var doctorId = UUID.randomUUID();
        var request = new MessageSendRequest(patientId, "hi");

        var conversation = new ConversationModel(
            conversationID, 
            List.of(patientId, doctorId), 
            ConversationStatus.ACTIVE, 
            LocalDateTime.now(), 
            null
        );

        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(conversation));
        when(userRepositoryPort.findById(patientId))
            .thenReturn(Optional.of(new UserModel(patientId, "P", Set.of(UserRole.PATIENT))));
        when(messageRepositoryPort.findByConversationID(conversationID))
            .thenReturn(List.of(new MessageModel(UUID.randomUUID(), patientId, "prev", LocalDateTime.now())));
        when(messageRepositoryPort.save(any(), eq(conversationID)))
            .thenAnswer(i -> i.getArgument(0));

        var response = messageService.sendMessage(conversationID, request);

        assertThat(response.messageContent()).isEqualTo("hi");
        assertThat(response.messageAuthorId()).isEqualTo(patientId);
    }

    @Test
    void sendMessage_validations_and_rules() {
        var conversationID = UUID.randomUUID();

        assertThatThrownBy(() -> messageService.sendMessage(conversationID, null))
            .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> messageService.sendMessage(conversationID, new MessageSendRequest(null, "x")))
            .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> messageService.sendMessage(conversationID, new MessageSendRequest(UUID.randomUUID(), "")))
            .isInstanceOf(BadRequestException.class);

        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            messageService.sendMessage(conversationID, new MessageSendRequest(UUID.randomUUID(), "x")))
            .isInstanceOf(NotFoundException.class);

        var patientID = UUID.randomUUID();
        var doctorID = UUID.randomUUID();
        var closed = new ConversationModel(
            conversationID, 
            List.of(patientID, doctorID), 
            ConversationStatus.CLOSED, 
            LocalDateTime.now(), 
            LocalDateTime.now()
        );

        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> messageService.sendMessage(conversationID, new MessageSendRequest(patientID, "x")))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("closed");

        var active = new ConversationModel(
            conversationID, 
            List.of(patientID, doctorID), 
            ConversationStatus.ACTIVE, 
            LocalDateTime.now(), 
            null
        );

        when(conversationRepositoryPort.findById(conversationID))
            .thenReturn(Optional.of(active));
        assertThatThrownBy(() 
            -> messageService.sendMessage(conversationID, new MessageSendRequest(UUID.randomUUID(), "x")))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not a participant");

        when(userRepositoryPort.findById(patientID))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() 
            -> messageService.sendMessage(conversationID, new MessageSendRequest(patientID, "x")))
            .isInstanceOf(NotFoundException.class);

        when(userRepositoryPort.findById(doctorID)).
            thenReturn(Optional.of(new UserModel(doctorID, "D", Set.of(UserRole.DOCTOR))));
        when(messageRepositoryPort.findByConversationID(conversationID))
            .thenReturn(new ArrayList<>()); 
        assertThatThrownBy(() 
            -> messageService.sendMessage(conversationID, new MessageSendRequest(doctorID, "x")))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("doctor cannot initiate");
    }

    @Test
    void listMessages_maps_models_to_dtos() {
        var conversationID = UUID.randomUUID();
        var message1 = new MessageModel(UUID.randomUUID(), UUID.randomUUID(), "a", LocalDateTime.now());
        var message2 = new MessageModel(UUID.randomUUID(), UUID.randomUUID(), "b", LocalDateTime.now());
        
        when(messageRepositoryPort.findByConversationID(conversationID))
            .thenReturn(List.of(message1, message2));

        var list = messageService.listMessages(conversationID);
        assertThat(list).extracting(r -> r.messageContent()).containsExactly("a", "b");
    }
}