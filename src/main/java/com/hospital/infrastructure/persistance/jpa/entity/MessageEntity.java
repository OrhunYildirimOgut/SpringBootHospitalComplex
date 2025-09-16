// src/main/java/com/hospital/infrastructure/persistence/jpa/entity/MessageEntity.java
package com.hospital.infrastructure.persistance.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class MessageEntity {
    
    @Id
    @Column(nullable = false, updatable = false)
    private UUID messageID;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conversation_id")
    private ConversationEntity messageConversation;

    @ManyToOne(optional = false) @JoinColumn(name = "author_id")
    private UserEntity messageAuthor;

    @Column(nullable = false, length = 1000)
    private String messageContext;

    private LocalDateTime messageCreatedAt;

    public static MessageEntity createMessageEntity
    (
        UUID messageID,
        ConversationEntity messageConversation,
        UserEntity messageAuthor,
        String messageContext,
        LocalDateTime messageCreatedAt
    ) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessageID(messageID);
        messageEntity.setConversation(messageConversation);
        messageEntity.setAuthor(messageAuthor);
        messageEntity.setMessageContext(messageContext);
        messageEntity.setMessageCreatedAt(messageCreatedAt);
        return messageEntity;
    }

    public UUID getMessageID(){ return messageID; }
    public ConversationEntity getConversation(){ return messageConversation; }
    public UserEntity getAuthor(){ return messageAuthor; }
    public String getMessageContext(){ return messageContext; }
    public LocalDateTime getMessageCreatedAt(){ return messageCreatedAt; }

    public void setMessageID(UUID messageID){ this.messageID = messageID; }
    public void setConversation(ConversationEntity messageConversation){ this.messageConversation = messageConversation; }
    public void setAuthor(UserEntity messageAuthor){ this.messageAuthor = messageAuthor; }
    public void setMessageContext(String messageContext){ this.messageContext = messageContext; }
    public void setMessageCreatedAt(LocalDateTime messageCreatedAt){ this.messageCreatedAt = messageCreatedAt; }

    protected MessageEntity() {}
}
