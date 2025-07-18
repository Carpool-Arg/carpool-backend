package com.carpool.carpool.dto.vehicle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleUpdateRequestDTO {
    @NotBlank(message = "La marca del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "La marca debe contener sólo letras, números y espacios.")
    private String brand;

    @NotBlank(message = "El modelo del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El modelo debe contener sólo letras, números y espacios.")
    private String model;

    @NotNull(message = "El año del vehiculo no puede estar en blanco.")
    @Min(value = 1900, message = "El año debe ser posterior a 1900.")
    private Integer year;

    @NotBlank(message = "El color del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El color debe contener sólo letras y espacios.")
    private String color;

    @NotNull(message = "La cantidad de asientos disponibles no puede estar en blanco.")
    @Min(value = 1, message = "La cantidad de asientos disponibles debe ser al menos 1.")
    private Integer availableSeats;

    @NotNull(message = "La cantidad de equipaje disponible no puede estar en blanco.")
    @PositiveOrZero(message = "La cantidad de equipaje disponible debe ser un número positivo o cero.")
    private Float luggageCapacity;
}
