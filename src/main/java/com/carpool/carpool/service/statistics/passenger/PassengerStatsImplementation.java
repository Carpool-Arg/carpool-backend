package com.carpool.carpool.service.statistics.passenger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.passenger.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.StatMetricPointDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.statistics.passenger.PassengerStadisticRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerStatsImplementation implements IPassengerStatsService {

    private final PassengerStadisticRepository passengerStadisticRepository;
    private final UserRepository userRepository;

    private static final double CO2_PER_KM = 0.13;
    
    @Override
    public Response<PassengerStatResponseDTO> getKmStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
        User user = getAuthenticatedUser();
        
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);

        //Total del historico recorrido del passenger 
        double historialKmTotal = Optional.ofNullable(
            passengerStadisticRepository.sumKmByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime)
        ).orElse(0.0);

        // Filtro de fechas 
        double kmFiltered = Optional.ofNullable(
            passengerStadisticRepository.sumKmByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime)
        ).orElse(0.0);
        
        // agrupados por métricas 
        List<Object[]> kmStats = passengerStadisticRepository.findKmMetricsByGrouping(
            user.getId(), fromDateTime, toDateTime, groupBy.name());
        
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
                .kmFiltered(kmFiltered)
                .historialByPeriod(metrics)
                .build()
        );
    }


    @Override
    public Response<PassengerStatResponseDTO> getTripStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
        User user = getAuthenticatedUser();
        
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime   = toDate.atTime(23, 59, 59);

        double historialTripsTotal = Optional.ofNullable(
            passengerStadisticRepository.countCompletedTripsByUserIdAndDateRange(user.getId(), null, null)
        ).orElse(0L).doubleValue();

        double kmFiltered = Optional.ofNullable(
            passengerStadisticRepository.countCompletedTripsByUserIdAndDateRange(user.getId(), fromDateTime, toDateTime)
        ).orElse(0L).doubleValue();

        List<Object[]> tripStats = passengerStadisticRepository.findTripMetricsByGrouping(
            user.getId(), fromDateTime, toDateTime, groupBy.name());

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
                .kmFiltered(kmFiltered)
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
    
    //TODO: para el calculo de C02 ahorrado se debe de multiplicar el C02 ahorrado unitario * la cantidad de gente que viajo y a eso se le resta el unitario. Tengo que tener en cuenta toda la gente que viajo conmigo en todos los viajes que estuve. Debe de representar una cantidad ahorrada. hay que tener en cuenta varias cosas (cantidad de personas que viajaron conmigo, )

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));
    }
    
}
