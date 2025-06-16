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

    @NotNull(message = "El nombre no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre debe contener sólo letras y espacios.")
    private String name;

    @NotNull(message = "El apellido no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido debe contener sólo letras y espacios.")
    private String lastname;

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @NotNull(message = "El nombre de usuario no puede ser nulo.")
    @Size(min = 6, message = "El nombre de usuario debe tener al menos 6 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario debe contener únicamente letras, números y guiones bajos.")
    @Column(unique = true)
    private String username;

    @NotNull(message = "El correo electrónico no puede ser nulo.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El correo electrónico debe ser una direccón de correo válida.")
    @Column(unique = true)
    private String email;

    @NotNull(message = "La contraseña no puede ser nula.La contraseña no puede ser nula.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número.")
    private String password;

    @NotNull(message = "El número del DNI no puede ser nulo.")
    @Pattern(regexp = "^[0-9]{8}$", message = "El número de DNI debe contener 8 dígitos.")
    @Column(unique = true)
    private String dni;

    @NotNull(message = "El número de teléfono no puede ser nulo.")
    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "El número de teléfono debe contener únicamente números, guiones, signos + y espacios.")
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
