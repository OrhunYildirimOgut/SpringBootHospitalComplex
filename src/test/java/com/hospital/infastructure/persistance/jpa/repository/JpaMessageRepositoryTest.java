package com.hospital.infastructure.persistance.jpa.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.MessageEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaMessageRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
public class JpaMessageRepositoryTest extends RepositorySliceConfig {

    @Autowired 
    JpaMessageRepository messageRepository;

    @Autowired 
    JpaConversationRepository conversationRepository;

    @Autowired 
    JpaUserRepository userRepository;

    private static UserEntity user(UUID id, String name, Set<UserRole> roles) {
        var user = new UserEntity();
        user.setUserEntityId(id);
        user.setUserEntityName(name);
        user.setUserEntityRoles(roles);
        return user;
    }

    @Test
    void findByConversation_orders_by_createdAt_asc() {
        var patient = userRepository
        .save(user(UUID.randomUUID(), "Ali", Set.of(UserRole.PATIENT)));
        var doctor  = userRepository
        .save(user(UUID.randomUUID(), "Doctor", Set.of(UserRole.DOCTOR)));

        var base = LocalDateTime.now().withNano(0);
        var conv  = conversationRepository.save(
            ConversationEntity.createConversationEntity(UUID.randomUUID(), List.of(patient, doctor), ConversationStatus.ACTIVE, base, null));

        var message1 = MessageEntity
            .createMessageEntity(UUID.randomUUID(), conv, patient, "message1", base.plusSeconds(0));
        var message2 = MessageEntity
            .createMessageEntity(UUID.randomUUID(), conv, doctor,  "message2", base.plusSeconds(1));
        var message3 = MessageEntity
            .createMessageEntity(UUID.randomUUID(), conv, patient, "message3", base.plusSeconds(2));

        messageRepository.saveAll(List.of(message2, message3, message1));

        var list = messageRepository
        .findByMessageConversation_ConversationIDOrderByMessageCreatedAtAsc(conv.getConversationID());

        assertThat(list).extracting(MessageEntity::getMessageContext).containsExactly("message1", "message2", "message3");
    }
}