package com.carpool.carpool.service.statistics.admin.user;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.user.VerifiedUserDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;

public interface IAdminUserStatsService {

    /**
     * Método para obtener el porcentaje histórico de usuarios registrados como choferes 
     * respecto a la cantidad total de usuarios activos.
     * @return Response con el porcentaje calculado.
     */
    Response<DriverPercentageStatResponseDTO> getDriverPercentage();
    /**
     * 
     * @param fromDate
     * @param toDate
     * @param groupBy
     * @return
     */
    Response<AdminStatSimpleDTO> getNewUsersStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy);
    
    /**
     * 
     * @return
     */
    Response<VerifiedUserDTO> getVerifiedUsersStats();
}
