package com.carpool.carpool.model.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.carpool.carpool.enums.user.UserGenderEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.validators.genderValidEnum.GenderValidEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    private String name;

    private String lastname;

    private String username;

    private String email;

    private String password;

    private String dni;

    private String phone;

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

    /*
     * Este campo se utiliza para almacenar un nuevo correo electrónico cuando el usuario decide cambiar su email.
     */
    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

    @Column(name="failed_attempts")
    private int failedAttempts;

    @Column(name="lock_time")
    private Date lockTime;

    @Column(name="last_failed_login_time")
    private Date lastFailedLoginTime;

    /*
     * Para el created_at empleamos la anotacicón @PrePersist.
     * Esto hace que que el método onCreate() se ejecute justo antes de que la
     * entidad se inserte en la base de datos.
     */
    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
        this.failedAttempts = 0;
    }

    /*
     * Para el updated_at empleamos la anotación @PreUpdate.
     * Esto hace que el método onUpdate() se ejecute justo antes de que la entidad
     * se actualice en la base de datos.
     */
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
