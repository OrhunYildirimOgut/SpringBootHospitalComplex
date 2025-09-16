package com.hospital.infastructure.persistance.jpa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hospital.domain.role.UserRole;
import com.hospital.domain.status.ConversationStatus;
import com.hospital.infrastructure.persistance.jpa.entity.ConversationEntity;
import com.hospital.infrastructure.persistance.jpa.entity.RatingEntity;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaConversationRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaRatingRepository;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
public class JpaRatingRepositoryTest extends RepositorySliceConfig {

    @Autowired 
    JpaRatingRepository ratingRepository;

    @Autowired 
    JpaUserRepository userRepository;

    @Autowired 
    JpaConversationRepository conversationRepository;

    private static UserEntity user(UUID id, String name, Set<UserRole> roles) {
        var user = new UserEntity();
        user.setUserEntityId(id);
        user.setUserEntityName(name);
        user.setUserEntityRoles(roles);
        return user;
    }

    @Test
    void average_and_count_for_doctor_work_and_null_when_no_data() {
        var patient1 = userRepository.save(user(UUID.randomUUID(), "Hasta1", Set.of(UserRole.PATIENT)));
        var patient2 = userRepository.save(user(UUID.randomUUID(), "Hasta2", Set.of(UserRole.PATIENT)));
        var doctor   = userRepository.save(user(UUID.randomUUID(), "Doktor",  Set.of(UserRole.DOCTOR)));

        assertThat(ratingRepository.findByDoctor_UserID(doctor.getUserID())).isEmpty();
        assertThat(ratingRepository.countByDoctor_UserID(doctor.getUserID())).isZero();


        var base = LocalDateTime.now().withNano(0);

        var conversation1 = conversationRepository.save(
            ConversationEntity.createConversationEntity(
                UUID.randomUUID(),
                List.of(patient1, doctor),
                ConversationStatus.CLOSED,
                base.minusHours(1),
                base.minusHours(1)
            )
        );

        var conversation2 = conversationRepository.save(
            ConversationEntity.createConversationEntity(
                UUID.randomUUID(),
                List.of(patient2, doctor),
                ConversationStatus.CLOSED,
                base.minusMinutes(30),
                base.minusMinutes(30)
            )
        );

        var ratingEntity1 = new RatingEntity();
        ratingEntity1.setRatingId(UUID.randomUUID());
        ratingEntity1.setConversation(conversation1);
        ratingEntity1.setPatient(patient1);
        ratingEntity1.setDoctor(doctor);
        ratingEntity1.setScore(4);
        ratingEntity1.setCreatedAt(base);
        ratingRepository.save(ratingEntity1);

        var ratingEntity2 = new RatingEntity();
        ratingEntity2.setRatingId(UUID.randomUUID());
        ratingEntity2.setConversation(conversation2); 
        ratingEntity2.setPatient(patient2);
        ratingEntity2.setDoctor(doctor);
        ratingEntity2.setScore(5);
        ratingEntity2.setCreatedAt(base.plusSeconds(1));
        ratingRepository.save(ratingEntity2);

        assertThat(ratingRepository.countByDoctor_UserID(doctor.getUserID())).isEqualTo(2);

        var list = ratingRepository.findByDoctor_UserID(doctor.getUserID());
        assertThat(list).hasSize(2);
        double avg = list.stream().mapToInt(RatingEntity::getScore).average().orElseThrow();
        assertThat(avg).isBetween(4.5, 5.0);
    }

    @Test
    void findByConversationAndPatient_returns_single_rating() {
        var patient = userRepository.save(user(UUID.randomUUID(), "Hasta", Set.of(UserRole.PATIENT)));
        var doctor  = userRepository.save(user(UUID.randomUUID(), "Doktor", Set.of(UserRole.DOCTOR)));

        var base = LocalDateTime.now().withNano(0);
        var conversation = conversationRepository.save(
            ConversationEntity.createConversationEntity(
                UUID.randomUUID(),
                List.of(patient, doctor),
                ConversationStatus.CLOSED, base, base
            )
        );

        var ratingEntity = new RatingEntity();
        ratingEntity.setRatingId(UUID.randomUUID());
        ratingEntity.setConversation(conversation);
        ratingEntity.setPatient(patient);
        ratingEntity.setDoctor(doctor);
        ratingEntity.setScore(5);
        ratingEntity.setCreatedAt(base);
        ratingRepository.save(ratingEntity);

        var opt = ratingRepository
            .findByConversation_ConversationIDAndPatient_UserID(
            conversation.getConversationID(), patient.getUserID()
        );

        assertThat(opt).isPresent();
        assertThat(opt.get().getScore()).isEqualTo(5);
    }
}