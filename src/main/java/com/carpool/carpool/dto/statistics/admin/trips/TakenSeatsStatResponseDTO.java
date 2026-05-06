package com.carpool.carpool.dto.statistics.admin.trips;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TakenSeatsStatResponseDTO {
    double takenPercentageFiltered;
    double takenPercentageHistorical;
    Long totalTakenSeatsFiltered;
    Long totalTakenSeatsHistorical;
    Long totalUntakenSeatsFiltered;
    Long totalUntakenSeatsHistorical;
}
