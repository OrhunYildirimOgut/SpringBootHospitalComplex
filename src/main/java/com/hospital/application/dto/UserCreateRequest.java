// src/main/java/com/hospital/application/dto/UserCreateRequest.java
package com.hospital.application.dto;

public record UserCreateRequest(
    @jakarta.validation.constraints.NotBlank
    String userName,

    @jakarta.validation.constraints.NotEmpty
    java.util.Set<com.hospital.domain.role.UserRole>
    userRoles

) {}
