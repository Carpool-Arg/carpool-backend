package com.carpool.carpool.dto.trip;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class TripDriverResponseDTO {
    private List<TripDriverDTO> trips;
}
