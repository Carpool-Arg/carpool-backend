package com.carpool.carpool.dto.media;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MediaIdRequestDTO {

    @NotNull(message = "El identificador del archivo no puede ser nulo")
    private Long mediaId;
}
