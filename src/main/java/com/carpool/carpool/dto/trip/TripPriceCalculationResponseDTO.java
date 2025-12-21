package com.carpool.carpool.dto.trip;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripPriceCalculationResponseDTO {
    private double publishedSeatPrice;   
    private double driverPriceDiscount;
    private double netEarningsPerSeat;   
}