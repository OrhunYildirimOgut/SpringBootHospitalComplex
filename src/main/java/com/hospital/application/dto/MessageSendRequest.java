// src/main/java/com/hospital/application/dto/MessageSendRequest.java
package com.hospital.application.dto;

import java.util.UUID;

public record MessageSendRequest(
    @jakarta.validation.constraints.NotNull
    UUID messageAuthorId,
    
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 1000)
    String messageContent
) {}
