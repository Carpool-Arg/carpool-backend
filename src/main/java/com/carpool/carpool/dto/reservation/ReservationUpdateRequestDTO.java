package com.carpool.carpool.dto.reservation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReservationUpdateRequestDTO {

    @NotNull(message = "El id de la reserva es obligatorio")
    private Long idReservation;

    private boolean reject;
}
