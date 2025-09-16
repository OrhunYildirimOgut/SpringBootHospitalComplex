// src/main/java/com/hospital/infrastructure/persistance/jpa/repository/JpaConversationRepository.java
package com.hospital.infrastructure.persistance.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hospital.domain.status.ConversationStatus;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;

public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {

  @Query("""
      select distinct c from ConversationEntity c
        join c.conversationUserList u1
        join c.conversationUserList u2
      where u1.userID = :userA
        and u2.userID = :userB
        and c.conversationStatus = :status
      order by c.conversationCreatedAt desc
      """)
  List<ConversationEntity> findBetweenWithStatus(UUID userA, UUID userB, ConversationStatus status);

  @Query("""
      select c from ConversationEntity c
        join c.conversationUserList u
      where u.userID = :userId
      order by c.conversationCreatedAt desc
      """)
  List<ConversationEntity> findAllByUserId(UUID userId);
}
