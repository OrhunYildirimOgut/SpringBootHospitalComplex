// src/main/java/com/hospital/api/MessageController.java
package com.hospital.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.application.dto.MessageResponse;
import com.hospital.application.dto.MessageSendRequest;
import com.hospital.application.dto.PatientToDoctorNameMessageRequest;
import com.hospital.application.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService = messageService;
    }
    
    // Send first message and create conversation (patient -> doctor)
    @PostMapping("/first")
    public MessageResponse sendFirst
    (
        @RequestBody 
        @Valid 
        PatientToDoctorNameMessageRequest patientToDoctorNameMessageRequest
    ) {
        return messageService.sendByPatientToDoctorName
        (
            patientToDoctorNameMessageRequest.doctorName(),
            patientToDoctorNameMessageRequest.patientId(),
            patientToDoctorNameMessageRequest.content()
        );
    }

    // Send message in created conversation (patient -> doctor) (doctor -> patient)
    @PostMapping("/conversation/{conversationId}")
    public MessageResponse send
    (
        @PathVariable UUID conversationId,
        @RequestBody @Valid MessageSendRequest req
    ) {
        return messageService.sendMessage(conversationId, req);
    }

    // List all messages in conversation
    @GetMapping("/conversation/{conversationId}")
    public List<MessageResponse> list(@PathVariable UUID conversationId) {
        return messageService.listMessages(conversationId);
    }
}
