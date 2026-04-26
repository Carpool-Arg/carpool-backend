package com.carpool.carpool.service.statistics.admin.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.statistics.StatMetricPointDTO;
import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.user.VerifiedUserDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.repository.statistics.admin.user.AdminUserStatisticsRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserStatsImplementation implements IAdminUserStatsService {   

    private final AdminUserStatisticsRepository adminUserStatisticsRepository; 


    @Override
    public Response<DriverPercentageStatResponseDTO> getDriverPercentage() {

        double percentage = Optional.ofNullable(
            adminUserStatisticsRepository.calculateDriverPercentage()
        ).orElse(0.0);

        return ResponseUtils.buildOKResponse(
            List.of("Estadística de porcentaje de choferes obtenida con éxito."), 
            DriverPercentageStatResponseDTO.builder()
                .driverPercentage(percentage)
                .build()
        );
    }

    @Override
    public Response<AdminStatSimpleDTO> getNewUsersStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy) {
        validateDate(fromDate, toDate); 
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;

        if (groupBy == GroupByEnum.WEEK) {
            fromDateTime = LocalDateTime.now().minusDays(7);
            toDateTime   = LocalDateTime.now();
        } else {
            fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            toDateTime   = toDate   != null ? toDate.atTime(23, 59, 59) : null;
        }

        double historicalTotal = Optional.ofNullable(
            adminUserStatisticsRepository.countNewUsersByDateRange(null, null)
        ).orElse(0L).doubleValue();

        double totalFiltered = Optional.ofNullable(
            adminUserStatisticsRepository.countNewUsersByDateRange(fromDateTime, toDateTime)
        ).orElse(0L).doubleValue();

        List<Object[]> raw = adminUserStatisticsRepository.findNewUsersByGrouping(
            fromDateTime, toDateTime, groupBy.name());

        List<StatMetricPointDTO> metrics = raw.stream()
            .map(row -> StatMetricPointDTO.builder()
                .label((String) row[0])
                .value(((Number) row[1]).doubleValue())
                .build())
            .toList();

        return ResponseUtils.buildOKResponse(
            List.of("Estadísticas de nuevos usuarios obtenidas con éxito."),
            AdminStatSimpleDTO.builder()
                .historicalTotal(historicalTotal)
                .totalFiltered(totalFiltered)
                .historialByPeriod(metrics)
                .build()
        );
    }

    @Override
    public Response<VerifiedUserDTO> getVerifiedUsersStats() {
        double total = Optional.ofNullable(
            adminUserStatisticsRepository.countVerifiedUsersHistorical()
        ).orElse(0L).doubleValue();

        return ResponseUtils.buildOKResponse(
            List.of("Total de usuarios verificados obtenido con éxito."),
            VerifiedUserDTO.builder()
                .totalVerified(total)
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
