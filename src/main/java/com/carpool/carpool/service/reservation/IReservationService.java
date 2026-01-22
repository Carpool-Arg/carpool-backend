package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationResponseDTO;
import com.carpool.carpool.dto.reservation.ReservationUpdateRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.exception.ResourceNotFoundException;

public interface IReservationService {
    /**
     * Metodo para crear una solicitud de reserva
     * @return Response<Void> devolviendo el mensaje si la solicitud de reserva fue creada
     * @throws ResourceNotFoundException
     */
    Response<Void> createReservation(CreateReservationRequestDTO createReservationRequestDTO);

    Response<Double> calculateTotal(Long idTrip, Long idStartCity, Long idDestinationCity);

    /**
     * Metodo encargado de obtener las reservas realizadas a un viaje. Solamente es accesible por aquellos usuarios que poseen el rol
     * CHOFER o ADMIN.
     * @param idTrip                Id del viaje
     * @param idStartCity           Id de la ciudad origen
     * @param idDestinationCity     Id de la ciudad destino
     * @param baggage               Si requiere o no equipaje
     * @param nameState             Nombre del estado del viaje
     * @param page                  Numero de pagina
     * @param size                  Cantidad de registros por pagina
     * @return Response<ReservationResponseDTO> devolviendo el mensaje con las reservas de un viaje o sin ellas
     * @throws UnauthorizedException
     */
    Response<ReservationResponseDTO> getReservation(Long idTrip, Long idStartCity, Long idDestinationCity, Boolean baggage, String nameState, int page, int size);

    /**
     * Metodo encargado para aceptar o rechazar una reserva.
     * @param reservationUpdateRequestDTO Request que contiene la informacion necesaria para cancelar o aceptar una reserva
     * @throws ResourceNotFoundException
     * @throws ConflictException
     */
    Response<Void> updateStateReservation(ReservationUpdateRequestDTO reservationUpdateRequestDTO);

    /**
     * Metodo para finalizar una reserva de un viaje
     * @param reservation
     */
    void finishTripReservation(Reservation reservation);
}
