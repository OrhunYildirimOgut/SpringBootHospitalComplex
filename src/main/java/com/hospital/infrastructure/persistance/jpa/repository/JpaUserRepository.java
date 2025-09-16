// src/main/java/com/hospital/infrastructure/persistence/jpa/repository/JpaUserRepository.java
package com.hospital.infrastructure.persistance.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.domain.role.UserRole;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    List<UserEntity> findByUserNameAndUserRoleSetContaining(String userName, UserRole role);
    List<UserEntity> findByUserRoleSetContaining(UserRole role); 
}