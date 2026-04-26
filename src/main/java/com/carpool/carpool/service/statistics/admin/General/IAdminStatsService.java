package com.carpool.carpool.service.statistics.admin.general;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminCo2StatDTO;
import com.carpool.carpool.response.Response;

public interface IAdminStatsService {
    Response<AdminStatSimpleDTO> getAppEarningsStats(LocalDate fromDate, LocalDate toDate);
    Response<AdminStatSimpleDTO> getTotalTransactedStats(LocalDate fromDate, LocalDate toDate);
    Response<AdminStatSimpleDTO> getFinishedTripsStats(LocalDate fromDate, LocalDate toDate);
    Response<AdminCo2StatDTO> getCo2Stats();
}