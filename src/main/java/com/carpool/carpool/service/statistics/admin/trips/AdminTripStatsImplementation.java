package com.carpool.carpool.service.statistics.admin.trips;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
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
    public Response<TopCityStatResponseDTO> getTopOriginCitiesStat() {
        List<Object[]> raw = adminTripsStatisticsRepository.findTop3CitiesByType("ORIGIN");
        List<TopCityStatDTO> cities = raw.stream()
            .map(row -> TopCityStatDTO.builder()
                .cityName((String) row[0])
                .reservationCount(((Number) row[1]).longValue())
                .build())
        .toList();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de ciudades obtenidas con éxito."),
            TopCityStatResponseDTO.builder()
            .cities(cities)
            .build()
        );

    }

    @Override
    public Response<TopCityStatResponseDTO> getTopDestinationCitiesStat() {
        List<Object[]> raw = adminTripsStatisticsRepository.findTop3CitiesByType("DESTINATION");
        List<TopCityStatDTO> cities = raw.stream()
            .map(row -> TopCityStatDTO.builder()
                .cityName((String) row[0])
                .reservationCount(((Number) row[1]).longValue())
                .build())
        .toList();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de ciudades obtenidas con éxito."),
            TopCityStatResponseDTO.builder()
            .cities(cities)
            .build()
        );

    }

    @Override
    public Response<DriverPercentageStatResponseDTO> getDriverPercentageByPeriod(LocalDate fromDate, LocalDate toDate) {

        if(fromDate.isAfter(toDate)){
            throw new BadRequestException("La fecha desde no puede ser mayor a la fecha hasta.");
        }

        double percentage = Optional.ofNullable(
                adminTripsStatisticsRepository.calculateDriverPercentage(fromDate, toDate)
        ).orElse(0.0);

        return ResponseUtils.buildOKResponse(
            List.of("Estadisticas de procentaje de choferes obtenida con éxito."), 
            DriverPercentageStatResponseDTO.builder()
                .driverPercentage(percentage)
                .build()
        );
    }

}
