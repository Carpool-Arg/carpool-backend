package com.carpool.carpool.service.statistics.driver;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;

public interface IDriverStatsService {
  /**
   * Metodo para obtener las estadisiticas de kilometros recorridos como chofer en un periodo especifico, agrupando por dia, semana, mes o año
   * @param fromDate fecha desde
   * @param toDate fecha hasta
   * @param groupBy agrupador
   * @return
   */
  Response<DriverStatResponseDTO> getKmStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  /**
   * Meotodo para obtener las estadisticas de ganancias de un chofer dentro de la aplicacion en un periodo especifico, agrupando por dia, semana, mes o año
   * @param fromDate fecha desde 
   * @param toDate fecha hasta 
   * @param groupBy agrupador
   * @return
   */
  Response<DriverStatResponseDTO> getEarningStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  /**
   * Meotdo para obtener estadisitcas de la cantidad de viajes realizados por un chofer en un periodo especifico, agrupando por dia, semana, mes o año
   * @param fromDate fecha desde 
   * @param toDate fecha hasta
   * @param groupBy agrupador
   * @return
   */
  Response<DriverStatResponseDTO> getTripsStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  /**
   * Meotodo para obtener la cantidad de CO2 ahorrados por un chofer en total
   * @return
   */
  Response<Co2StatResponseDTO> getCo2Stats();
}
