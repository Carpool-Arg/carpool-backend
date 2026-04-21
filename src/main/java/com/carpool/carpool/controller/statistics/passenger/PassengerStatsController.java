package com.carpool.carpool.controller.statistics.passenger;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.statistics.passenger.Co2StatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.statistics.passenger.IPassengerStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@Tag(name = "Passenger Stats", description = "Estadísticas del pasajero")
@RequestMapping("/passenger/stats")
@RequiredArgsConstructor
public class PassengerStatsController {
    private final IPassengerStatsService passengerStatsService;

    @Operation(
        summary = "Obtener estadísticas de kilómetros recorridos",
        description = "Obtiene estadísticas de kilómetros recorridos por el pasajero en un rango de fechas específico, agrupados por día, semana o mes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas de kilómetros obtenidas con éxito."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
        @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
        @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/km")
    public ResponseEntity<Response<PassengerStatResponseDTO>> getKmStats(
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
        @RequestParam(defaultValue = "MONTH") GroupByEnum groupBy) {
        return new ResponseEntity<>(passengerStatsService.getKmStats(fromDate, toDate, groupBy), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener estadísticas de viajes completados",
        description = "Obtiene estadísticas de viajes completados por el pasajero en un rango de fechas específico, agrupados por día, semana o mes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas de viajes obtenidas con éxito."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
        @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
        @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/trips")
    public ResponseEntity<Response<PassengerStatResponseDTO>> getTripStats(
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
        @RequestParam(defaultValue = "MONTH") GroupByEnum groupBy) {
        return new ResponseEntity<>(passengerStatsService.getTripStats(fromDate, toDate, groupBy), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener estadísticas de CO2 ahorrado",
        description = "Obtiene estadísticas de CO2 ahorrado por el pasajero en un rango de fechas específico, agrupados por día, semana o mes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas de CO2 obtenidas con éxito."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
        @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
        @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/co2")
    public ResponseEntity<Response<Co2StatResponseDTO>> getCo2Stats() {
        return new ResponseEntity<>(passengerStatsService.getCo2Stats(), HttpStatus.OK);
    }
    
}
