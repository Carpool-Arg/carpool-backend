package com.carpool.carpool.model.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.carpool.carpool.enums.user.UserStatus;
import com.carpool.carpool.model.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede quedar en blanco.")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracter.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre debe contener sólo letras y espacios.")
    private String name;

    @Size(min = 1, max = 100, message = "El apellido debe tener entre 1 y 100 caracter.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido debe contener sólo letras y espacios.")
    private String lastname;

    @Size(min = 3, max = 25, message = "El nombre de usuario debe tener entre 3 y 25 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    private String username;

    @NotBlank(message = "El correo electrónico no puede quedar en blanco.")
    @Size(max = 75, message = "El correo electrónico debe tener como máximo 75 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico debe ser una direccón de correo válida.")
    private String email;

    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;

    @Size(min = 7, max = 50, message = "El número del DNI debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9]+$", message = "El número del DNI debe contener únicamente números.")
    private String dni;

    @Size(min = 7, max = 25, message = "El número de teléfono debe tener entre 7 y 50 caracteres.")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @JsonIgnoreProperties({"users", "handler", "hibernateLazyInitializer"})
    @ManyToMany
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="role_id"),
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "role_id"})}
    )
    private List<Role> roles;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

    @Column(name="account_status")
    @Enumerated(EnumType.STRING)
    private UserStatus accountStatus;

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
        this.accountStatus = UserStatus.ACTIVE;
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

    public boolean isAccountNonLockedOrSuspended(){
        return accountStatus==UserStatus.ACTIVE;
    }
}
