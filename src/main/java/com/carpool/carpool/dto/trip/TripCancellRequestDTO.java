package com.carpool.carpool.dto.trip;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TripCancellRequestDTO {
    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;

    @Size(max = 100, message = "El motivo no puede superar los 100 caracteres")
    private String reason;
}
