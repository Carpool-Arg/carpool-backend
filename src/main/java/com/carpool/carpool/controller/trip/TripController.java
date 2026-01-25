package com.carpool.carpool.controller.trip;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.trip.CurrentTripResponseDTO;
import com.carpool.carpool.dto.trip.TripArriveRequestDTO;
import com.carpool.carpool.dto.trip.TripDriverResponseDTO;
import com.carpool.carpool.dto.trip.TripPriceCalculationResponseDTO;
import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.TripSearchRequestDTO;
import com.carpool.carpool.dto.trip.TripSearchResponseDTO;
import com.carpool.carpool.dto.trip.TripStartRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.trip.ITripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Trip", description = "Operaciones relacionadas con viajes")
@RequestMapping("/trip")
@RequiredArgsConstructor
public class TripController {

    private final ITripService tripService;

        @Operation(summary = "Obtener viajes creados por un chofer")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Listado de viajes obtenidos con éxito"),
                        @ApiResponse(responseCode = "401", description = "El usuario no inició sesión", content = @Content),
        })
        @GetMapping
        public ResponseEntity<Response<TripDriverResponseDTO>> getTrips(
                        @RequestParam(defaultValue = "CREATED") List<String> tripState) {
                return new ResponseEntity<>(tripService.getTrips(tripState), HttpStatus.OK);
        }

    @Operation(
            summary = "Visualizar los detalles de un viaje específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalles del viaje obtenidos con éxito"),
            @ApiResponse(responseCode = "400", description = "ID de viaje inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver los detalles del viaje"),
            @ApiResponse(responseCode = "404", description = "El viaje no existe", content = @Content),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<TripResponseDTO>> getTripDetails(@PathVariable Long id) {
        return new ResponseEntity<>(tripService.getTripDetails(id), HttpStatus.OK);
    }

    @Operation(
                summary = "Verificar la disponibilidad de un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El viaje es posible"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    })
    @GetMapping("/check-trip-availability")
    public  Response<Void> checkTripAvailability(@RequestParam String startDateTime) {
        return tripService.checkTripAvailability(LocalDateTime.parse(startDateTime));
    }

    @Operation(
            summary = "Obtener el feed inicial de viajes"

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de viajes obtenida con éxito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver los viajes"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content),
    })
    @GetMapping("/feed")
    public ResponseEntity<Response<List<TripSearchResponseDTO>>> getInitialFeed(
            @RequestParam(name = "cityId", required = false) Long userCityId,
            @RequestParam(defaultValue = "10") int limit) {
        return new ResponseEntity<>(tripService.getInitialFeed(userCityId, limit), HttpStatus.OK);
    }

    @Operation(
        summary = "Verifica si el usuario autenticado es el conductor/creador del viaje"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado de propiedad obtenido con éxito"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "404", description = "El viaje no existe")
    })
    @GetMapping("/is-creator/{id}")
    public ResponseEntity<Response<Boolean>> isTripCreator(@PathVariable("id") Long tripId) {
        return new ResponseEntity<>(tripService.isTripCreator(tripId), HttpStatus.OK);
    }

    @Operation(
            summary = "Calculos de los procios que se obtienen con el precio del asiento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo realizado con éxito"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos (precio o asientos no son positivos)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor en el cálculo", content = @Content),
    })
    @GetMapping("/calculate-price-trip")
    public ResponseEntity<Response<TripPriceCalculationResponseDTO>> calculatePriceTrip(
            @RequestParam("seatPrice") Double publishedPrice,
            @RequestParam("availableCurrentSeats") Integer availableSeats) {
        Response<TripPriceCalculationResponseDTO> response = tripService.calculatePublishSeatPrice(publishedPrice, availableSeats);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(
            summary = "Obtener el viaje en curso del chofer logueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Viaje obtenido con éxito."),
            @ApiResponse(responseCode = "404", description = "No se pudo encontrar el viaje.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor al obtener el viaje.", content = @Content),
    })
    @GetMapping("/current-trip")
    public ResponseEntity<Response<CurrentTripResponseDTO>> getDriverCurrentTrip() {
        Response<CurrentTripResponseDTO> response = tripService.getCurrentTrip();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Buscar viajes con filtros aplicados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de viajes obtenida con éxito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para ver los viajes"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content),
    })
    @PostMapping("/search")
    public ResponseEntity<Response<List<TripSearchResponseDTO>>> searchTrips(
            @RequestBody TripSearchRequestDTO request,
            @RequestParam(defaultValue = "10") int limit) {
        return new ResponseEntity<>(tripService.searchTrips(request, limit), HttpStatus.OK);
    }

    @Operation(
            summary = "Crear y publicar un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Viaje creado y publicado con exito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para crear un viaje"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),       
    })
    @PostMapping()
    public ResponseEntity<Response<Void>> createTrip(@Valid @RequestBody TripRequestDTO tripRequestDTO){
        return new ResponseEntity<>(tripService.createTrip(tripRequestDTO), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Indicar que se llego a una parada intermedia o al destino de un viaje en curso."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Llegada registrada con exito"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "No autorizado para registrar una llegada"),
        @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
        @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),
    })
    @PostMapping("/arrive-tripstop")
    public ResponseEntity<Response<Void>> arriveTripStop(@Valid @RequestBody TripArriveRequestDTO tripArriveRequestDTO) {
        return new ResponseEntity<>(tripService.arriveTripStop(tripArriveRequestDTO), HttpStatus.CREATED);

    }

    @Operation(summary = "Iniciar un viaje programado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Viaje iniciado con éxito"),
            @ApiResponse(responseCode = "404", description = "El viaje no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto con el horario o el estado del viaje", content = @Content)
    })
    @PostMapping("/start")
    public ResponseEntity<Response<CurrentTripResponseDTO>> startTrip(@Valid @RequestBody TripStartRequestDTO tripStartRequestDTO) {
        Response<CurrentTripResponseDTO> response = tripService.startTrip(tripStartRequestDTO.getTripId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
