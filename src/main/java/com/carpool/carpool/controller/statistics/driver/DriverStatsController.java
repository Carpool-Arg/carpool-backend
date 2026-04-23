package com.carpool.carpool.controller.statistics.driver;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.dto.statistics.passenger.PassengerStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.statistics.driver.DriverStatsImplementation;
import com.carpool.carpool.service.statistics.driver.IDriverStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Driver Stats", description = "Estadisticas del chofer")
@RequestMapping("/driver/stats")
@RequiredArgsConstructor
public class DriverStatsController {
  private final IDriverStatsService driverStatsService;
  @Operation(
      summary = "Obtener estadísticas de kilómetros recorridos",
      description = "Obtiene estadísticas de kilómetros recorridos por el chofer en un rango de fechas específico, agrupados por día, semana o mes."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas de kilómetros obtenidas con éxito."),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
      @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
      @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
  })
  @GetMapping("/km")
  public ResponseEntity<Response<DriverStatResponseDTO>> getKmStats(
      @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
      @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
      @RequestParam(defaultValue = "MONTH") GroupByEnum groupBy) {
    return new ResponseEntity<>(driverStatsService.getKmStats(fromDate, toDate, groupBy), HttpStatus.OK);
  }

  @Operation(
      summary = "Obtener estadísticas de ganancias del chofer",
      description = "Obtiene estadísticas de ganancias del chofer en un rango de fechas específico, agrupados por día, semana o mes."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas de ganancias obtenidas con éxito."),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
      @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
      @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
  })
  @GetMapping("/earnings")
  public ResponseEntity<Response<DriverStatResponseDTO>> getEarningStats(
      @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
      @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
      @RequestParam(defaultValue = "MONTH") GroupByEnum groupBy) {
    return new ResponseEntity<>(driverStatsService.getEarningStats(fromDate, toDate, groupBy), HttpStatus.OK);
  }

}
