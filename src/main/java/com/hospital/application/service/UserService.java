// src/main/java/com/hospital/application/service/UserService.java
package com.hospital.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hospital.api.projection.DoctorSummary;
import com.hospital.application.dto.DoctorSummaryDto;
import com.hospital.application.dto.UserCreateRequest;
import com.hospital.application.dto.UserResponse;
import com.hospital.application.port.RatingRepositoryPort;
import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;

@org.springframework.transaction.annotation.Transactional
@Service
public class UserService {

    private final UserRepositoryPort userRepository;
    private final RatingRepositoryPort ratingRepository; 

    public UserService(
        UserRepositoryPort userRepository,
        RatingRepositoryPort ratingRepository
    ) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    // Create new user
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        UserModel userModel = new UserModel
        (
            UUID.randomUUID(),
            userCreateRequest.userName(),
            userCreateRequest.userRoles()
        );

        UserModel saved = userRepository.save(userModel);
        return new UserResponse(
            saved.userID(),
            saved.userName(),
            saved.userRoles()
        );
    }

    // List all users
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(u -> new UserResponse(u.userID(), u.userName(), u.userRoles()))
            .collect(Collectors.toList());
    }

    // Return user acc ID
    public UserResponse getUser(UUID userID) {
        UserModel userModel = userRepository.findById(userID)
        .orElseThrow(() -> new com.hospital.application.common.exceptions.NotFoundException("User not found"));
        return new UserResponse(
            userModel.userID(),
            userModel.userName(),
            userModel.userRoles()
        );
    }
    
    public List<DoctorSummary> listDoctorsWithRatings() {
        return userRepository.findAllByRole(UserRole.DOCTOR).stream()
            .map(this::toDoctorSummary)
            .map(d -> (DoctorSummary) d) 
            .sorted(Comparator.comparingDouble(DoctorSummary::getRating).reversed())
            .toList();
    }

    private DoctorSummary toDoctorSummary(UserModel doctor) {
        double avg = ratingRepository.averageForDoctor(doctor.userID());
        long count = ratingRepository.countForDoctor(doctor.userID());
        double rounded = round(avg, 2);

        return new DoctorSummaryDto(
            doctor.userID(),
            doctor.userName(),
            rounded,
            (int) count
        );
    }


    private double round(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
