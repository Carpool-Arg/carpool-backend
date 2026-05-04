package com.carpool.carpool.service.statistics.admin.trips;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.admin.trips.TakenSeatsStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.exception.BadRequestException;
import com.carpool.carpool.repository.statistics.admin.trips.AdminTripsStatisticsRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AdminTripStatsImplementation implements IAdminTripsStatsService{
  private AdminTripsStatisticsRepository adminTripsStatisticsRepository;

    @Override
    public Response<TopCityStatResponseDTO> getTopOriginCitiesStat(int limit) {
        List<Object[]> raw = adminTripsStatisticsRepository.findTopCitiesByType("ORIGIN", limit);
        List<TopCityStatDTO> cities = raw.stream()
            .map(row -> TopCityStatDTO.builder()
                .cityName((String) row[0])
                .reservationCount(((Number) row[1]).longValue())
                .build())
        .toList();

        long totalReservations = cities.stream()
            .mapToLong(TopCityStatDTO::getReservationCount)
            .sum();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de ciudades obtenidas con éxito."),
            TopCityStatResponseDTO.builder()
            .cities(cities)
            .totalReservationsCount(totalReservations)
            .build()
        );

    }

    @Override
    public Response<TopCityStatResponseDTO> getTopDestinationCitiesStat(int limit) {
        List<Object[]> raw = adminTripsStatisticsRepository.findTopCitiesByType("DESTINATION", limit);
        List<TopCityStatDTO> cities = raw.stream()
            .map(row -> TopCityStatDTO.builder()
                .cityName((String) row[0])
                .reservationCount(((Number) row[1]).longValue())
                .build())
        .toList();
        
        long totalReservations = cities.stream()
            .mapToLong(TopCityStatDTO::getReservationCount)
            .sum();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de ciudades obtenidas con éxito."),
            TopCityStatResponseDTO.builder()
            .cities(cities)
            .totalReservationsCount(totalReservations)
            .build()
        );

    }

    @Override
    public Response<TakenSeatsStatResponseDTO> getTakenSeatsStat(LocalDate fromDate, LocalDate toDate) {
        if(fromDate.isAfter(toDate)){
            throw new BadRequestException("La fecha desde no puede ser mayor a la fecha hasta.");
        }

        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);

        Object[] historical = (Object[]) adminTripsStatisticsRepository.getSeatStatsHistorical()[0];
        Object[] filtered   = (Object[]) adminTripsStatisticsRepository.getSeatStatsFiltered(fromDateTime, toDateTime)[0];


        long takenHistorical   = toLong(historical[0]);
        long untakenHistorical = toLong(historical[1]);
        long takenFiltered     = toLong(filtered[0]);
        long untakenFiltered   = toLong(filtered[1]);

        long totalHistorical = takenHistorical + untakenHistorical;
        long totalFiltered   = takenFiltered   + untakenFiltered;

        double percentageHistorical = totalHistorical > 0
                ? Math.round((takenHistorical * 100.0 / totalHistorical) * 100.0) / 100.0
                : 0.0;

        double percentageFiltered = totalFiltered > 0
                ? Math.round((takenFiltered * 100.0 / totalFiltered) * 100.0) / 100.0
                : 0.0;

        return ResponseUtils.buildOKResponse(
            List.of("Estadisiticas de asientos obtenidas con éxito."),             
            TakenSeatsStatResponseDTO.builder()
                .takenPercentageHistorical(percentageHistorical)
                .takenPercentageFiltered(percentageFiltered)
                .totalTakenSeatsHistorical(takenHistorical)
                .totalTakenSeatsFiltered(takenFiltered)
                .totalUntakenSeatsHistorical(untakenHistorical)
                .totalUntakenSeatsFiltered(untakenFiltered)
            .build()
        );

    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        return ((Number) value).longValue();
    }

}
