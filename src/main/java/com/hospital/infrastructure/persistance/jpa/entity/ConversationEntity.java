// src/main/java/com/hospital/infrastructure/persistence/jpa/entity/ConversationEntity.java
package com.hospital.infrastructure.persistance.jpa.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hospital.domain.status.ConversationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversations")
public class ConversationEntity  {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID conversationID;

    @ManyToMany
    @JoinTable(
        name = "conversation_users",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> conversationUserList = new ArrayList<>();

    @Column(name = "status", nullable = false)
    private ConversationStatus conversationStatus;

    private LocalDateTime conversationCreatedAt;
    private LocalDateTime conversationClosedAt;

    public static ConversationEntity createConversationEntity
    (
        UUID conversationID,
        List<UserEntity> conversationUserList,
        ConversationStatus conversationStatus,
        LocalDateTime conversationCreatedAt,
        LocalDateTime conversationClosedAt
    ) {
        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setConversationEntityId(conversationID);
        conversationEntity.setConversationEntityUserList(conversationUserList);
        conversationEntity.setConversationEntityStatus(conversationStatus);
        conversationEntity.setConversationEntityCreatedAt(conversationCreatedAt);
        conversationEntity.setConversationEntityClosedAt(conversationClosedAt);
        return conversationEntity;
    }

    public UUID getConversationID(){ return conversationID; }
    public List<UserEntity> getConversationEntityUserList(){ return conversationUserList; }
    public ConversationStatus getConversationEntityStatus(){ return conversationStatus; }
    public LocalDateTime getConversationEntityCreatedAt(){ return conversationCreatedAt; }
    public LocalDateTime getConversationEntityClosedAt(){ return conversationClosedAt; }

    public void setConversationEntityId(UUID conversationID){ this.conversationID = conversationID; }
    public void setConversationEntityUserList(List<UserEntity> users){
        if (users == null) {
            this.conversationUserList = new ArrayList<>();
        } else {
            this.conversationUserList = users;
        }
    }
    public void setConversationEntityStatus(ConversationStatus conversationStatus){ this.conversationStatus = conversationStatus; }
    public void setConversationEntityCreatedAt(LocalDateTime conversationCreatedAt){ this.conversationCreatedAt = conversationCreatedAt; }
    public void setConversationEntityClosedAt(LocalDateTime conversationClosedAt){ this.conversationClosedAt = conversationClosedAt; }

    public ConversationEntity() {}
}
