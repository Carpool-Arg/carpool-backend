package com.carpool.carpool.model.review;

import java.time.LocalDateTime;

import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="review")
@AllArgsConstructor
@NoArgsConstructor  
@Builder
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="description", nullable = true)
  private String description;

  @Column(name = "stars", nullable = false)
  private int stars;

  @ManyToOne
  @JoinColumn(
    name = "target_user_id",
    referencedColumnName = "id",
    nullable = false
  )

  private User targetUser;

  @ManyToOne
  @JoinColumn(
    name = "reviewer_user_id",
    referencedColumnName = "id",
    nullable = false
  )

  private User reviewerUser;

  @ManyToOne
  @JoinColumn(
    name = "trip_id",
    referencedColumnName = "id",
    nullable = false
  )

  private Trip trip;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

