// src/test/java/com/hospital/infrastructure/persistance/jpa/adapter/UserRepositoryAdapterTest.java
package com.hospital.infastructure.persistance.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;
import com.hospital.infrastructure.persistance.jpa.adapter.UserRepositoryAdapter;


class UserRepositoryAdapterTest {

    JpaUserRepository jpaUserRepository = mock(JpaUserRepository.class);
    UserRepositoryPort userRepositoryPort = new UserRepositoryAdapter(jpaUserRepository);

    private static UserEntity entity(UUID id, String name, Set<UserRole> roles) {
      var userEntity = new UserEntity();
      userEntity.setUserEntityId(id);
      userEntity.setUserEntityName(name);
      userEntity.setUserEntityRoles(roles);
      return userEntity;
    }

    @Test
    void save_maps_entity_and_back() {
      var model = new UserModel(UUID.randomUUID(), "Ali", Set.of(UserRole.PATIENT));
      when(jpaUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

      var saved = userRepositoryPort.save(model);
      assertThat(saved.userID()).isEqualTo(model.userID());
      assertThat(saved.userName()).isEqualTo("Ali");
      assertThat(saved.userRoles()).containsExactly(UserRole.PATIENT);
    }

    @Test
    void findById_maps_optional() {
      var entity = entity(UUID.randomUUID(), "Veli", Set.of(UserRole.DOCTOR));
      when(jpaUserRepository.findById(entity.getUserID())).thenReturn(Optional.of(entity));

      var opt = userRepositoryPort.findById(entity.getUserID());
      assertThat(opt).isPresent();
      assertThat(opt.get().userName()).isEqualTo("Veli");
      assertThat(opt.get().userRoles()).containsExactly(UserRole.DOCTOR);
    }

    @Test
    void findAll_maps_list() {
      var entity1 = entity(UUID.randomUUID(), "A", Set.of(UserRole.DOCTOR));
      var entity2 = entity(UUID.randomUUID(), "B", Set.of(UserRole.PATIENT, UserRole.DOCTOR));

      when(jpaUserRepository.findAll()).thenReturn(List.of(entity1, entity2));

      var list = userRepositoryPort.findAll();
      assertThat(list).hasSize(2);
      assertThat(list.get(1).userRoles()).contains(UserRole.PATIENT, UserRole.DOCTOR);
    }

    @Test
    void findAllByRole_delegates_and_maps() {
      var entity = entity(UUID.randomUUID(), "Doctor", Set.of(UserRole.DOCTOR));

      when(jpaUserRepository.findByUserRoleSetContaining(UserRole.DOCTOR)).thenReturn(List.of(entity));

      var userList = userRepositoryPort.findAllByRole(UserRole.DOCTOR);

      assertThat(userList).hasSize(1);
      assertThat(userList.get(0).userRoles()).containsExactly(UserRole.DOCTOR);
    }

    @Test
    void findByNameAndRole_delegates_and_maps() {
      var entity = entity(UUID.randomUUID(), "Hakan", Set.of(UserRole.PATIENT));
      
      when(jpaUserRepository.findByUserNameAndUserRoleSetContaining("Hakan", UserRole.PATIENT))
        .thenReturn(List.of(entity));

      var userList = userRepositoryPort.findByNameAndRole("Hakan", UserRole.PATIENT);
      assertThat(userList)
      .extracting(UserModel::userName)
      .containsExactly("Hakan");
    }

    @Test
    void deleteAll_inBatch_or_fallback() {
      userRepositoryPort.deleteAll();
      verify(jpaUserRepository).deleteAllInBatch();

      reset(jpaUserRepository);
      doThrow(new UnsupportedOperationException())
        .when(jpaUserRepository).deleteAllInBatch();

      userRepositoryPort.deleteAll();
      verify(jpaUserRepository).deleteAll();
    }
}
