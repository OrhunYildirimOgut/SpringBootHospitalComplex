// src/main/java/com/hospital/api/projection/DoctorSummary.java

package com.hospital.api.projection;
import java.util.UUID;

public interface DoctorSummary {
    UUID getId();
    String getFullName();
    Double getRating();
    Integer getRatingsCount();
}
