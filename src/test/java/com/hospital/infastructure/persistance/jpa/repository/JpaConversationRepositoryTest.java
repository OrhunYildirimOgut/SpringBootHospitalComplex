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
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
public class JpaConversationRepositoryTest extends RepositorySliceConfig {

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

    private static ConversationEntity conv(List<UserEntity> users, ConversationStatus status, LocalDateTime createdAt) {
        return ConversationEntity.createConversationEntity(UUID.randomUUID(), users, status, createdAt, null);
    }

    @Test
    void findBetweenWithStatus_returns_latest_active_conversation() {
        var patient = userRepository.save(user(UUID.randomUUID(), "Ali Hasta", Set.of(UserRole.PATIENT)));
        var doctor = userRepository.save(user(UUID.randomUUID(), "Ahmet Cuhsin", Set.of(UserRole.DOCTOR)));

        var base = LocalDateTime.now().withNano(0);

        var conversation1 = conversationRepository
            .save(conv(List.of(patient, doctor), ConversationStatus.ACTIVE, base.minusHours(2)));
        var conversation2 = conversationRepository
            .save(conv(List.of(patient, doctor), ConversationStatus.ACTIVE, base));

        var list = conversationRepository.findBetweenWithStatus(patient.getUserID(), doctor.getUserID(), ConversationStatus.ACTIVE);
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getConversationID()).isEqualTo(conversation2.getConversationID());
    }

    @Test
    void findAllByUserId_returns_desc_order() {
        var user = userRepository.save(user(UUID.randomUUID(), "X", Set.of(UserRole.PATIENT)));
        var base = LocalDateTime.now().withNano(0);

        var conversationOld = conversationRepository
            .save(conv(List.of(user), ConversationStatus.ACTIVE, base.minusDays(1)));
        var conversationNew = conversationRepository
            .save(conv(List.of(user), ConversationStatus.ACTIVE, base));

        var list = conversationRepository.findAllByUserId(user.getUserID());

        assertThat(list)
            .extracting(ConversationEntity::getConversationID)
            .containsExactly(conversationNew.getConversationID(), conversationOld.getConversationID());
    }
}