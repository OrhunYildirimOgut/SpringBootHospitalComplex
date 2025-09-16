package com.hospital.infastructure.persistance.jpa.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.mapper.UserMapper;

public class UserMapperUnitTest {
    
    @ParameterizedTest
    @EnumSource(UserRole.class) 
        void toDomain_should_map_single_role(UserRole userRole) {
        UUID userID = UUID.randomUUID();
        String userName = userRole.name().toLowerCase() + " name";

        UserEntity userEntity = new UserEntity();
        userEntity.setUserEntityId(userID);
        userEntity.setUserEntityName(userName);

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(userRole);
        userEntity.setUserEntityRoles(userRoles);

        UserModel domain = UserMapper.toDomain(userEntity);

        assertThat(domain).isNotNull();
        assertThat(domain.userID()).isEqualTo(userID);
        assertThat(domain.userName()).isEqualTo(userName);
        assertThat(domain.userRoles()).containsExactly(userRole);
    }
    
    @Test
    void toDomain_should_map_multiple_roles() {
        UserRole[] allRoles = UserRole.values();

        int n = allRoles.length;
        int totalMasks = 1 << n;

        for (int mask = 1; mask < totalMasks; mask++) {
            if (Integer.bitCount(mask) < 2) continue; 

            Set<UserRole> userRoles = new HashSet<>();
            List<String> nameParts = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    userRoles.add(allRoles[i]);
                    nameParts.add(allRoles[i].name().toLowerCase());
                }
            }

            String userName = String.join(" and ", nameParts) + " name";

            UUID userID = UUID.randomUUID();
            UserEntity userEntity = new UserEntity();
            userEntity.setUserEntityId(userID);
            userEntity.setUserEntityName(userName);
            userEntity.setUserEntityRoles(userRoles);

            UserModel domain = UserMapper.toDomain(userEntity);

            assertThat(domain).isNotNull();
            assertThat(domain.userID()).isEqualTo(userID);
            assertThat(domain.userName()).isEqualTo(userName);
            assertThat(domain.userRoles()).containsExactlyInAnyOrderElementsOf(userRoles);
        }
    }

    @ParameterizedTest
    @MethodSource("roleSets")
    void toEntity_should_map_back_and_forth(Set<UserRole> roles, String name) {
        UUID id = UUID.randomUUID();
        UserModel domain = new UserModel(id, name, roles);
        UserEntity entity = UserMapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getUserID()).isEqualTo(id);
        assertThat(entity.getUserName()).isEqualTo(name);
        assertThat(entity.getUserRoles()).containsExactlyInAnyOrderElementsOf(roles);
    }

    static Stream<Arguments> roleSets() {
        return Stream.of(
            Arguments.of(Set.of(UserRole.PATIENT), "Ali"),
            Arguments.of(Set.of(UserRole.DOCTOR), "Veli"),
            Arguments.of(Set.of(UserRole.PATIENT, UserRole.DOCTOR), "Ayşe")
        );
    }

}