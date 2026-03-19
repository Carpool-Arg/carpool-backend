package com.carpool.carpool.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReviewDriverRequestDTO {
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe ser al menos 1") 
    @Max(value = 5, message = "La calificación no puede ser mayor a 5")
    private Integer stars;

    @Size(max = 250, message = "El comentario no puede superar los 250 caracteres")
    private String description;

    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;
}
