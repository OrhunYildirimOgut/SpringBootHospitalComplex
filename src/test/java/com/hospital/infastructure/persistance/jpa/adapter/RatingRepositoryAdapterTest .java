// src/test/java/com/hospital/infrastructure/persistance/jpa/adapter/RatingRepositoryAdapterTest.java
package com.hospital.infastructure.persistance.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hospital.domain.model.RatingModel;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.RatingEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaRatingRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import com.hospital.infrastructure.persistance.jpa.adapter.RatingRepositoryAdapter;

class RatingRepositoryAdapterTest {

    JpaRatingRepository jpaRatingRepository = mock(JpaRatingRepository.class);
    JpaConversationRepository jpaConversationRepository = mock(JpaConversationRepository.class);
    JpaUserRepository jpaUserRepository = mock(JpaUserRepository.class);

    RatingRepositoryAdapter adapter = new RatingRepositoryAdapter(jpaRatingRepository, jpaConversationRepository, jpaUserRepository);

    private static UserEntity user(UUID id) {
        var user = new UserEntity();
        user.setUserEntityId(id);
        user.setUserEntityName("U");
        user.setUserEntityRoles(java.util.Set.of(com.hospital.domain.role.UserRole.PATIENT));
        return user;
    }

    @Test
    void save_maps_and_persists() {
        var conversation = new ConversationEntity();
        conversation.setConversationEntityId(UUID.randomUUID());
        var patient = user(UUID.randomUUID());
        var doctor = user(UUID.randomUUID());

        when(jpaConversationRepository.findById(conversation.getConversationID())).thenReturn(Optional.of(conversation));
        when(jpaUserRepository.findById(patient.getUserID())).thenReturn(Optional.of(patient));
        when(jpaUserRepository.findById(doctor.getUserID())).thenReturn(Optional.of(doctor));

        var now = LocalDateTime.now().withNano(0);
        var modelIn = new RatingModel(UUID.randomUUID(), conversation.getConversationID(), patient.getUserID(), doctor.getUserID(), 5, now);

        var entitySaved = new RatingEntity();
        entitySaved.setRatingId(modelIn.ratingId());
        entitySaved.setConversation(conversation);
        entitySaved.setPatient(patient);
        entitySaved.setDoctor(doctor);
        entitySaved.setScore(5);
        entitySaved.setCreatedAt(now);
        when(jpaRatingRepository.save(any())).thenReturn(entitySaved);

        var modelOut = adapter.save(modelIn);
        assertThat(modelOut.score()).isEqualTo(5);
        assertThat(modelOut.conversationId()).isEqualTo(conversation.getConversationID());
    }

    @Test
    void save_missing_refs_throw_illegalArgument() {
        var conversationID = UUID.randomUUID();
        var patientID = UUID.randomUUID();
        var doctorID = UUID.randomUUID();
        var model = new RatingModel(UUID.randomUUID(), conversationID, patientID, doctorID, 4, LocalDateTime.now());

        when(jpaConversationRepository.findById(conversationID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adapter.save(model))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Conversation not found");

        when(jpaConversationRepository.findById(conversationID)).thenReturn(Optional.of(new ConversationEntity()));
        when(jpaUserRepository.findById(patientID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adapter.save(model))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Patient not found");

        when(jpaUserRepository.findById(patientID)).thenReturn(Optional.of(user(patientID)));
        when(jpaUserRepository.findById(doctorID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adapter.save(model))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Doctor not found");
    }

    @Test
    void findByConversationAndPatient_maps_optional() {
        var conv = new ConversationEntity(); conv.setConversationEntityId(UUID.randomUUID());
        var patient = user(UUID.randomUUID());
        var doctor = user(UUID.randomUUID());
        var entity = new RatingEntity();

        entity.setRatingId(UUID.randomUUID());
        entity.setConversation(conv);
        entity.setPatient(patient);
        entity.setDoctor(doctor);
        entity.setScore(3);
        entity.setCreatedAt(LocalDateTime.now());

        when(jpaRatingRepository.findByConversation_ConversationIDAndPatient_UserID(conv.getConversationID(), patient.getUserID()))
            .thenReturn(Optional.of(entity));

        var opt = adapter.findByConversationAndPatient(conv.getConversationID(), patient.getUserID());

        assertThat(opt).isPresent();
        assertThat(opt.get().score()).isEqualTo(3);
    }

    @Test
    void average_computed_from_list_and_count_pass_through() {
        var doctorID = UUID.randomUUID();

        var r1 = new RatingEntity(); r1.setScore(4);
        var r2 = new RatingEntity(); r2.setScore(5);

        when(jpaRatingRepository.findByDoctor_UserID(doctorID)).thenReturn(java.util.List.of(r1, r2));
        when(jpaRatingRepository.countByDoctor_UserID(doctorID)).thenReturn(2L);

        assertThat(adapter.averageForDoctor(doctorID)).isEqualTo(4.5);
        assertThat(adapter.countForDoctor(doctorID)).isEqualTo(2L);
    }
}
