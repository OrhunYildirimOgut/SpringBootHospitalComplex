// src/main/java/com/hospital/application/service/UserService.java
package com.hospital.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.UserCreateRequest;
import com.hospital.application.dto.UserResponse;
import com.hospital.application.port.RatingRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;

class UserServiceTest {

    UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    RatingRepositoryPort ratingRepositoryPort = mock(RatingRepositoryPort.class);
    UserService userService = new UserService(userRepositoryPort,ratingRepositoryPort );

    @Test
    void createUser_saves_and_returns_responseponse() {
        var request = new UserCreateRequest("Ali", Set.of(UserRole.PATIENT));
        when(userRepositoryPort.save(any()))
        .thenAnswer(i -> i.getArgument(0)); 

        UserResponse response = userService.createUser(request);
        assertThat(response.userName()).isEqualTo("Ali");
        assertThat(response.userRoles()).containsExactly(UserRole.PATIENT);
    }

    @Test
    void listUsers_maps_domain_to_dto() {
        var user1 = new UserModel(UUID.randomUUID(), "A", Set.of(UserRole.DOCTOR));
        var user2 = new UserModel(UUID.randomUUID(), "B", Set.of(UserRole.PATIENT, UserRole.DOCTOR));
        when(userRepositoryPort.findAll())
        .thenReturn(List.of(user1, user2));

        var list = userService.listUsers();
        assertThat(list).hasSize(2);
        assertThat(list.get(1).userRoles()).contains(UserRole.PATIENT, UserRole.DOCTOR);
    }

    @Test
    void getUser_success_and_not_found() {
        var id = UUID.randomUUID();
        var u = new UserModel(id, "X", Set.of(UserRole.PATIENT));
        when(userRepositoryPort.findById(id))
            .thenReturn(Optional.of(u));

        var response = userService.getUser(id);
        assertThat(response.userID()).isEqualTo(id);

        var missing = UUID.randomUUID();
        when(userRepositoryPort.findById(missing))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(missing))
            .isInstanceOf(NotFoundException.class);
    }
}