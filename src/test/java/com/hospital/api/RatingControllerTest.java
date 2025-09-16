// src/main/java/com/hospital/api/RatingController.java
package com.hospital.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.application.dto.DoctorRatingResponse;
import com.hospital.application.dto.RatingCreateRequest;
import com.hospital.application.dto.RatingResponse;
import com.hospital.application.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
@WebMvcTest(controllers = RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.hospital.application.common.exceptions.GlobalExceptionHandler.class)
class RatingControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired 
    ObjectMapper objectMapper;
    
    @MockitoBean
    RatingService ratingService;

    @Test
    void create_returns_rating() throws Exception {
        var request = new RatingCreateRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5);
        var response = new RatingResponse(UUID.randomUUID(), request.conversationId(), request.patientId(), request.doctorId(), 5, LocalDateTime.now());

        when(ratingService.create(any(RatingCreateRequest.class))).thenReturn(response);

        mvc.perform(post("/api/ratings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score")
                .value(5))
            .andExpect(jsonPath("$.conversationId")
                .value(request.conversationId().toString()));
    }

    @Test
    void create_validation_score_out_of_range_400() throws Exception {
        var invalid = """
        {
            "conversationId": "%s",
            "patientId": "%s",
            "doctorId": "%s",
            "score": 6
        }
        """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/api/ratings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Validation failed"))
            .andExpect(jsonPath("$.errors.score")
                .exists());
    }

    @Test
    void doctorRating_returns_summary() throws Exception {
        var doctorID = UUID.randomUUID();
        var response = new DoctorRatingResponse(doctorID, 4.75, 8L);
        when(ratingService.doctorRating(doctorID)).thenReturn(response);

        mvc.perform(get("/api/ratings/doctor/{doctorId}", doctorID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.doctorId")
                .value(doctorID.toString()))
            .andExpect(jsonPath("$.averageScore")
                .value(4.75))
            .andExpect(jsonPath("$.ratingCount")
                .value(8));
    }
}