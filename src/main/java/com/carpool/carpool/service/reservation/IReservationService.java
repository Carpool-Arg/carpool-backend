package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.DeleteTripPassengerRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationResponseDTO;
import com.carpool.carpool.dto.reservation.ReservationUpdateRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.UnauthorizedException;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.exception.ResourceNotFoundException;

import java.time.LocalDate;

public interface IReservationService {
    /**
     * Metodo para crear una solicitud de reserva
     * 
     * @return Response<Void> devolviendo el mensaje si la solicitud de reserva fue
     *         creada
     * @throws ResourceNotFoundException
     */
    Response<Void> createReservation(CreateReservationRequestDTO createReservationRequestDTO);

    Response<Double> calculateTotal(Long idTrip, Long idStartCity, Long idDestinationCity);

    /**
     * Metodo encargado de obtener las reservas realizadas a un viaje. Solamente es
     * accesible por aquellos usuarios que poseen el rol
     * CHOFER o ADMIN.
     * 
     * @param idTrip            Id del viaje
     * @param idStartCity       Id de la ciudad origen
     * @param idDestinationCity Id de la ciudad destino
     * @param baggage           Si requiere o no equipaje
     * @param nameState         Nombre del estado del viaje
     * @param page              Numero de pagina
     * @param size              Cantidad de registros por pagina
     * @return Response<ReservationResponseDTO> devolviendo el mensaje con las
     *         reservas de un viaje o sin ellas
     * @throws UnauthorizedException
     */
    Response<ReservationResponseDTO> getReservation(Long idTrip, Long idStartCity, Long idDestinationCity,
            Boolean baggage, String nameState, int page, int size);

    /**
     * Metodo encargado de obtener las reservas realizadas por el usuario.
     *
     * @param state
     * @param skip
     * @param orderBy
     * @return Response<ReservationResponseDTO> devolviendo el mensaje con las
     *         reservas de un viaje o sin ellas
     * @throws UnauthorizedException
     */
    Response<ReservationResponseDTO> getMyReservation(String state, LocalDate dateFrom, LocalDate dateTo, int skip, String orderBy);

    /**
     * Metodo encargado para aceptar o rechazar una reserva.
     * 
     * @param reservationUpdateRequestDTO Request que contiene la informacion
     *                                    necesaria para cancelar o aceptar una
     *                                    reserva
     * @throws ResourceNotFoundException
     * @throws ConflictException
     */
    Response<Void> updateStateReservation(ReservationUpdateRequestDTO reservationUpdateRequestDTO);

    /**
     * Método para que cambia el estado de las reservas a IN_PROGRESS que señala el
     * inicio de un viaje.
     * 
     * @param reservationId id de la reserva del usuario para un viaje.
     */
    void startTripReservation(Long reservationId);

    /**
     * Método para que el usuario en sesión realice la cancelación de una reserva
     *
     * @param reservationId id de la reserva del usuario para un viaje.
     */
    Response<Void> cancelReservationByPassenger(Long reservationId);

    /**
     * Método para que cambia el estado de las reservas a CANCELLED que señala la
     * cancelación de las mismas (por motivos propios o cancelar un viaje)
     * 
     * @param reservationId id de la reserva del usuario para un viaje.
     */
    void cancelReservation(Long reservationId);

    /**
     * Método que cancela la reserva de un viaje, en caso de que el viaje no salga y
     * se de, de baja por motivos de inpuntualidad.
     * 
     * @param reservationId
     * @param reason
     */
    void cancelBySystem(Long reservationId);

    /**
     * Procesa el pago de una reserva UNPAID.
     * <p>
     * Valida que la reserva exista, que pertenezca al usuario autenticado
     * y que se encuentre en un estado pendiente de pago.
     * En caso de éxito, la reserva es marcada como completada..
     * </p>
     * 
     * @throws ResourceNotFoundException
     * @throws ConflictException
     */
    Response<Void> payReservation();

    /**
     * Metodo para finalizar una reserva de un viaje
     * 
     * @param reservation
     */
    void finishTripReservation(Reservation reservation);

    /**
     * Metodo para dar de baja a un pasajero de un viaje
     * Valida que la reserva exista, que este en estado aceptada, horarios, entre
     * otros
     * 
     * @param request que contiene el motivo(opcionalmente) y el id de la reserva
     *                del pasajero que sera eliminado del viaje
     * @return
     */
    Response<Void> deleteTripPassenger(DeleteTripPassengerRequestDTO request);
}
