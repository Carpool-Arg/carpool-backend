package com.carpool.carpool.controller.statistics.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.statistics.admin.AdminStatSimpleDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminCo2StatDTO;
import com.carpool.carpool.dto.statistics.admin.general.AdminTripMonthlyStatDTO;
import com.carpool.carpool.dto.statistics.admin.trips.DriverPercentageStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TakenSeatsStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.trips.TopCityStatResponseDTO;
import com.carpool.carpool.dto.statistics.admin.user.VerifiedUserDTO;
import com.carpool.carpool.enums.statistics.GroupByEnum;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.statistics.admin.general.IAdminStatsService;
import com.carpool.carpool.service.statistics.admin.trips.IAdminTripsStatsService;
import com.carpool.carpool.service.statistics.admin.user.IAdminUserStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Admin Stats", description = "Estadisticas para el administrador")
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
    private final IAdminTripsStatsService adminTripsStatsService;
    private final IAdminStatsService adminStatsService; 
    private final IAdminUserStatsService adminUserStatsService; 


    @Operation(
    summary = "Ganancias de la aplicación",
    description = "Obtiene las ganancias de la aplicación " )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/earnings")
    public ResponseEntity<Response<AdminStatSimpleDTO>> getAppEarningsStats(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {
        return new ResponseEntity<>(adminStatsService.getAppEarningsStats(fromDate, toDate), HttpStatus.OK);
    }

    @Operation(
    summary = "Monto transaccionado en la aplicación",
    description = "Calcula el flujo total de dinero movido entre pasajeros y choferes dentro del sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/transacted")
    public ResponseEntity<Response<AdminStatSimpleDTO>> getTotalTransactedStats(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {
        return new ResponseEntity<>(adminStatsService.getTotalTransactedStats(fromDate, toDate), HttpStatus.OK);
    }

    @Operation(
    summary = "Cantidad de viajes finalizados",
    description = "Retorna el número total de viajes que han completado su ciclo exitosamente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/trips")
    public ResponseEntity<Response<AdminStatSimpleDTO>> getFinishedTripsStats(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {
        return new ResponseEntity<>(adminStatsService.getFinishedTripsStats(fromDate, toDate), HttpStatus.OK);
    }

    @Operation(
    summary = "CO2 ahorrado en la plataforma",
    description = "Calcula la cantidad total de CO2 evitado mediante el uso de viajes compartidos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/co2")
    public ResponseEntity<Response<AdminCo2StatDTO>> getCo2Stats() {
        return new ResponseEntity<>(adminStatsService.getCo2Stats(), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener el top de localidades mas eleigdas como origen",
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
    public ResponseEntity<Response<TopCityStatResponseDTO>> getOriginCitiesTopStats(
    @RequestParam(required = false, defaultValue = "3") int limit
    ) {
    return new ResponseEntity<>(adminTripsStatsService.getTopOriginCitiesStat(limit), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener el top de localidades mas eleigdas como destino",
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
    public ResponseEntity<Response<TopCityStatResponseDTO>> getDestinationCitiesTopStats(
    @RequestParam(required = false, defaultValue = "3") int limit
    ) {
    return new ResponseEntity<>(adminTripsStatsService.getTopDestinationCitiesStat(limit), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener estadísticas de porcentaje de asientos ocupados en un periodo",
        description = "Obtiene estadísticas de porcentaje de asientos ocupados respecto a publicados en un rango de fechas específico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas de porcentaje de asientos obtenidas con éxito."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
        @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
        @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/seats-percentage")
    public ResponseEntity<Response<TakenSeatsStatResponseDTO>> getSeatsStats(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {
    return new ResponseEntity<>(adminTripsStatsService.getTakenSeatsStat(fromDate, toDate), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener estadísticas de porcentaje de choferes",
        description = "Obtiene estadísticas de porcetaje de choferes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas de porcentaje de choferes obtenidas con éxito."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida, por ejemplo, si las fechas no son válidas o el rango es incorrecto."),
        @ApiResponse(responseCode = "401", description = "No autorizado, el usuario no ha iniciado sesión o no tiene permisos adecuados."),
        @ApiResponse(responseCode = "403", description = "Prohibido, el usuario no tiene acceso a estas estadísticas."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/drivers-percentage")
    public ResponseEntity<Response<DriverPercentageStatResponseDTO>> getTripsStats() {
    return new ResponseEntity<>(adminUserStatsService.getDriverPercentage(), HttpStatus.OK);
    }

    @Operation(
        summary = "Nuevos usuarios por período",
        description = "Obtiene la cantidad de nuevos usuarios registrados agrupados por día, semana, mes o año dentro de un rango de fechas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/users/new")
    public ResponseEntity<Response<AdminStatSimpleDTO>> getNewUsersStats(
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
        @RequestParam(defaultValue = "MONTH") GroupByEnum groupBy) {
        return new ResponseEntity<>(adminUserStatsService.getNewUsersStats(fromDate, toDate, groupBy), HttpStatus.OK);
    }

    @Operation(
        summary = "Total de usuarios verificados",
        description = "Obtiene la cantidad total de usuarios activos y verificados registrados en la plataforma.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total de usuarios verificados obtenido con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/users/verified")
    public ResponseEntity<Response<VerifiedUserDTO>> getVerifiedUsersStats() {
        return new ResponseEntity<>(adminUserStatsService.getVerifiedUsersStats(), HttpStatus.OK);
    }

    @Operation(
        summary = "Viajes publicados en el mes actual vs mes anterior",
        description = "Obtiene los viajes publicados dentro del mes y la diferencia de viajes de este mes contra el anteriror." )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tenés permisos para acceder a este recurso")
    })
    @GetMapping("/trips/monthly")
    public ResponseEntity<Response<AdminTripMonthlyStatDTO>> getMonthlyPublishedTripsStats() {
        return new ResponseEntity<>(adminStatsService.getMonthlyPublishedTripsStats(), HttpStatus.OK);
    }
}
