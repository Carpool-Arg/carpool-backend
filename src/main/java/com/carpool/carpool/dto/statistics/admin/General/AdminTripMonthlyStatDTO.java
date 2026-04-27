package com.carpool.carpool.dto.statistics.admin.general;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTripMonthlyStatDTO {
    private long currentMonthTrips; 
    private double delta; 
}
