package com.carpool.carpool.controller.trip;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.trip.ITripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Tag(name = "Trip", description = "Operaciones relacionadas con viajes")
@RequestMapping("/trip")
@RequiredArgsConstructor
public class TripController {

    private final ITripService tripService;

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
            summary = "Crear y publicar un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Viaje creado y publicado con exito"),
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
}
