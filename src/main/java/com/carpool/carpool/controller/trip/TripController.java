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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Trip", description = "Operaciones relacionadas con viajes")
@RequestMapping("/trip")
@RequiredArgsConstructor
public class TripController {

    private final ITripService tripService;

    // TODO: ver mensajes de apiResponse
    @Operation(
            summary = "Visualizar los detalles de un viaje específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalles del viaje obtenidos con éxito"),
            @ApiResponse(responseCode = "404", description = "El viaje no existe", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<TripResponseDTO>> getTripDetails(@PathVariable Long id) {
        return new ResponseEntity<>(tripService.getTripDetails(id), HttpStatus.OK);
    } 

    @Operation(
            summary = "Crear y publicar un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Viaje creado y publicado con exito"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),
            
    })
    @PostMapping()
    public ResponseEntity<Response<Void>> createTrip(@Valid @RequestBody TripRequestDTO tripRequestDTO){
        return new ResponseEntity<>(tripService.createTrip(tripRequestDTO), HttpStatus.CREATED);
    }
}
