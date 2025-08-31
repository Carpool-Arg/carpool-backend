package com.carpool.carpool.dto.driver;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
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
public class DriverRequestDTO {

    @NotBlank(message = "El carnet no puede estar en blanco.")
    @Size(max = 2, message = "El carnet debe tener un máximo de 2 caracteres.")
    @Pattern(
        regexp = "^(A[1-3]|B[1-2]|C[1-3]|D[1-4]|E[1-2]|F|G[1-3])$",
        message = "La clase del carnet de conducir no es válida. Debe ser de las categorías vigentes."
    )
    private String licenseClass;

    /*
     * FutureOrPresent: La fecha debe ser una fecha futura o presente.
     *                  Utilizamos esta validación para asegurarnos de que la fecha de vencimiento del carnet no sea una fecha pasada.
     * Past: La fecha debe ser una fecha pasada.
     *       Utilizamos esta validación para asegurarnos de que la fecha de nacimiento del conductor
     */
    @NotNull(message = "La fecha de vencimiento del Carnet es obligatoria.")
    @FutureOrPresent(message = "La fecha de vencimiento del carnet debe ser una fecha futura o presente.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate licenseExpirationDate;

    @NotBlank(message = "El domicilio no puede estar en blanco.")
    @Size(max = 255, message = "El domicilio debe tener un máximo de 255 caracteres.")
    private String addressStreet;

    @NotBlank(message = "El número del domicilio no puede estar en blanco.")
    @Size(max = 255, message = "El número del domicilio debe tener un máximo de 255 caracteres.")
    @Pattern(regexp = "^\\d{1,255}$", message = "El número del domicilio debe contener solo números.")
    private String addressNumber;

    @NotBlank(message = "La ciudad no puede estar en blanco.")
    @Size(max = 100, message = "La ciudad debe tener un máximo de 100 caracteres.")
    private String locality;
}
