package com.carpool.carpool.service.statistics.passenger;

import java.time.LocalDate;

import com.carpool.carpool.dto.statistics.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;

public interface IPassengerStatsService {
    /**
     * 
     * @param fromDate
     * @param toDate
     * @param groupBy
     * @return
     */
    Response<PassengerStatResponseDTO> getKmStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy);
    
    /**
     * 
     * @param fromDate
     * @param toDate
     * @param groupBy
     * @return
     */
    Response<PassengerStatResponseDTO> getTripStats(LocalDate fromDate, LocalDate toDate, GroupByEnum groupBy);
    
    /**
     * 
     * @return
     */
    Response<Co2StatResponseDTO> getCo2Stats();
}