package com.carpool.carpool.service.statistics.driver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.StatMetricPointDTO;
import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.statistics.driver.DriverStatisticsRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverStatsImplementation implements IDriverStatsService{
  private final DriverStatisticsRepository driverStatisticsRepository;
  private final UserRepository userRepository;

  @Override
  public Response<DriverStatResponseDTO> getKmStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
    User user = getAuthenticatedUser();

    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);
    // Total histórico sin filtro de fechas
    Double historicalTotal = driverStatisticsRepository
            .sumKmHistoricalByUserId(user.getId());

    // Total dentro del rango filtrado
    Double kmFiltered = driverStatisticsRepository
      .sumKmByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime);

    // Serie temporal agrupada
    List<Object[]> rawMetrics = driverStatisticsRepository
      .findKmMetricsByGrouping(user.getId(), fromDateTime, toDateTime, groupBy.name());
    
    List<StatMetricPointDTO> metrics = rawMetrics.stream()
    .map(row -> StatMetricPointDTO.builder()
        .label((String) row[0])
        .value(((Number) row[1]).doubleValue())
        .build())
    .toList();

    return ResponseUtils.buildOKResponse(
        List.of("Estadísticas de kilómetros obtenidas con éxito."),
        DriverStatResponseDTO.builder()
            .historialTotal(historicalTotal)
            .kmFiltered(kmFiltered)
            .historialByPeriod(metrics)
            .build()
    );

  }

  @Override
  public Response<DriverStatResponseDTO> getEarningStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
    User user = getAuthenticatedUser();

    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);
    // Total histórico sin filtro de fechas
    Double historicalTotal = driverStatisticsRepository
            .sumEarningsHistoricalByUserId(user.getId());

    // Total dentro del rango filtrado
    Double earningFiltered = driverStatisticsRepository
      .sumEarningsByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime);

    // Serie temporal agrupada
    List<Object[]> rawMetrics = driverStatisticsRepository
      .findEarningsMetricsByGrouping(user.getId(), fromDateTime, toDateTime, groupBy.name());
    
    List<StatMetricPointDTO> metrics = rawMetrics.stream()
    .map(row -> StatMetricPointDTO.builder()
        .label((String) row[0])
        .value(((Number) row[1]).doubleValue())
        .build())
    .toList();

    return ResponseUtils.buildOKResponse(
        List.of("Estadísticas de kilómetros obtenidas con éxito."),
        DriverStatResponseDTO.builder()
            .historialTotal(historicalTotal)
            .kmFiltered(earningFiltered)
            .historialByPeriod(metrics)
            .build()
    );

  }


  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));
  }
}
