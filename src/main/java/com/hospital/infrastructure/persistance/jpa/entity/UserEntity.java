// src/main/java/com/hospital/infrastructure/persistence/jpa/entity/UserEntity.java

package com.hospital.infrastructure.persistance.jpa.entity;

import java.util.Set;
import java.util.UUID;

import com.hospital.domain.role.UserRole;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, updatable= false)
    private UUID userID;

    @Column (name = "name", nullable = false, length = 80)
    private String userName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<UserRole> userRoleSet;

    public UUID getUserID(){ return userID; }
    public String getUserName(){ return userName; }
    public Set<UserRole> getUserRoles(){ return userRoleSet; }

    public void setUserEntityId(UUID userID){ this.userID = userID; }
    public void setUserEntityName(String userName){ this.userName = userName; }
    public void setUserEntityRoles(Set<UserRole> userRoleSet){
        if (userRoleSet != null)
            this.userRoleSet = userRoleSet; 
    }
    public UserEntity() {}
}