package com.carpool.carpool.service.statistics.admin.trips;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.response.Response;

public interface IAdminTripsStatsService {

  /**
   * Meotodo para obtener las 3 ciudades mas elegidas como origen por los pasajeros
   * @return
   */
  Response<TopCityStatResponseDTO> getTopOriginCitiesStat();
  
  /**
   * Meotodo apra obtener las 3 ciudades mas elegidas como destino por los pasajeros
   * @return
   */
  Response<TopCityStatResponseDTO> getTopDestinationCitiesStat();

  /**
   * Metodo para obtener el procentaje de usuarios registrados como choferes respecto a la cantidad toal de usuarios en un periodo especifico
   * @param fromDate fecha desde
   * @param toDate fecha hasta
   * @return
   */
  Response<DriverPercentageStatResponseDTO> getDriverPercentageByPeriod(
    LocalDate fromDate,
    LocalDate toDate
  );
}
