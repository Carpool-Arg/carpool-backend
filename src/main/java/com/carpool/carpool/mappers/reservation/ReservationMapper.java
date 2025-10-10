package com.carpool.carpool.mappers.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    /**
     * Convierte un DTO de reserva en una entidad Reservation.
     *
     * @param reservationRequestDTO DTO recibido desde el cliente
     * @param user Usuario que realiza la reserva
     * @param trip Viaje asociado a la reserva
     * @param startCity TripStop de origen
     * @param destinationCity TripStop de destino
     * @return Reservation lista para persistir
     */
    public static Reservation convertReservationRequestDTOToReservation(
            ReservationRequestDTO reservationRequestDTO,
            User user,
            Trip trip,
            TripStop startCity,
            TripStop destinationCity
    ) {
        return Reservation.builder()
                .user(user)
                .trip(trip)
                .startCity(startCity)
                .destinationCity(destinationCity)
                .baggage(reservationRequestDTO.isBaggage())
                .build();
    }
}
