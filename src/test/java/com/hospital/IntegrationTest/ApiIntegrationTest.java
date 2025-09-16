// src/test/java/com/hospital/IntegrationTest/ApiIntegrationTest.java

package com.hospital.IntegrationTest;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;

@AutoConfigureMockMvc(addFilters = false) 
public class ApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired 
    MockMvc mockMvc;

    @Autowired 
    ObjectMapper om;

    @Autowired 
    UserRepositoryPort userRepo;

    UserModel patient;

    @BeforeEach
    void init() {
        userRepo.deleteAll();

        patient = userRepo.save(new UserModel(UUID.randomUUID(), "Ali Hasta", Set.of(UserRole.PATIENT)));
        userRepo.save(new UserModel(UUID.randomUUID(), "Veli Doktor", Set.of(UserRole.DOCTOR)));
    }

    @Test
    void post_firstMessage_returns_200_and_message_json() throws Exception {
        record Req(String doctorName, UUID patientId, String content) {}
        var body = new Req("Veli Doktor", patient.userID(), "Merhaba hocam");

        mockMvc.perform(post("/api/messages/first")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messageContent").value("Merhaba hocam"));
    }
}