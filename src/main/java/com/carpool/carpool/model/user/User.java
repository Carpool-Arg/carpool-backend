package com.carpool.carpool.model.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.model.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The Name cannot be null.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "The Name must contain only letters and spaces.")
    private String name;

    @NotNull(message = "The Lastname cannot be null.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "The Lastname must contain only letters and spaces.")
    private String lastname;

    @NotBlank(message = "The Username cannot be blank.")
    @NotNull(message = "The Username cannot be null.")
    @Size(min = 6, message = "The Username must be at least 6 characters long.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "The Username must contain only letters, numbers, and underscores.")
    @Column(unique = true)
    private String username;

    @NotNull(message = "The Email cannot be null.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "The Email must be a valid email address.")
    @Column(unique = true)
    private String email;

    @NotNull(message = "The Password cannot be null.")
    @Size(min = 8, message = "The Password must be at least 8 characters long.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "The Password must contain at least one lowercase letter, one uppercase letter, and one number")
    private String password;

    @NotNull(message = "The DNI number cannot be null.")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "The DNI number must be a valid 7 or 8 digit number.")
    @Column(unique = true)
    private String dni;

    @NotNull(message = "The Phone number cannot be null.")
    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "The phone number must contain only numbers, dashes, plus signs, and spaces.")
    private String phone;

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

    /*
     * Para el created_at empleamos la anotacicón @PrePersist.
     * Esto hace que que el método onCreate() se ejecute justo antes de que la
     * entidad se inserte en la base de datos.
     */
    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
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

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "deleted_by")
    private Long deleted_by;

    public boolean isEnabled(){
        if(deleted_by != null){
            return false;
        }else{
            return true;
        }
    }
}
