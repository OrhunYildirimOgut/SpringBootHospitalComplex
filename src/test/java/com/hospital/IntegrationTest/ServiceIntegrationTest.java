// src/test/java/com/hospital/IntegrationTest/ServiceIntegrationTest.java

package com.hospital.IntegrationTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.hospital.IntegrationTest.TestDataFactory.newDoctor;
import static com.hospital.IntegrationTest.TestDataFactory.newPatient;
import com.hospital.application.common.exceptions.BadRequestException;
import com.hospital.application.common.exceptions.ForbiddenException;
import com.hospital.application.dto.MessageResponse;
import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.application.port.MessageRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.application.service.ConversationService;
import com.hospital.application.service.MessageService;
import com.hospital.application.service.UserService;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.status.ConversationStatus;

public class ServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired 
    UserRepositoryPort userRepo;

    @Autowired 
    MessageRepositoryPort messageRepo;

    @Autowired 
    ConversationRepositoryPort convRepo;

    @Autowired 
    UserService userService;

    @Autowired 
    MessageService messageService;

    @Autowired 
    ConversationService conversationService;

    UserModel patient;
    UserModel doctor;

    @BeforeEach
    void setup() {
        messageRepo.deleteAll();
        convRepo.deleteAll();
        userRepo.deleteAll();

        patient = newPatient(userRepo, "Ali Hasta");
        doctor  = newDoctor(userRepo,  "Ahmet Cuhsin");
    }

    @Test
    void patient_first_message_creates_conversation_and_message() {
        MessageResponse resp = messageService.sendByPatientToDoctorName(
                "Ahmet Cuhsin", patient.userID(), "Merhaba hocam");

        assertThat(resp.messageContent()).isEqualTo("Merhaba hocam");

        var active = convRepo.findActiveBetween(patient.userID(), doctor.userID());
        assertThat(active).isPresent();
        assertThat(active.get().conversationStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void second_firstMessage_reuses_active_conversation() {
        var r1 = messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var c1 = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        var r2 = messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m2");
        var c2 = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        assertThat(c2.conversationID()).isEqualTo(c1.conversationID());
    }

    @Test
    void when_previous_closed_new_conversation_opens() {
        // 1) ilk konuşmayı aç ve kapat
        messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var conv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();
        var closed = conversationService.closeConversation(conv.conversationID(), patient.userID());
        assertThat(closed.conversationStatus()).isEqualTo(ConversationStatus.CLOSED);

        // 2) tekrar ilk mesaj → yeni konuşma
        var r2 = messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m2");
        var newConv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        assertThat(newConv.conversationID()).isNotEqualTo(conv.conversationID());
        assertThat(newConv.conversationStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void cannot_send_to_closed_conversation() {
        // konuşmayı yarat
        messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var conv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();
        conversationService.closeConversation(conv.conversationID(), patient.userID());

        // kapalıya atmayı dene
        assertThatThrownBy(() ->
            messageService.sendMessage(conv.conversationID(),
                new com.hospital.application.dto.MessageSendRequest(patient.userID(), "deneme"))
        ).isInstanceOf(BadRequestException.class)
         .hasMessageContaining("Conversation is closed, messages cannot be sent.");
    }

    @Test
    void non_participant_cannot_send() {
        // aktif konuşmayı aç
        messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var conv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        var stranger = newPatient(userRepo, "Yabancı Hasta");
        assertThatThrownBy(() ->
            messageService.sendMessage(conv.conversationID(),
                new com.hospital.application.dto.MessageSendRequest(stranger.userID(), "selam"))
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void doctor_cannot_start_conversation() {
        var emptyConv = convRepo.save(new ConversationModel(
        java.util.UUID.randomUUID(),      
        List.of(patient.userID(), doctor.userID()),
        ConversationStatus.ACTIVE,
        LocalDateTime.now(),
        null));

        assertThatThrownBy(() ->
            messageService.sendMessage(emptyConv.conversationID(),
                new com.hospital.application.dto.MessageSendRequest(doctor.userID(), "ilk mesaj doktor"))
        ).isInstanceOf(ForbiddenException.class)
         .hasMessageContaining("The doctor cannot initiate the conversation.");
    }

    @Test
    void patient_can_close_conversation_doctor_cannot() {
        messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var conv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        // doktor dener → 403
        assertThatThrownBy(() ->
            conversationService.closeConversation(conv.conversationID(), doctor.userID())
        ).isInstanceOf(ForbiddenException.class);

        // hasta kapatır → OK
        var closed = conversationService.closeConversation(conv.conversationID(), patient.userID());
        assertThat(closed.conversationStatus()).isEqualTo(ConversationStatus.CLOSED);

        // tekrar kapatmaya çalış → 400
        assertThatThrownBy(() ->
            conversationService.closeConversation(conv.conversationID(), patient.userID())
        ).isInstanceOf(BadRequestException.class);
    }

    @Test
    void messages_are_returned_in_chronological_order() {
        messageService.sendByPatientToDoctorName("Ahmet Cuhsin", patient.userID(), "m1");
        var conv = convRepo.findActiveBetween(patient.userID(), doctor.userID()).orElseThrow();

        messageService.sendMessage(conv.conversationID(),
                new com.hospital.application.dto.MessageSendRequest(doctor.userID(),  "m2"));
        messageService.sendMessage(conv.conversationID(),
                new com.hospital.application.dto.MessageSendRequest(patient.userID(), "m3"));

        var list = messageRepo.findByConversationID(conv.conversationID());
        assertThat(list).extracting(m -> m.messageContext()).containsExactly("m1", "m2", "m3");

        // zaman damgaları artan olsun
        assertThat(list.get(1).messageCreatedAt())
                .isAfterOrEqualTo(list.get(0).messageCreatedAt());
        assertThat(list.get(2).messageCreatedAt())
                .isAfterOrEqualTo(list.get(1).messageCreatedAt());
    }
}