package com.carpool.carpool.dto.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelReservationByPassengerRequestDTO {
    @NotNull(message = "El codigo de la reserva es un dato obligatorio")
    private Long reservationId;
}

