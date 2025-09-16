// src/main/java/com/hospital/infrastructure/persistance/jpa/entity/RatingEntity.java
package com.hospital.infrastructure.persistance.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
  name = "ratings",
  uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id","patient_id"})
)
public class RatingEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID ratingId;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "conversation_id")
    private ConversationEntity conversation;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "patient_id")
    private UserEntity patient;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "doctor_id")
    private UserEntity doctor;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UUID getRatingId() { return ratingId; }
    public ConversationEntity getConversation() { return conversation; }
    public UserEntity getPatient() { return patient; }
    public UserEntity getDoctor() { return doctor; }
    public int getScore() { return score; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRatingId(UUID ratingId) { this.ratingId = ratingId; }
    public void setConversation(ConversationEntity conversation) { this.conversation = conversation; }
    public void setPatient(UserEntity patient) { this.patient = patient; }
    public void setDoctor(UserEntity doctor) { this.doctor = doctor; }
    public void setScore(int score) { this.score = score; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public RatingEntity() {}
}
