// src/main/java/com/hospital/domain/model/User.java
package com.hospital.domain.model;

import java.util.Set;
import java.util.UUID;

import com.hospital.domain.role.UserRole;

public record UserModel(
    UUID userID,
    String userName,
    Set<UserRole> userRoles
) {}
