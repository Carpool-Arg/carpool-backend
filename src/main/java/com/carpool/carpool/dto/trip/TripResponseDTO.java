package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripResponseDTO {
    private Long id;
    private String driverName;
        //TODO: poner la clasificación del chofer, cuando see haga
    private List<TripStopResponseDTO> tripStops;
    private LocalDateTime startDateTime;
    private int availableSeat;
    private String availableBaggage;
    private double seatPrice;

}
