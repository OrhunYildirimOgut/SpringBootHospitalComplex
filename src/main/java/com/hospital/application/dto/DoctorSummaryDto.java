// src/main/java/com/hospital/application/dto/DoctorSummaryDto.java
package com.hospital.application.dto;

import java.util.UUID;
import com.hospital.api.projection.DoctorSummary;

public class DoctorSummaryDto implements DoctorSummary {
    private final UUID id;
    private final String fullName;
    private final Double rating;
    private final Integer ratingsCount;

    public DoctorSummaryDto(UUID id, String fullName, Double rating, Integer ratingsCount) {
        this.id = id;
        this.fullName = fullName;
        this.rating = rating;
        this.ratingsCount = ratingsCount;
    }

    @Override public UUID getId() { return id; }
    @Override public String getFullName() { return fullName; }
    @Override public Double getRating() { return rating; }
    @Override public Integer getRatingsCount() { return ratingsCount; }
}
