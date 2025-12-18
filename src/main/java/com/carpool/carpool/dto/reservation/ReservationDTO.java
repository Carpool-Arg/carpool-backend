package com.carpool.carpool.dto.reservation;

import com.carpool.carpool.model.trip.tripStop.TripStop;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String startCity;
    private String destinationCity;
    private boolean baggage;
    private String nameUser;
    private String lastNameUser;
    private String urlImage;
    private String state;
}
