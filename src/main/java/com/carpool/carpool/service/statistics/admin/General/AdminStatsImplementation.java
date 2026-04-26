package com.carpool.carpool.service.statistics.admin.general;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminCo2StatDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.repository.statistics.admin.general.AdminStatisticsRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsImplementation implements IAdminStatsService {

    private final AdminStatisticsRepository adminStatisticsRepository; 
    private static final double C02_PER_KM = 0.13;  

    @Override
    public Response<AdminStatSimpleDTO> getAppEarningsStats(LocalDate fromDate, LocalDate toDate) {
        validateDate(fromDate, toDate); 
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime   = toDate   != null ? toDate.atTime(23, 59, 59) : null;

        double historicalTotal = Optional.ofNullable(
            adminStatisticsRepository.sumAppEarningsHistorical()
        ).orElse(0.0);

        double totalFiltered = Optional.ofNullable(
            adminStatisticsRepository.sumAppEarningsByDateRange(fromDateTime, toDateTime)
        ).orElse(0.0);

        return ResponseUtils.buildOKResponse(
            List.of("Ganancias de la aplicación obtenidas con éxito."),
            AdminStatSimpleDTO.builder()
                .historicalTotal(historicalTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(null)
                .build()
        );
    }

    @Override
    public Response<AdminStatSimpleDTO> getTotalTransactedStats(LocalDate fromDate, LocalDate toDate) {
        validateDate(fromDate, toDate); 
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime   = toDate   != null ? toDate.atTime(23, 59, 59) : null;
        
       double historicalTotal = Optional.ofNullable(
            adminStatisticsRepository.sumTotalTransactedHistorical()
        ).orElse(0.0);

        double totalFiltered = Optional.ofNullable(
            adminStatisticsRepository.sumTotalTransactedByDateRange(fromDateTime, toDateTime)
        ).orElse(0.0);

        return ResponseUtils.buildOKResponse(
            List.of("Monto transaccionado obtenido con éxito."),
            AdminStatSimpleDTO.builder()
                .historicalTotal(historicalTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(null)
                .build()
        );
    }

    @Override
    public Response<AdminStatSimpleDTO> getFinishedTripsStats(LocalDate fromDate, LocalDate toDate) {

        validateDate(fromDate, toDate); 
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime   = toDate   != null ? toDate.atTime(23, 59, 59) : null;

        double historicalTotal = Optional.ofNullable(
            adminStatisticsRepository.countFinishedTripsHistorical()
        ).orElse(0L).doubleValue();

        double totalFiltered = Optional.ofNullable(
            adminStatisticsRepository.countFinishedTripsByDateRange(fromDateTime, toDateTime)
        ).orElse(0L).doubleValue();

        return ResponseUtils.buildOKResponse(
            List.of("Cantidad de viajes finalizados obtenida con éxito."),
            AdminStatSimpleDTO.builder()
                .historicalTotal(historicalTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(null)
                .build()
        );
    }

    @Override
    public Response<AdminCo2StatDTO> getCo2Stats() {
        double totalCo2Saved = Optional.ofNullable(
            adminStatisticsRepository.calculateCo2SavedHistorical(C02_PER_KM)
        ).orElse(0.0);

        return ResponseUtils.buildOKResponse(
            List.of("CO2 ahorrado obtenido con éxito."),
            AdminCo2StatDTO.builder()
                .totalC02Saved(totalCo2Saved)
                .build()
        );
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
