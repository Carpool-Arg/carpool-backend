package com.carpool.carpool.service.statistics.admin.trips;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TakenSeatsStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.response.Response;

public interface IAdminTripsStatsService {

  /**
   * Metodo para obtener las ciudades mas elegidas como origen por los pasajeros
   * @param limit cantidad a obtener
   * @return
   */
  Response<TopCityStatResponseDTO> getTopOriginCitiesStat(int limit);
  
  /**
   * Metodo apra obtener las ciudades mas elegidas como destino por los pasajeros
   * * @param limit cantidad a obtener
   * @return
   */
  Response<TopCityStatResponseDTO> getTopDestinationCitiesStat(int limit);

  /**
   * Metodo para obtener estadisticas acerca de la cantida de asientos ocupados respecto a los publicados
   * @param fromDate
   * @param toDate
   * @return
   */
  Response<TakenSeatsStatResponseDTO> getTakenSeatsStat(
    LocalDate fromDate,
    LocalDate toDate
  );

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
