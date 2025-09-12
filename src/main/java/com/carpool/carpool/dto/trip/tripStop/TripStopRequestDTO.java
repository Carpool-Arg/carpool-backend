package com.carpool.carpool.dto.trip.tripStop;



import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TripStopRequestDTO {

    @NotNull(message = "La ciudad no puede ser nula.")
    private Long cityId;

    @NotNull(message = "Debes definir si la parada es el origen del viaje.")
    private boolean start;

    @NotNull(message = "Debes definir si la parada es el destino del viaje.")
    private boolean destination;

    @NotNull(message = "El orden de la parada no puede ser nulo.")
    @Min(value=1, message = "Debe especificar un orden correcto para las paradas.")
    private int order;

    @NotNull(message = "Debes especificar una observacion para cada parada.")
    private String observation;


    @AssertTrue(message = "Una ciudad no puede ser origen y destino al mismo tiempo")
    @JsonIgnore
    public boolean isOriginOrDestinationValid() {
        return !(start && destination);
    }
}
