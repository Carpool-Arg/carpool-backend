package com.carpool.carpool.service.statistics.admin.trips;

import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.response.Response;

public interface IAdminTripsStatsService {

  Response<TopCityStatResponseDTO> getTopOriginCitiesStat();
}
