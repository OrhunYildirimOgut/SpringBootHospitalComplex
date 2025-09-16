// src/main/java/com/hospital/infrastructure/persistence/jpa/adapter/ConversationRepositoryAdapter.java
package com.hospital.infrastructure.persistance.jpa.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.application.port.ConversationRepositoryPort;
import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.status.ConversationStatus;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;

@Component
public class ConversationRepositoryAdapter implements ConversationRepositoryPort {

    private final JpaConversationRepository jpaConversationRepository;
    private final JpaUserRepository jpaUserRepository;

    public ConversationRepositoryAdapter
    (
        JpaConversationRepository jpaConversationRepository,
        JpaUserRepository jpaUserRepository
    ){
        this.jpaConversationRepository = jpaConversationRepository;
        this.jpaUserRepository = jpaUserRepository;
    }

    // Create user entity from domain model
    @Override
    public ConversationModel save(ConversationModel conversationModel){
        List<UserEntity> participants = new ArrayList<>();

        for (UUID conversationUUID : conversationModel.conversationUsersList()) 
        {
            UserEntity user = jpaUserRepository.findById(conversationUUID)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + conversationUUID));
            participants.add(user);
        }

        ConversationEntity conversationEntity = ConversationEntity.createConversationEntity
        (
            conversationModel.conversationID(),
            participants,
            conversationModel.conversationStatus(),
            conversationModel.conversationCreatedAt(),
            conversationModel.conversationClosedAt()
        );

        ConversationEntity saved = jpaConversationRepository.save(conversationEntity);

        List<UUID> userUUIDList = new ArrayList<>();
        for (UserEntity userEntity : saved.getConversationEntityUserList()){
            userUUIDList.add(userEntity.getUserID());
        }

        return new ConversationModel(
            saved.getConversationID(),
            userUUIDList,
            saved.getConversationEntityStatus(),
            saved.getConversationEntityCreatedAt(),
            saved.getConversationEntityClosedAt()
        );
    }

    // Find conversation entity with ID
    @Override
    public Optional<ConversationModel> findById(UUID userID){
        return jpaConversationRepository.findById(userID).map(saved -> {
            List<UUID> userIds = saved.getConversationEntityUserList()
                                      .stream().map(UserEntity::getUserID).toList();
            return new ConversationModel(
                saved.getConversationID(),
                userIds,
                saved.getConversationEntityStatus(),
                saved.getConversationEntityCreatedAt(),
                saved.getConversationEntityClosedAt()
            );
        });
    }

    @Override
    public List<ConversationModel> findAllByUserId(UUID userID){
        List<ConversationEntity> conversationEntityList = jpaConversationRepository.findAllByUserId(userID);
        List<ConversationModel> ConversationModelList = new ArrayList<>();

        for (ConversationEntity conversationEntity : conversationEntityList){
            List<UUID> userIds = conversationEntity.getConversationEntityUserList()
                .stream().map(UserEntity::getUserID).toList();
            ConversationModelList.add(new ConversationModel(
                conversationEntity.getConversationID(),
                userIds,
                conversationEntity.getConversationEntityStatus(),
                conversationEntity.getConversationEntityCreatedAt(),
                conversationEntity.getConversationEntityClosedAt()
            ));
        }
        return ConversationModelList;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversationModel> findActiveBetween(UUID userA, UUID userB) {
        List<ConversationEntity> list =
            jpaConversationRepository.findBetweenWithStatus(userA, userB, ConversationStatus.ACTIVE);

        return list.stream().findFirst().map(saved -> {
            List<UUID> userIds = saved.getConversationEntityUserList()
                                      .stream().map(UserEntity::getUserID).toList();
            return new ConversationModel
            (
                saved.getConversationID(), userIds, saved.getConversationEntityStatus(),
                saved.getConversationEntityCreatedAt(), saved.getConversationEntityClosedAt()
            );
        });
    }

    @Override
    public void deleteAll() {
        try {
            jpaConversationRepository.deleteAllInBatch();
        }
        catch (UnsupportedOperationException ex) {
            jpaConversationRepository.deleteAll();
        }
    }
}
