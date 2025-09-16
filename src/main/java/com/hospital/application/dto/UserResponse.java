// src/main/java/com/hospital/application/dto/UserResponse.java
package com.hospital.application.dto;

import java.util.Set;
import java.util.UUID;

import com.hospital.domain.role.UserRole;

public record UserResponse(
    UUID userID,
    String userName,
    Set<UserRole> userRoles
) {}
