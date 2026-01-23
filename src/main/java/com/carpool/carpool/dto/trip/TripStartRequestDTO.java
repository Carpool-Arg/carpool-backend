package com.carpool.carpool.dto.trip;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripStartRequestDTO {
    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;
}
