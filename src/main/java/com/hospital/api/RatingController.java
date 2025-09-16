// src/main/java/com/hospital/api/RatingController.java
package com.hospital.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.application.dto.DoctorRatingResponse;
import com.hospital.application.dto.RatingCreateRequest;
import com.hospital.application.dto.RatingResponse;
import com.hospital.application.service.RatingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) { this.ratingService = ratingService; }

    // Create rating
    @PostMapping
    public RatingResponse create(@RequestBody @Valid RatingCreateRequest ratingCreateRequest) {
        return ratingService.create(ratingCreateRequest);
    }

    // Return rating of doctor
    @GetMapping("/doctor/{doctorId}")
    public DoctorRatingResponse doctorRating(@PathVariable UUID doctorId) {
        return ratingService.doctorRating(doctorId);
    }
}
