package com.carpool.carpool.controller.reservation;

import com.carpool.carpool.dto.reservation.*;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.reservation.IReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Tag(name = "Reservation", description = "Operaciones relacionadas con la reserva de viajes")
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @Operation(summary = "Obtener la reservas de un viaje")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas con éxito", content = @Content),
    })
    @GetMapping("/filter")
    public ResponseEntity<Response<ReservationResponseDTO>> getReservations(
            @RequestParam(required = false) @Positive(message = "El id del viaje debe ser mayor a 0") Long idTrip,
            @RequestParam(required = false) Long idStartCity, @RequestParam(required = false) Long idDestinationCity,
            @RequestParam(required = false) Boolean baggage, @RequestParam(required = false) String nameState,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(30) @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(reservationService.getReservation(idTrip, idStartCity, idDestinationCity, baggage,
                nameState, page, size), HttpStatus.OK);
    }

    @Operation(summary = "Obtener las reservas de un usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas con éxito", content = @Content),
    })
    @GetMapping("/me")
    public ResponseEntity<Response<ReservationResponseDTO>> getMyReservations(
            @RequestParam(required = false, defaultValue = "PENDING") String state,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false, defaultValue = "0") int skip,
            @RequestParam(required = false, defaultValue = "DATE_DESC") String orderBy) {
        return new ResponseEntity<>(reservationService.getMyReservation(state, fromDate,  toDate, skip, orderBy), HttpStatus.OK);
    }

    @Operation(summary = "Calcular total a pagar para una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado con éxito", content = @Content)
    })
    @GetMapping("/calculate-total")
    public ResponseEntity<Response<Double>> calculateTotal(
            @RequestParam(required = true) @Positive(message = "El id del viaje debe ser mayor que 0") Long idTrip,
            @RequestParam(required = false) Long idStartCity,
            @RequestParam(required = false) Long idDestinationCity) {
        return new ResponseEntity<>(reservationService.calculateTotal(idTrip, idStartCity, idDestinationCity),
                HttpStatus.OK);
    }

    @Operation(summary = "Solicitar una reserva de un viaje")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud de reserva creada con exito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para solicitar una reserva de viaje"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),
    })
    @PostMapping
    public ResponseEntity<Response<Void>> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return new ResponseEntity<>(reservationService.createReservation(createReservationRequestDTO),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Pagar la reserva con estado UNPAID del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva pagada con exito"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para pagar una reserva de viaje"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content),
    })
    @PostMapping("/payment")
    public ResponseEntity<Response<Void>> payReservation() {
        return new ResponseEntity<>(reservationService.payReservation(), HttpStatus.CREATED);
    }

    @Operation(summary = "Aceptar-Rechazar una reserva de un viaje")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas aceptada-cancelada con éxito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Cupo de asientos ocupados", content = @Content),
    })
    @PutMapping
    public ResponseEntity<Response<Void>> updateReservation(
            @Valid @RequestBody ReservationUpdateRequestDTO reservationUpdateRequestDTO) {
        return new ResponseEntity<>(reservationService.updateStateReservation(reservationUpdateRequestDTO),
                HttpStatus.OK);
    }

    @Operation(summary = "Eliminar a un pasajero de un viaje")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pasajero eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ocurrio un error al eliminar al pasajero.", content = @Content),
    })
    @PutMapping("/delete-trip-passenger")
    public ResponseEntity<Response<Void>> deleteTripPassenger(
            @Valid @RequestBody DeleteTripPassengerRequestDTO deleteTripPassengerRequestDTO) {
        return new ResponseEntity<>(reservationService.deleteTripPassenger(deleteTripPassengerRequestDTO),
                HttpStatus.OK);
    }

    @Operation(summary = "Cancelar la reserva de un viaje viaje")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ocurrio un error al cancelar la reserva.", content = @Content),
    })
    @PutMapping("/cancel")
    public ResponseEntity<Response<Void>> cancelReservationByPassenger(
            @Valid @RequestBody CancelReservationByPassengerRequestDTO cancelReservationByPassengerRequestDTO) {
        return new ResponseEntity<>(reservationService.cancelReservationByPassenger(cancelReservationByPassengerRequestDTO.getReservationId()),
                HttpStatus.OK);
    }
}
