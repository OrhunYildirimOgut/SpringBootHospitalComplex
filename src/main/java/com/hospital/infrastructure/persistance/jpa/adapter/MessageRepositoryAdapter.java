// src/main/java/com/hospital/infrastructure/persistence/jpa/adapter/MessageRepositoryAdapter.java
package com.hospital.infrastructure.persistance.jpa.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.application.port.MessageRepositoryPort;
import com.hospital.domain.model.MessageModel;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.MessageEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaMessageRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;

@Component
public class MessageRepositoryAdapter implements MessageRepositoryPort {

    private final JpaMessageRepository jpaMessageRepository;
    private final JpaConversationRepository jpaConversationRepository;
    private final JpaUserRepository jpaUserRepository;

    public MessageRepositoryAdapter(
        JpaMessageRepository jpaMessageRepository,                            
        JpaConversationRepository jpaConversationRepository,
        JpaUserRepository jpaUserRepository
    ){
        this.jpaMessageRepository = jpaMessageRepository;
        this.jpaConversationRepository = jpaConversationRepository;
        this.jpaUserRepository = jpaUserRepository;
    }

    // Save message entity with using conversationID
    @Override
    public MessageModel save(MessageModel messageModel, UUID conversationID){
        ConversationEntity conversationEntity = jpaConversationRepository
            .findById(conversationID).orElseThrow();
        UserEntity author = jpaUserRepository
            .findById(messageModel.authorID()).orElseThrow();

        MessageEntity entity = MessageEntity.createMessageEntity(
            messageModel.messageID(),
            conversationEntity,
            author,
            messageModel.messageContext(),
            messageModel.messageCreatedAt()
        );

        MessageEntity saved = jpaMessageRepository.save(entity);

        return new MessageModel(
            saved.getMessageID(),
            saved.getAuthor().getUserID(),
            saved.getMessageContext(),
            saved.getMessageCreatedAt()
        );
    }

    // take all messages using JpaMessageRepository
    @Override
    public List<MessageModel> findByConversationID(UUID conversationID){

        List<MessageEntity> messageEntityList =
        jpaMessageRepository.findByMessageConversation_ConversationIDOrderByMessageCreatedAtAsc(conversationID);

        List<MessageModel> messageModelList = new ArrayList<>();

        for (MessageEntity messageEntity : messageEntityList){
            messageModelList.add(new MessageModel(
                messageEntity.getMessageID(),
                messageEntity.getAuthor().getUserID(),
                messageEntity.getMessageContext(),
                messageEntity.getMessageCreatedAt()
            ));
        }
        return messageModelList;
    }

    @Override
    public void deleteAll() {
        try {
            jpaMessageRepository.deleteAllInBatch(); 
        }
        catch (UnsupportedOperationException exception) {
            jpaMessageRepository.deleteAll(); 
        }
    }
}
