// src/test/java/com/hospital/IntegrationTest/TestDataFactory.java

package com.hospital.IntegrationTest;

import java.util.Set;
import java.util.UUID;

import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;

public final class TestDataFactory {
    private TestDataFactory(){}

    public static UserModel newPatient(UserRepositoryPort repo, String name) {
        var userModel = new UserModel(UUID.randomUUID(), name, Set.of(UserRole.PATIENT));
        return repo.save(userModel);
    }

    public static UserModel newDoctor(UserRepositoryPort repo, String name) {
        var userModel = new UserModel(UUID.randomUUID(), name, Set.of(UserRole.DOCTOR));
        return repo.save(userModel);
    }

    public static UserModel newDoctorPatient(UserRepositoryPort repo, String name) {
        var userModel = new UserModel(UUID.randomUUID(), name, Set.of(UserRole.DOCTOR, UserRole.PATIENT));
        return repo.save(userModel);
    }
}