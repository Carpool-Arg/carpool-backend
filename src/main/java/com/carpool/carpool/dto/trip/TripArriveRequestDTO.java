package com.carpool.carpool.dto.trip;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TripArriveRequestDTO {

  @NotNull(message = "El identificador de la parada es obligatorio.")
  @Min(value = 0, message = "Ingrese un valor válido para el indetificador de la parada.")
  private long idTripStop;
  
}
