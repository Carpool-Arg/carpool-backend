package com.carpool.carpool.service.statistics.passenger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.StatMetricPointDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.statistics.passenger.PassengerStatisticRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerStatsImplementation implements IPassengerStatsService {

    private final PassengerStatisticRepository passengerStadisticRepository;
    private final UserRepository userRepository;

    private static final double CO2_PER_KM = 0.13;
    
    @Override
    public Response<PassengerStatResponseDTO> getKmStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
        User user = getAuthenticatedUser();
        validateDate(fromDate, toDate);

        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);

        //Total del historico recorrido del passenger 
        double historialKmTotal = Optional.ofNullable(
            passengerStadisticRepository.sumKmHistoricalByUserId(user.getId())
        ).orElse(0.0);

        // Filtro de fechas 
        double totalFiltered = Optional.ofNullable(
            passengerStadisticRepository.sumKmByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime)
        ).orElse(0.0);
        
        List<Object[]> kmStats = passengerStadisticRepository.findKmMetricsByGrouping(user.getId(), fromDateTime, toDateTime, groupBy.name());
        
        List<StatMetricPointDTO> metrics = kmStats.stream()
            .map(row -> StatMetricPointDTO.builder()
                .label((String) row[0])
                .value(((Number) row[1]).doubleValue())
                .build())
            .toList();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de kilómetros obtenidas con éxito."),
            PassengerStatResponseDTO.builder()
                .historialTotal(historialKmTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(metrics)
                .build()
        );
    }


    @Override
    public Response<PassengerStatResponseDTO> getTripStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
        User user = getAuthenticatedUser();
        validateDate(fromDate, toDate);
        
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);

        double historialTripsTotal = Optional.ofNullable(
            passengerStadisticRepository.countCompletedTripsHistoricalByUserId(user.getId())
        ).orElse(0L).doubleValue();

        double totalFiltered = Optional.ofNullable(
            passengerStadisticRepository.countCompletedTripsByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime)
        ).orElse(0L).doubleValue();

        List<Object[]> tripStats = passengerStadisticRepository.findTripMetricsByGrouping(user.getId(), fromDateTime, toDateTime, groupBy.name());

        List<StatMetricPointDTO> metrics = tripStats.stream()
            .map(row -> StatMetricPointDTO.builder()
                .label((String) row[0])
                .value(((Number) row[1]).doubleValue())
                .build())
            .toList();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de viajes obtenidas con éxito."),
            PassengerStatResponseDTO.builder()
                .historialTotal(historialTripsTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(metrics)
                .build()
        );
    }

    @Override
    public Response<Co2StatResponseDTO> getCo2Stats() {
        User user = getAuthenticatedUser();

        double totalCo2Saved = Optional.ofNullable(
            passengerStadisticRepository.calculateCo2SavedByUserId(user.getId(), CO2_PER_KM)
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

    private void validateDate(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new ConflictException("Las fechas 'desde' y 'hasta' son obligatorias.");
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new ConflictException("La fecha 'desde' no puede ser mayor a la fecha 'hasta'.");
        }

        if (fromDate.isAfter(LocalDate.now())) {
            throw new ConflictException("La fecha 'desde' no puede ser una fecha futura.");
        }
    }
    
}
