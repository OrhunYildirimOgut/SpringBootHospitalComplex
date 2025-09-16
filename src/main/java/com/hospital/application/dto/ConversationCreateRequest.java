// src/main/java/com/hospital/application/dto/ConversationCreateRequest.java
package com.hospital.application.dto;

import java.util.List;
import java.util.UUID;

public record ConversationCreateRequest(List<UUID> participantIdList) {}