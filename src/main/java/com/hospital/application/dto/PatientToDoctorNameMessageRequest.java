// src/main/java/com/hospital/application/dto/PatientToDoctorNameMessageRequest.java
package com.hospital.application.dto;

import java.util.UUID;

public record PatientToDoctorNameMessageRequest(
        @jakarta.validation.constraints.NotBlank
        String doctorName,
        
        @jakarta.validation.constraints.NotNull 
        UUID patientId,

        @jakarta.validation.constraints.NotBlank 
        @jakarta.validation.constraints.Size(max = 1000)
        String content
) {}
