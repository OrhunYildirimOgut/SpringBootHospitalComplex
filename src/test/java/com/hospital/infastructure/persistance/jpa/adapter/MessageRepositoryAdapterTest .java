// src/test/java/com/hospital/infrastructure/persistance/jpa/adapter/MessageRepositoryAdapterTest.java
package com.hospital.infastructure.persistance.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hospital.domain.model.MessageModel;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.MessageEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaMessageRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import com.hospital.infrastructure.persistance.jpa.adapter.MessageRepositoryAdapter;

class MessageRepositoryAdapterTest {

    JpaMessageRepository jpaMessageRepository 
        = mock(JpaMessageRepository.class);
    JpaConversationRepository jpaConversationRepository
        = mock(JpaConversationRepository.class);
    JpaUserRepository jpaUserRepository
        = mock(JpaUserRepository.class);

    MessageRepositoryAdapter adapter = new MessageRepositoryAdapter(jpaMessageRepository, jpaConversationRepository, jpaUserRepository);

    private static UserEntity user(UUID id, String name) {
        var user = new UserEntity();
        user.setUserEntityId(id);
        user.setUserEntityName(name);
        user.setUserEntityRoles(java.util.Set.of(com.hospital.domain.role.UserRole.PATIENT));
        return user;
    }

    @Test
    void save_maps_and_persists_message() {

        var conversation = new ConversationEntity();
        conversation.setConversationEntityId(UUID.randomUUID());
        var author = user(UUID.randomUUID(), "A");

        when(jpaConversationRepository.findById(conversation.getConversationID())).thenReturn(Optional.of(conversation));
        when(jpaUserRepository.findById(author.getUserID())).thenReturn(Optional.of(author));

        var now = LocalDateTime.now().withNano(0);
        var entitySaved = MessageEntity.createMessageEntity(UUID.randomUUID(), conversation, author, "hi", now);
        when(jpaMessageRepository.save(any())).thenReturn(entitySaved);

        var domainIn = new MessageModel(UUID.randomUUID(), author.getUserID(), "hi", now);
        var domainOut = adapter.save(domainIn, conversation.getConversationID());

        assertThat(domainOut.messageContext()).isEqualTo("hi");
        assertThat(domainOut.authorID()).isEqualTo(author.getUserID());
    }

    @Test
    void save_missing_conversation_or_author_throws_NoSuchElement() {
        var conversationID = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var model = new MessageModel(UUID.randomUUID(), authorId, "x", LocalDateTime.now());

        when(jpaConversationRepository.findById(conversationID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adapter.save(model, conversationID))
            .isInstanceOf(java.util.NoSuchElementException.class);

        var conversation = new ConversationEntity();
        conversation.setConversationEntityId(conversationID);

        when(jpaConversationRepository.findById(conversationID)).thenReturn(Optional.of(conversation));
        when(jpaUserRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(model, conversationID))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void findByConversationID_maps_list() {
        var conversationID = UUID.randomUUID();
        var user = user(UUID.randomUUID(), "User");
        var now = LocalDateTime.now().withNano(0);

        var messageEntity1 = MessageEntity
            .createMessageEntity(UUID.randomUUID(), new ConversationEntity(), user, "a", now);
        var messageEntity2 = MessageEntity
            .createMessageEntity(UUID.randomUUID(), new ConversationEntity(), user, "b", now.plusSeconds(1));

        when(jpaMessageRepository.findByMessageConversation_ConversationIDOrderByMessageCreatedAtAsc(conversationID))
            .thenReturn(List.of(messageEntity1, messageEntity2));

        var list = adapter.findByConversationID(conversationID);
        assertThat(list).extracting(MessageModel::messageContext).containsExactly("a", "b");
    }

    @Test
    void deleteAll_uses_inBatch_or_fallback() {
        adapter.deleteAll();
        verify(jpaMessageRepository).deleteAllInBatch();

        reset(jpaMessageRepository);
        doThrow(new UnsupportedOperationException()).when(jpaMessageRepository).deleteAllInBatch();
        adapter.deleteAll();
        verify(jpaMessageRepository).deleteAll();
    }
}
