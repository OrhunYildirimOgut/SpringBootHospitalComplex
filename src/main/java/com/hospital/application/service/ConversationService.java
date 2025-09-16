// src/main/java/com/hospital/application/service/ConversationService.java
package com.hospital.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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


@org.springframework.transaction.annotation.Transactional
@Service
public class ConversationService {

    private final ConversationRepositoryPort conversationRepository;
    private final UserRepositoryPort userRepository;

    public ConversationService
    (
        ConversationRepositoryPort conversationRepository,   
        UserRepositoryPort userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    // ListByUser(UUID userId) -> return all Converstation about user
    public List<ConversationResponse> listByUser(UUID userId) {
        return conversationRepository.findAllByUserId(userId).stream()
            .map(
                conversation -> new ConversationResponse(
                conversation.conversationID(),
                conversation.conversationUsersList(),
                conversation.conversationStatus(),
                conversation.conversationCreatedAt(),
                conversation.conversationClosedAt()
            ))
            .collect(Collectors.toList());
    }

    // CloseConversation(UUID conversationID, UUID actorId) -> return all Converstation about user
    public ConversationResponse closeConversation(UUID conversationID, UUID actorId) {
        ConversationModel conversationModel = conversationRepository.findById(conversationID)
            .orElseThrow(() -> new NotFoundException("Conversation not found"));

        UserModel actor = userRepository.findById(actorId)
            .orElseThrow(() -> new NotFoundException("Actor user not found"));

        if (!conversationModel.conversationUsersList().contains(actorId)) {
            throw new ForbiddenException("You are not a participant in this conversation.");
        }
        if (!actor.userRoles().contains(UserRole.PATIENT)) {
            throw new ForbiddenException("Only the patient can shut down the conversation.");
        }
        if (conversationModel.conversationStatus() == ConversationStatus.CLOSED) {
            throw new BadRequestException("The conversation is already closed.");
        }

        ConversationModel closed = new ConversationModel(
            conversationModel.conversationID(), 
            conversationModel.conversationUsersList(),
            ConversationStatus.CLOSED, 
            conversationModel.conversationCreatedAt(), 
            LocalDateTime.now()
        );
        ConversationModel saved = conversationRepository.save(closed);

        return new ConversationResponse(
            saved.conversationID(), saved.conversationUsersList(), saved.conversationStatus(),
            saved.conversationCreatedAt(), saved.conversationClosedAt()
        );
    }
}
