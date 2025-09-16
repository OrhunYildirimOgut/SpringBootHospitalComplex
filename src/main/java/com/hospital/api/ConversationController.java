// src/main/java/com/hospital/api/ConversationController.java
package com.hospital.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.application.dto.ConversationResponse;
import com.hospital.application.service.ConversationService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // Return all conversation about user
    @GetMapping("/user/{userId}")
    public List<ConversationResponse> listByUser(@PathVariable UUID userId) {
        return conversationService.listByUser(userId);
    }

    // Close conversation
    @PostMapping("/{conversationId}/close")
    public ConversationResponse close(@PathVariable UUID conversationId,
                                      @RequestParam UUID actorId) {
        return conversationService.closeConversation(conversationId, actorId);
    }
}
