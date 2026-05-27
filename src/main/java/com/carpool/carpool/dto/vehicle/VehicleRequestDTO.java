package com.carpool.carpool.dto.vehicle;

import com.carpool.carpool.validators.yearNotInFuture.YearNotInFuture;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleRequestDTO {
    @Pattern(
        regexp = "^[A-Z]{3}[0-9]{3}$|^[A-Z]{2}[0-9]{3}[A-Z]{2}$",
        message = "La patente debe ser del tipo 'AAA123' o 'AA111XX'."
    )
    @NotBlank(message = "La patente no puede estar en blanco.")
    private String domain;

    @NotBlank(message = "La marca del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[A-Za-z-]+$", message = "La marca debe contener sólo letras, números y espacios.")
    private String brand;

    @NotBlank(message = "El modelo del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El modelo debe contener sólo letras, números y espacios.")
    private String model;

    @NotNull(message = "El año del vehiculo no puede estar en blanco.")
    @Min(value = 1900, message = "El año debe ser posterior a 1900.")
    @YearNotInFuture
    private Integer year;

    @NotBlank(message = "El color del vehiculo no puede estar en blanco.")
    @Pattern(
            regexp = "^[a-zA-Z0-9# ]+$",
            message = "El color debe contener sólo letras, números, espacios o #."
    )
    private String color;

    @NotNull(message = "La cantidad de asientos disponibles no puede estar en blanco.")
    @Min(value = 2, message = "La cantidad de asientos disponibles debe ser al menos 2.")
    @Max(value = 40, message = "La cantidad de asientos disponibles no puede ser mayor a 40.")
    private Integer availableSeats;

    @NotNull(message = "El ID del tipo de vehículo no puede estar en blanco.")
    @Min(value = 1, message = "El ID del tipo de vehículo debe ser un número positivo.")
    private Long vehicleTypeId;
}
