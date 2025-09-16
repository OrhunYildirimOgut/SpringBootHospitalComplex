// src/main/java/com/hospital/api/ConversationController.java
package com.hospital.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.ConversationResponse;
import com.hospital.application.service.ConversationService;
import com.hospital.domain.status.ConversationStatus;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
@WebMvcTest(controllers = ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.hospital.application.common.exceptions.GlobalExceptionHandler.class)
class ConversationControllerTest {

    @Autowired 
    MockMvc mvc;

    @MockitoBean 
    ConversationService conversationService;

    @Test
    void listByUser_returns_conversations() throws Exception {
        var userID = UUID.randomUUID();
        var conversationResponse = List.of(new ConversationResponse(
            UUID.randomUUID(),
            List.of(userID),
            ConversationStatus.ACTIVE,
            LocalDateTime.now(),
            null
        ));

        when(conversationService.listByUser(userID)).thenReturn(conversationResponse);

        mvc.perform(get("/api/conversations/user/{userId}", userID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].conversationID")
            .value(conversationResponse.get(0).conversationID().toString()))
            .andExpect(jsonPath("$[0].conversationStatus")
            .value("ACTIVE"));
    }

    @Test
    void close_returns_closed_conversation() throws Exception {
        var conversationID = UUID.randomUUID();
        var actorID = UUID.randomUUID();
        var closed = new ConversationResponse(
            conversationID, List.of(actorID),
            ConversationStatus.CLOSED,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );

        when(conversationService.closeConversation(conversationID, actorID)).thenReturn(closed);

        mvc.perform(post("/api/conversations/{conversationId}/close", conversationID)
            .param("actorId", actorID.toString())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conversationID")
            .value(conversationID.toString()))
            .andExpect(jsonPath("$.conversationStatus")
            .value("CLOSED"));
    }

    @Test
    void close_when_not_found_maps_to_404() throws Exception {
        var conversationID = UUID.randomUUID();
        var actorID = UUID.randomUUID();
        when(conversationService.closeConversation(conversationID, actorID))
            .thenThrow(new NotFoundException("Conversation not found"));

        mvc.perform(post("/api/conversations/{conversationId}/close", conversationID)
            .param("actorId", actorID.toString()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
            .value("Conversation not found"));
    }
}