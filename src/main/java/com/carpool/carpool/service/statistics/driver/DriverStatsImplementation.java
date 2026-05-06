package com.carpool.carpool.service.statistics.driver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.StatMetricPointDTO;
import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.exception.BadRequestException;
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
  private static final double CO2_PER_KM = 0.13;

  @Override
  public Response<DriverStatResponseDTO> getKmStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
    User user = getAuthenticatedUser();

    if(fromDate.isAfter(toDate)){
      throw new BadRequestException("La fecha desde no puede ser mayor a la fecha hasta.");
    }

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
            .totalFiltered(kmFiltered)
            .historialByPeriod(metrics)
            .build()
    );

  }

  @Override
  public Response<DriverStatResponseDTO> getEarningStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
    User user = getAuthenticatedUser();
    if(fromDate.isAfter(toDate)){
      throw new BadRequestException("La fecha desde no puede ser mayor a la fecha hasta.");
    }
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
            .totalFiltered(earningFiltered)
            .historialByPeriod(metrics)
            .build()
    );

  }

  @Override
  public Response<DriverStatResponseDTO> getTripsStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
    User user = getAuthenticatedUser();
    if(fromDate.isAfter(toDate)){
      throw new BadRequestException("La fecha desde no puede ser mayor a la fecha hasta.");
    }
    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);
    // Total histórico sin filtro de fechas
    Long historicalTotal = driverStatisticsRepository
            .countTripsHistoricalByUserId(user.getId());

    // Total dentro del rango filtrado
    Long tripTotalFiltered = driverStatisticsRepository
      .countTripsByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime);

    // Serie temporal agrupada
    List<Object[]> rawMetrics = driverStatisticsRepository
      .findTripMetricsByGrouping(user.getId(), fromDateTime, toDateTime, groupBy.name());
    
    List<StatMetricPointDTO> metrics = rawMetrics.stream()
    .map(row -> StatMetricPointDTO.builder()
        .label((String) row[0])
        .value(((Number) row[1]).doubleValue())
        .build())
    .toList();

    return ResponseUtils.buildOKResponse(
        List.of("Estadísticas de kilómetros obtenidas con éxito."),
        DriverStatResponseDTO.builder()
            .historialTotal(historicalTotal != null ? historicalTotal.doubleValue() : 0.0)
            .totalFiltered(tripTotalFiltered != null ? tripTotalFiltered.doubleValue() : 0.0)
            .historialByPeriod(metrics)
            .build()
    );

  }

  @Override
  public Response<Co2StatResponseDTO> getCo2Stats(){
    User user = getAuthenticatedUser();

    double totalCo2Saved = Optional.ofNullable(
        driverStatisticsRepository.calculateCo2SavedByUserId(user.getId(), CO2_PER_KM)
    ).orElse(0.0);

    return ResponseUtils.buildOKResponse(
        List.of("CO2 ahorrado calculado con éxito."),
        Co2StatResponseDTO.builder()
            .totalCo2Saved(totalCo2Saved)
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
