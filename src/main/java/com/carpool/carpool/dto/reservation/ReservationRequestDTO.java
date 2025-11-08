package com.carpool.carpool.dto.reservation;

import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ReservationRequestDTO {

    @Positive(message = "El id del viaje debe ser mayor a 0")
    private Long idTrip;

    private Long idStartCity;

    private Long idDestinationCity;

    private Boolean baggage;

    private String nameState;
}
