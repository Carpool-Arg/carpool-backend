package com.carpool.carpool.dto.driver;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DriverPendingPageResponseDTO {
    private long total;
    private List<DriverPendingResponseDTO> drivers;
}
