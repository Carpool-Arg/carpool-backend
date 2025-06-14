package com.carpool.carpool.dto.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
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

    @NotNull(message = "The Password cannot be null.")
    @Size(min = 8, message = "The Password must be at least 8 characters long.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "The Password must contain at least one lowercase letter, one uppercase letter, and one number")
    private String confirmPassword;

    @NotNull(message = "The DNI number cannot be null.")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "The DNI number must be a valid 7 or 8 digit number.")
    @Column(unique = true)
    private String dni;

    @NotNull(message = "The Phone number cannot be null.")
    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "The phone number must contain only numbers, dashes, plus signs, and spaces.")
    private String phone;
}
