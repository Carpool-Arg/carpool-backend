package com.carpool.carpool.dto.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateReservationRequestDTO {

    @NotNull(message = "El viaje es un dato obligatorio")
    private Long trip;

    @NotNull(message = "Debe indicar la localidad origen")
    private Long startCity;

    @NotNull(message = "Debe indicar la localidad destino")
    private Long destinationCity;

    @NotNull(message = "El equipaje es un dato obligatorio.")
    private boolean baggage;
}
