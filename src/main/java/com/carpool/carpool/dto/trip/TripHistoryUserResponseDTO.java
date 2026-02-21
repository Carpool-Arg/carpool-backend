package com.carpool.carpool.dto.trip;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class TripHistoryUserResponseDTO {
	private List<TripHistoryUserDTO> trips;
}
