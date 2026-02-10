package com.carpool.carpool.model.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.carpool.carpool.enums.user.UserGenderEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.validators.genderValidEnum.GenderValidEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String lastname;

    @Column(unique = true, length = 25)
    private String username;

    @Column(nullable = false, unique = true, length = 75)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true, length = 8)
    private String dni;

    @Column(length = 25)
    private String phone;

    @Column(name = "birth_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @GenderValidEnum
    private UserGenderEnum gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStateEnum status;

    @JsonIgnoreProperties({"users", "handler", "hibernateLazyInitializer"})
    @ManyToMany
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="role_id"),
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "role_id"})}
    )
    private List<Role> roles;

    @Column(name = "pending_email", length = 75) 
    private String pendingEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

    @Column(name="failed_attempts", nullable = false) 
    private int failedAttempts;

    @Column(name="lock_time")
    private Date lockTime;

    @Column(name="last_failed_login_time")
    private Date lastFailedLoginTime;

    @Column(name="rating")
    private double rating;

    @OneToMany(mappedBy = "reviewerUser",cascade = CascadeType.ALL,orphanRemoval = true)
    private transient List<Review> writtenReviews;

    @OneToMany(mappedBy = "targetUser",cascade = CascadeType.ALL,orphanRemoval = true)
    private transient List<Review> recievedReviews;

    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
        this.failedAttempts = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    public boolean isEnabled(){
        return deletedAt == null;
    }

    public boolean isAccountActive(){
        return status == UserStateEnum.ACTIVE;
    }
}