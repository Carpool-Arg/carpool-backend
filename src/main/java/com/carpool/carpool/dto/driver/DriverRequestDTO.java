package com.carpool.carpool.dto.driver;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverRequestDTO {
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

    @NotNull(message = "La ciudad es un dato obligatorio.")
    private Long cityId;

    @NotNull(message = "El ID de la clase de la licencia de conducir es obligatorio.")
    @Min(value = 1, message = "El ID de la clase de licencia debe ser un número positivo.")
    private Long licenseClassId;

    @NotNull(message = "La foto del frente del carnet es obligatoria.")
    private MultipartFile frontLicensePhoto;

    @NotNull(message = "La foto del dorso del carnet es obligatoria.")
    private MultipartFile backLicensePhoto;
}
