package com.carpool.carpool.dto.statistics.admin.general;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTripPublishedStatDTO {
    private long historicalTotal;
    private long totalFiltered;
}
