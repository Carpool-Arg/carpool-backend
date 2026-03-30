package com.carpool.carpool.dto.trip.tripStop;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TripStopUpdateRequestDTO extends TripStopRequestDTO{
    @NotNull(message = "El id de la parada es un dato obligatorio.")
    private Long tripStopId;
}
