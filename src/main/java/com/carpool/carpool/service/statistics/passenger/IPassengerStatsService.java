package com.carpool.carpool.service.statistics.passenger;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;

public interface IPassengerStatsService {

    Response<PassengerStatResponseDTO> getKmStats(
        LocalDate fromDate,
        LocalDate toDate,
        GroupByEnum groupBy
    );
    
    Response<PassengerStatResponseDTO> getTripStats(
        LocalDate fromDate, 
        LocalDate toDate, 
        GroupByEnum groupBy
    );
    
    Response<Co2StatResponseDTO> getCo2Stats();
}