package com.carpool.carpool.dto.driver;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLicenseVerifyRequestDTO {
    @NotNull(message = "La decisión es obligatoria.")
    private Boolean approved;

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres.")
    private String rejectionReason;
}
