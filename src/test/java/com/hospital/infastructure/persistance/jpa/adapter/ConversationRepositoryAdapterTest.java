// src/test/java/com/hospital/infrastructure/persistance/jpa/adapter/ConversationRepositoryAdapterTest.java
package com.hospital.infastructure.persistance.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hospital.domain.model.ConversationModel;
import com.hospital.domain.status.ConversationStatus;
import com.hospital.infrastructure.persistance.jpa.adapter.ConversationRepositoryAdapter;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;

class ConversationRepositoryAdapterTest {

    JpaConversationRepository jpaConversationRepository = mock(JpaConversationRepository.class);
    JpaUserRepository jpaUserRepository = mock(JpaUserRepository.class);
    ConversationRepositoryAdapter conversationRepositoryAdapter = new ConversationRepositoryAdapter(jpaConversationRepository, jpaUserRepository);

    private static UserEntity user(UUID id, String name) {
        var user = new UserEntity();
        user.setUserEntityId(id);
        user.setUserEntityName(name);
        user.setUserEntityRoles(java.util.Set.of(com.hospital.domain.role.UserRole.PATIENT));
        return user;
    }

    @Test
    void save_maps_domain_to_entity_and_back() {
        var user1 = user(UUID.randomUUID(), "A");
        var user2 = user(UUID.randomUUID(), "B");

        when(jpaUserRepository.findById(user1.getUserID()))
        .thenReturn(Optional.of(user1));
        when(jpaUserRepository.findById(user2.getUserID()))
        .thenReturn(Optional.of(user2));

        var createdAt = LocalDateTime.now().withNano(0);
        var domain = new ConversationModel
        (
            UUID.randomUUID(),
            List.of(user1.getUserID(), user2.getUserID()),
            ConversationStatus.ACTIVE, createdAt, null
        );

        var entityToReturn = ConversationEntity.createConversationEntity
        (
            domain.conversationID(), 
            List.of(user1, user2), 
            ConversationStatus.ACTIVE, 
            createdAt, 
            null
        );

        when(jpaConversationRepository.save(any())).thenReturn(entityToReturn);

        ConversationModel saved = conversationRepositoryAdapter.save(domain);

        assertThat(saved.conversationID()).isEqualTo(domain.conversationID());
        assertThat(saved.conversationUsersList()).containsExactly(user1.getUserID(), user2.getUserID());
        assertThat(saved.conversationStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void save_when_participant_missing_throws_illegalArgument() {
        var userID = UUID.randomUUID();
        when(jpaUserRepository.findById(userID)).thenReturn(Optional.empty());

        var domain = new ConversationModel(UUID.randomUUID(), List.of(userID),
            ConversationStatus.ACTIVE, LocalDateTime.now(), null);

        assertThatThrownBy(() -> conversationRepositoryAdapter.save(domain))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Participant not found");
    }

    @Test
    void findById_maps_entity_to_domain() {
        var user = user(UUID.randomUUID(), "X");
        var entity = ConversationEntity.createConversationEntity(
            UUID.randomUUID(),
            List.of(user),
            ConversationStatus.CLOSED,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(jpaConversationRepository.findById(entity.getConversationID())).thenReturn(Optional.of(entity));

        var opt = conversationRepositoryAdapter.findById(entity.getConversationID());
        assertThat(opt).isPresent();
        assertThat(opt.get().conversationUsersList()).containsExactly(user.getUserID());
        assertThat(opt.get().conversationStatus()).isEqualTo(ConversationStatus.CLOSED);
    }

    @Test
    void findAllByUserId_maps_list() {
        var user = user(UUID.randomUUID(), "U");

        var conversationEntity1 = ConversationEntity.createConversationEntity(UUID.randomUUID(), 
            List.of(user), ConversationStatus.ACTIVE, LocalDateTime.now(), null);
        var conversationEntity2 = ConversationEntity.createConversationEntity(UUID.randomUUID(),
            List.of(user), ConversationStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());

        when(jpaConversationRepository.findAllByUserId(user.getUserID())).thenReturn(List.of(conversationEntity1, conversationEntity2));

        var list = conversationRepositoryAdapter.findAllByUserId(user.getUserID());
        assertThat(list).hasSize(2);
        assertThat(list.get(1).conversationStatus()).isEqualTo(ConversationStatus.CLOSED);
    }

    @Test
    void findActiveBetween_returns_first_mapped() {
        var user1 = user(UUID.randomUUID(), "A");
        var user2 = user(UUID.randomUUID(), "B");
        var conversationEntity = ConversationEntity
            .createConversationEntity(
                UUID.randomUUID(), 
                List.of(user1, user2), 
                ConversationStatus.ACTIVE, 
                LocalDateTime.now(), 
                null
            );

        when(jpaConversationRepository.findBetweenWithStatus(user1.getUserID(), user2.getUserID(), ConversationStatus.ACTIVE))
            .thenReturn(List.of(conversationEntity));

        var opt = conversationRepositoryAdapter.findActiveBetween(user1.getUserID(), user2.getUserID());
        assertThat(opt).isPresent();
        assertThat(opt.get().conversationStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void deleteAll_uses_deleteAllInBatch_or_fallback() {
        conversationRepositoryAdapter.deleteAll();
        verify(jpaConversationRepository, times(1)).deleteAllInBatch();

        reset(jpaConversationRepository);
        doThrow(new UnsupportedOperationException()).when(jpaConversationRepository).deleteAllInBatch();
        conversationRepositoryAdapter.deleteAll();
        verify(jpaConversationRepository, times(1)).deleteAll();
    }
}
