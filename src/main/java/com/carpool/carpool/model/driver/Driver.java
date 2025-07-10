package com.carpool.carpool.model.driver;

import java.time.LocalDate;

import com.carpool.carpool.model.city.City;
import com.carpool.carpool.model.user.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@Table(name = "driver")


public class Driver {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El carnet no puede estar en blanco.")
    @Size(max= 5, message = "El carnet debe tener un máximo de 5 caracteres.")
    @Pattern(regexp = "^(A[1-3]|B[1-2]|C[1-3]|D[1-4]|E[1-2]|F|G[1-3])$",
             message = "La clase del carnet de conducir no es válida. Debe ser de las categorías vigentes.")
    private String licenseClass;

    /*
     * FutureOrPresent: La fecha debe ser una fecha futura o presente. 
     *                  Utilizamos esta validación para asegurarnos de que la fecha de vencimiento del carnet no sea una fecha pasada.
     * Past: La fecha debe ser una fecha pasada.
     *       Utilizamos esta validación para asegurarnos de que la fecha de nacimiento del conductor
     */

    @NotNull(message = "La fecha de venciminto del Carnet es obligatoria.")
    @FutureOrPresent(message = "La fecha de vencimiento del carnet debe ser una fecha futura o presente.")
    private LocalDate licenseExpirationDate;

    @NotNull(message= "La fecha de nacimiento es obligatoria.") 
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    private LocalDate birthDate;

    @NotBlank(message= "El domicilio no puede estar en blanco.")
    @Size(max = 255, message = "El domicilio debe tener un máximo de 255 caracteres.")
    private String addressStreet;

    @NotBlank(message= "El número del domicilio no puede estar en blanco.")
    @Size(max = 255, message = "El domicilio debe tener un máximo de 255 caracteres.")
    private String addressNumber;

    @ManyToOne
    @JoinColumn(name = "locality_id",referencedColumnName = "id", nullable = false)
    private City city;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}
