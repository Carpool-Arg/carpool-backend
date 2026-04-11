package com.carpool.carpool.dto.reservation;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ReservationResponseDTO {
    private long total;
    private List<ReservationDTO> reservation;
}
