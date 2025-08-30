package com.carpool.carpool.dto.trip;

import com.carpool.carpool.enums.trip.BaggageEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TripRequestDTO {

    @NotNull(message = "La fecha de inicio del viaje es un dato obligatorio.")
    @FutureOrPresent(message = "La fecha de inicio debe ser igual o posterior a la actual.")
    private LocalDateTime startDateTime;

    @NotBlank(message = "La ciudad de origen es un dato obligatorio.")
    private String originTown;

    @NotBlank(message = "La ciudad de destino es un dato obligatorio.")
    private String destinationTown;

    private String intermediateTown;

    @Min(value = 1, message = "Debe indicar una cantidad correcta de asientos.")
    private int availableSeat;

    @NotBlank(message = "El tipo de equipaje es un dato obligatorio.")
    private String availableBaggage;

    @NotNull(message = "El precio de los asientos es un dato obligatorio.")
    @DecimalMin(value = "0.0", message = "El precio tiene que tener un valor igual a 0 o superior.")
    private double seatPrice;

    @NotNull(message = "El vehiculo es un dato obligatorio.")
    private Long idVehicle;
}
