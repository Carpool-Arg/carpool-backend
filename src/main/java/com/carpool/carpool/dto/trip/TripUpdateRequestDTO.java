package com.carpool.carpool.dto.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.tripStop.TripStopUpdateRequestDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TripUpdateRequestDTO {

	@NotNull(message = "El id del viaje es un dato obligatorio.")
	private Long idTrip;
	
    @FutureOrPresent(message = "La fecha de inicio debe ser igual o posterior a la actual.")
    private LocalDateTime startDateTime;

    @Min(value = 1, message = "Debe indicar una cantidad correcta de asientos.")
    private Integer availableSeat;

    @NotBlank(message = "El tipo de equipaje es un dato obligatorio.")
    private String availableBaggage;

    @DecimalMin(value = "1.0", message = "El precio debe tener un valor minimo de $ 1.0")
    private Double seatPrice;

    @Min(value = 1, message = "Debe indicar un id válido.")
    private Long idVehicle;

    @Size(min = 2, message = "Debe haber 2 o mas paradas.")
    private List<TripStopUpdateRequestDTO> tripStops;
}
 