package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripResponseDTO {
    private Long id;
    private String driverName;
        //TODO: poner la clasificación del chofer, cuando see haga
    private String originCity;
    private String destinationCity;
    private String intermediateCity;
        //TODO: esperar a cambios que realicen los chicos
    private LocalDateTime startDateTime;
    private int availableSeat;
    private String availableBaggage;
    private double seatPrice;

}
