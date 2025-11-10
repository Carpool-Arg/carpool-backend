package com.carpool.carpool.controller.reservation;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationResponseDTO;
import com.carpool.carpool.dto.reservation.ReservationUpdateRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.reservation.IReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="Reservation", description = "Operaciones relacionadas con la reserva de viajes")
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @Operation(
            summary = "Solicitar una reserva de un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud de reserva creada con exito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para solicitar una reserva de viaje"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),
    })
    @PostMapping
    public ResponseEntity<Response<Void>> createReservation(@Valid @RequestBody CreateReservationRequestDTO createReservationRequestDTO){
        return new ResponseEntity<>(reservationService.createReservation(createReservationRequestDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Obtener la reservas de un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas con éxito", content = @Content),
    })
    @PostMapping("/filter")
    public ResponseEntity<Response<ReservationResponseDTO>> getReservations(@Valid @RequestBody ReservationRequestDTO reservationRequestDTO){
        return new ResponseEntity<>(reservationService.getReservation(reservationRequestDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Aceptar-Rechazar una reserva de un viaje"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas aceptada-cancelada con éxito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Cupo de asientos ocupados", content = @Content),
    })
    @PostMapping("/update-reservation")
    public ResponseEntity<Response<Void>> updateReservation(@Valid @RequestBody ReservationUpdateRequestDTO reservationUpdateRequestDTO){
        return new ResponseEntity<>(reservationService.updateStateReservation(reservationUpdateRequestDTO), HttpStatus.OK);
    }
}
