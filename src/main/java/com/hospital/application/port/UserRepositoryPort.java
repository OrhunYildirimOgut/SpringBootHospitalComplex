// src/main/java/com/hospital/application/port/UserRepositoryPort.java
package com.hospital.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
// Interface for user
public interface UserRepositoryPort {
    UserModel save(UserModel user);
    Optional<UserModel> findById(UUID userID);
    List<UserModel> findAll();
    List<UserModel> findAllByRole(UserRole role);
    List<UserModel> findByNameAndRole(String userName, UserRole role);
    void deleteAll();
}
