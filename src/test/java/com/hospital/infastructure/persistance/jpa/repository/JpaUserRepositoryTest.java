package com.hospital.infastructure.persistance.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hospital.domain.role.UserRole;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
public class JpaUserRepositoryTest extends RepositorySliceConfig {

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
    void findByUserNameAndUserRoleSetContaining_filters_correctly() {
        userRepository.save(user(UUID.randomUUID(), "Veli Doktor", Set.of(UserRole.DOCTOR)));
        userRepository.save(user(UUID.randomUUID(), "Veli Doktor", Set.of(UserRole.PATIENT)));

        var onlyDoctors = userRepository.findByUserNameAndUserRoleSetContaining("Veli Doktor", UserRole.DOCTOR);
        assertThat(onlyDoctors).hasSize(1);
        assertThat(onlyDoctors.get(0).getUserRoles()).contains(UserRole.DOCTOR);
    }

    @Test
    void findByUserRoleSetContaining_returns_all_with_role() {
        userRepository.save(user(UUID.randomUUID(), "A", Set.of(UserRole.DOCTOR)));
        userRepository.save(user(UUID.randomUUID(), "B", Set.of(UserRole.DOCTOR, UserRole.PATIENT)));
        userRepository.save(user(UUID.randomUUID(), "C", Set.of(UserRole.PATIENT)));

        var doctors = userRepository.findByUserRoleSetContaining(UserRole.DOCTOR);
        assertThat(doctors).hasSize(2);
    }
}