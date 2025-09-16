// src/main/java/com/hospital/infrastructure/persistence/jpa/mapper/UserMapper.java
package com.hospital.infrastructure.persistance.jpa.mapper;

import com.hospital.domain.model.UserModel;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {}

    public static UserModel toDomain(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return new UserModel(userEntity.getUserID(), userEntity.getUserName(), userEntity.getUserRoles());
    }

    public static UserEntity toEntity(UserModel userObject) {
        if (userObject == null)
            return null;
        UserEntity userEntity = new UserEntity();
        userEntity.setUserEntityId(userObject.userID());
        userEntity.setUserEntityName(userObject.userName());
        userEntity.setUserEntityRoles(userObject.userRoles());
        return userEntity;
    }
}