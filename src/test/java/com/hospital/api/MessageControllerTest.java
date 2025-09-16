// src/main/java/com/hospital/api/MessageController.java
package com.hospital.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.application.dto.MessageResponse;
import com.hospital.application.dto.MessageSendRequest;
import com.hospital.application.dto.PatientToDoctorNameMessageRequest;
import com.hospital.application.service.MessageService;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
@WebMvcTest(controllers = MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.hospital.application.common.exceptions.GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired 
    MockMvc mvc;

    @Autowired 
    ObjectMapper objectMapper;

    @MockitoBean 
    MessageService messageService;

    @Test
    void sendFirst_returns_message_response() throws Exception {
        var request = new PatientToDoctorNameMessageRequest("Doctor", UUID.randomUUID(), "hi");
        var response = new MessageResponse(UUID.randomUUID(), request.patientId(), request.content(), LocalDateTime.now());

        when(messageService.sendByPatientToDoctorName(request.doctorName(), request.patientId(), request.content())).thenReturn(response);

        mvc.perform(post("/api/messages/first")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messageID")
                .value(response.messageID().toString()))
            .andExpect(jsonPath("$.messageAuthorId")
                .value(request.patientId().toString()))
            .andExpect(jsonPath("$.messageContent")
                .value("hi"));
    }

    @Test
    void sendFirst_validation_errors_400() throws Exception {
        var invalidJson = """
        {"doctorName":"", "patientId": "%s", "content": "" }
        """.formatted(UUID.randomUUID());

        mvc.perform(post("/api/messages/first")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Validation failed"))
            .andExpect(jsonPath("$.errors.doctorName")
                .exists())
            .andExpect(jsonPath("$.errors.content")
                .exists());
    }

    @Test
    void sendMessage_returns_message_response() throws Exception {
        var conversationID = UUID.randomUUID();
        var request = new MessageSendRequest(UUID.randomUUID(), "hi");
        var response = new MessageResponse(UUID.randomUUID(), request.messageAuthorId(), request.messageContent(), LocalDateTime.now());

        when(messageService.sendMessage(any(UUID.class), any(MessageSendRequest.class))).thenReturn(response);

        mvc.perform(post("/api/messages/conversation/{conversationId}", conversationID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messageAuthorId")
                .value(request.messageAuthorId().toString()))
            .andExpect(jsonPath("$.messageContent")
                .value("hi"));
    }

    @Test
    void sendMessage_validation_errors_400() throws Exception {
        var conversationID = UUID.randomUUID();
        var invalid = """
        {"messageContent": "" }
        """;

        mvc.perform(post("/api/messages/conversation/{conversationId}", conversationID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Validation failed"))
            .andExpect(jsonPath("$.errors.messageAuthorId")
                .exists());
    }

    @Test
    void listMessages_returns_array() throws Exception {
        var conversationID = UUID.randomUUID();
        var message1 
            = new MessageResponse(UUID.randomUUID(), UUID.randomUUID(), "a", LocalDateTime.now());
        var message2 
            = new MessageResponse(UUID.randomUUID(), UUID.randomUUID(), "b", LocalDateTime.now());

        when(messageService.listMessages(conversationID)).thenReturn(List.of(message1, message2));

        mvc.perform(get("/api/messages/conversation/{conversationId}", conversationID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].messageContent")
                .value("a"))
            .andExpect(jsonPath("$[1].messageContent")
                .value("b"));
    }
}