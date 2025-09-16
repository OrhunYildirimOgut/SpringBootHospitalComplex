// src/main/java/com/hospital/application/service/MessageService.java
package com.hospital.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hospital.application.common.exceptions.BadRequestException;
import com.hospital.application.common.exceptions.ForbiddenException;
import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.MessageResponse;
import com.hospital.application.dto.MessageSendRequest;
import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.application.port.MessageRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.model.MessageModel;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;

@org.springframework.transaction.annotation.Transactional
@Service
public class MessageService {

    private final MessageRepositoryPort messageRepositoryPort;
    private final ConversationRepositoryPort conversationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public MessageService
    (
        MessageRepositoryPort messageRepositoryPort,
        ConversationRepositoryPort conversationRepositoryPort,
        UserRepositoryPort userRepositoryPort
    ) {
        this.messageRepositoryPort = messageRepositoryPort;
        this.conversationRepositoryPort = conversationRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    // Send message from patient to doctor with name
    public MessageResponse sendByPatientToDoctorName(String doctorName, UUID patientID, String content) {
        if (doctorName == null || doctorName.isBlank()) {
            throw new BadRequestException("Doctor name cannot be blank.");
        }
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Message content cannot be empty.");
        }

        UserModel patient = userRepositoryPort.findById(patientID)
                .orElseThrow(() -> new NotFoundException("No patient found."));

        if (!patient.userRoles().contains(UserRole.PATIENT)) {
            throw new ForbiddenException("Only the patient can initiate a message.");
        }

        List<UserModel> doctors = userRepositoryPort.findByNameAndRole(doctorName, UserRole.DOCTOR);
        if (doctors.isEmpty()) {
            throw new NotFoundException("No doctor found with this name: " + doctorName);
        }

        if (doctors.size() > 1) {
            throw new BadRequestException("There is more than one doctor with the same name, please clarify.");
        }

        UserModel doctor = doctors.get(0);

        ConversationModel conversationModel = conversationRepositoryPort
            .findActiveBetween(patient.userID(), doctor.userID())
            .orElseGet(() -> {
                ConversationModel created = new ConversationModel(
                    UUID.randomUUID(),
                    List.of(patient.userID(), doctor.userID()),
                    ConversationStatus.ACTIVE,
                    LocalDateTime.now(),
                    null
                );
                return conversationRepositoryPort.save(created);
        });

        MessageModel message = new MessageModel(
            UUID.randomUUID(),
            patient.userID(),
            content,
            LocalDateTime.now()
        );
        MessageModel saved = messageRepositoryPort.save(message, conversationModel.conversationID());

        return new MessageResponse(
            saved.messageID(), saved.authorID(), saved.messageContext(), saved.messageCreatedAt()
        );
    }

    // Send message from existing conversation
    public MessageResponse sendMessage(UUID conversationID, MessageSendRequest messageSendRequest) {
        if (messageSendRequest == null || messageSendRequest.messageAuthorId() == null) {
            throw new BadRequestException("authorID is required.");
        }
        if (messageSendRequest.messageContent() == null || messageSendRequest.messageContent().isBlank()) {
            throw new BadRequestException("Message content cannot be empty.");
        }

        ConversationModel conversationModel = conversationRepositoryPort.findById(conversationID)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversationModel.conversationStatus() != ConversationStatus.ACTIVE) {
            throw new BadRequestException("Conversation is closed, messages cannot be sent.");
        }
        if (!conversationModel.conversationUsersList().contains(messageSendRequest.messageAuthorId())) {
            throw new ForbiddenException("You are not a participant in this conversation.");
        }

        UserModel author = userRepositoryPort.findById(messageSendRequest.messageAuthorId())
                .orElseThrow(() -> new NotFoundException("Author user not found"));

        if (author.userRoles().contains(UserRole.DOCTOR) && messageRepositoryPort.findByConversationID(conversationID).isEmpty())
        {
            throw new ForbiddenException("The doctor cannot initiate the conversation.");
        }

        MessageModel messageModel = new MessageModel(
            UUID.randomUUID(),
            messageSendRequest.messageAuthorId(),
            messageSendRequest.messageContent(),
            LocalDateTime.now()
        );
        MessageModel saved = messageRepositoryPort.save(messageModel, conversationID);

        return new MessageResponse(
            saved.messageID(),
            saved.authorID(),
            saved.messageContext(),
            saved.messageCreatedAt()
        );
    }

    // List all messages from existing conversation
    public List<MessageResponse> listMessages(UUID conversationID) {
        List<MessageModel> messageModelList = messageRepositoryPort.findByConversationID(conversationID);
        List<MessageResponse> messageResponseList = new ArrayList<>();

        for (MessageModel messageModel : messageModelList) {
            messageResponseList.add(
                new MessageResponse(
                    messageModel.messageID(),
                    messageModel.authorID(),
                    messageModel.messageContext(),
                    messageModel.messageCreatedAt()
                )
            );
        }
        return messageResponseList;
    }
}
