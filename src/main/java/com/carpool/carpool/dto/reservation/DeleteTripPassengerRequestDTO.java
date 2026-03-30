package com.carpool.carpool.dto.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DeleteTripPassengerRequestDTO {
  @NotNull(message = "El IDde la reserva es obligatorio")
  private Long reservationId;

  @Size(max = 100, message = "El motivo no puede superar los 100 caracteres")
  private String reason;
}
