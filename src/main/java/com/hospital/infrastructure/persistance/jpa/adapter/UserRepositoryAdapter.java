// src/main/java/com/hospital/infrastructure/persistence/jpa/adapter/UserRepositoryAdapter.java
package com.hospital.infrastructure.persistance.jpa.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.application.port.UserRepositoryPort;
import com.hospital.domain.model.UserModel;
import com.hospital.domain.role.UserRole;
import com.hospital.infrastructure.persistance.jpa.entity.UserEntity;
import com.hospital.infrastructure.persistance.jpa.mapper.UserMapper;
import com.hospital.infrastructure.persistance.jpa.repository.JpaUserRepository;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort{
    
    private final JpaUserRepository jpa;

    public UserRepositoryAdapter(JpaUserRepository jpa){
        this.jpa = jpa;
    }

    // Save user model as entity
    @Override
    public UserModel save(UserModel userDomain){
        UserEntity userEntity = UserMapper.toEntity(userDomain);
        UserEntity saved = jpa.save(userEntity);
        return UserMapper.toDomain(saved);
    }

    // Find user with ID
    @Override
    public Optional<UserModel> findById(UUID userID){
        return jpa.findById(userID).map(UserMapper::toDomain);
    }

    // Retrives all users -> made them domain
    @Override
    public List<UserModel> findAll(){
        List<UserEntity> userEntityList = jpa.findAll();
        List<UserModel> userDomainList = new ArrayList<>();
        
        for(UserEntity userEntity : userEntityList){
            UserModel user = UserMapper.toDomain(userEntity);
            userDomainList.add(user);
        }
        return userDomainList;
    }

    // Return users which have roles
    @Override
    public List<UserModel> findAllByRole(UserRole userRole){
        List<UserEntity> userEntityList = jpa.findByUserRoleSetContaining(userRole);
        List<UserModel> userDomainList = new ArrayList<>();

        for(UserEntity userEntity : userEntityList){
            UserModel user = UserMapper.toDomain(userEntity);
            userDomainList.add(user);
        }
        return userDomainList;
    }

    // Return users which have name and roles
    @Override
    public List<UserModel> findByNameAndRole(String userName, UserRole role) {
        List<UserEntity> userEntityList = jpa.findByUserNameAndUserRoleSetContaining(userName, role);
        List<UserModel> userModelList = new ArrayList<>();

        for (UserEntity userEntity : userEntityList)
            userModelList.add(UserMapper.toDomain(userEntity));
        return userModelList;
    }

    @Override
    public void deleteAll() {
        try {
            jpa.deleteAllInBatch();
        }
        catch (UnsupportedOperationException ex) {
            jpa.deleteAll();
        }
    }
}