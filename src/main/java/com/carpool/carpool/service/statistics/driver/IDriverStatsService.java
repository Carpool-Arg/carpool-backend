package com.carpool.carpool.service.statistics.driver;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;

public interface IDriverStatsService {
  Response<DriverStatResponseDTO> getKmStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  Response<DriverStatResponseDTO> getEarningStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  Response<DriverStatResponseDTO> getTripsStats(
    LocalDate fromDate,
    LocalDate toDate,
    GroupByEnum groupBy
  );

  Response<Co2StatResponseDTO> getCo2Stats();
}
