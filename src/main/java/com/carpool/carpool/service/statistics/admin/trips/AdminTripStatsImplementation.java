package com.carpool.carpool.service.statistics.admin.trips;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.response.Response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AdminTripStatsImplementation implements IAdminTripsStatsService{@Override
  
  public Response<TopCityStatResponseDTO> getTopOriginCitiesStat() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getTopOriginCitiesStat'");
  }

}
