package com.carpool.carpool.controller.statistics.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.dto.statistics.driver.DriverStatResponseDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.statistics.admin.trips.IAdminTripsStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Admin Stats", description = "Estadisticas para el administrador")
@RequestMapping("/stats/admin")
@RequiredArgsConstructor
public class AdminStatsController {
  private final IAdminTripsStatsService adminTripsStatsService;

  @Operation(
      summary = "Obtener el top 3 de localidades mas eleigdas como origen",
      description = "Obtiene estadísticas de las 3 localidades mas elegidas por los pasajeros como origen."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas de localidades obtenidas con éxito."),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
      @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
      @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
  })
  @GetMapping("/top/origin")
  public ResponseEntity<Response<TopCityStatResponseDTO>> getOriginCitiesTopStats() {
    return new ResponseEntity<>(adminTripsStatsService.getTopOriginCitiesStat(), HttpStatus.OK);
  }

  @Operation(
      summary = "Obtener el top 3 de localidades mas eleigdas como destino",
      description = "Obtiene estadísticas de las 3 localidades mas elegidas por los pasajeros como destino."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas de localidades obtenidas con éxito."),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
      @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
      @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
  })
  @GetMapping("/top/destination")
  public ResponseEntity<Response<TopCityStatResponseDTO>> getDestinationCitiesTopStats() {
    return new ResponseEntity<>(adminTripsStatsService.getTopDestinationCitiesStat(), HttpStatus.OK);
  }

  @Operation(
      summary = "Obtener estadísticas de porcentaje de choferes en un periodo",
      description = "Obtiene estadísticas de porcetaje de choferes  en un rango de fechas específico."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estadísticas de porcentaje de choferes obtenidas con éxito."),
      @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
      @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
      @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
  })
  @GetMapping("/drivers-percentage")
  public ResponseEntity<Response<DriverPercentageStatResponseDTO>> getTripsStats(
      @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {
    return new ResponseEntity<>(adminTripsStatsService.getDriverPercentageByPeriod(fromDate, toDate), HttpStatus.OK);
  }
}
