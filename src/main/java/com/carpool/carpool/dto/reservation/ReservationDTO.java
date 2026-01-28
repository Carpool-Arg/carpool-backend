package com.carpool.carpool.dto.reservation;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String startCity;
    private String destinationCity;
    private boolean baggage;
    private String nameUser;
    private String lastNameUser;
    private String urlImage;
    private String state;
}
