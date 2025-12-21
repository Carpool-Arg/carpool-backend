package com.carpool.carpool.dto.trip;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;


@Getter
public class TripRequestDTO {

    @NotNull(message = "La fecha de inicio del viaje es un dato obligatorio.")
    @FutureOrPresent(message = "La fecha de inicio debe ser igual o posterior a la actual.")
    private LocalDateTime startDateTime;

    @Min(value = 1, message = "Debe indicar una cantidad correcta de asientos.")
    private int availableSeat;

    @NotBlank(message = "El tipo de equipaje es un dato obligatorio.")
    private String availableBaggage;

    @NotNull(message = "El precio de los asientos es un dato obligatorio.")
    @DecimalMin(value = "1.0", message = "El precio debe tener un valor minimo de $ 1.0")
    private double seatPrice;

    @NotNull(message = "El vehiculo es un dato obligatorio.")
    private Long idVehicle;

    @NotEmpty(message = "La lista de paradas no puede estar vacia.")
    @Size(min = 2, message = "Debe haber 2 o mas paradas.")
    private List<TripStopRequestDTO> tripStops;
}
