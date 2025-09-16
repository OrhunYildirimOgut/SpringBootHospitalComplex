// src/main/java/com/hospital/application/port/MessageRepositoryPort.java
package com.hospital.application.port;

import java.util.List;
import java.util.UUID;

import com.hospital.domain.model.MessageModel;
// Interface for message
public interface MessageRepositoryPort {
    MessageModel save(MessageModel message, UUID conversationID);
    List<MessageModel> findByConversationID(UUID conversationID);
    void deleteAll();
}
