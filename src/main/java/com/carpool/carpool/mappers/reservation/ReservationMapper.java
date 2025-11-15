package com.carpool.carpool.mappers.reservation;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationDTO;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    /**
     * Convierte un DTO de reserva en una entidad Reservation.
     *
     * @param createReservationRequestDTO DTO recibido desde el cliente
     * @param user Usuario que realiza la reserva
     * @param trip Viaje asociado a la reserva
     * @param startCity TripStop de origen
     * @param destinationCity TripStop de destino
     * @return Reservation lista para persistir
     */
    public static Reservation convertReservationRequestDTOToReservation(
            CreateReservationRequestDTO createReservationRequestDTO,
            User user,
            Trip trip,
            TripStop startCity,
            TripStop destinationCity,
            State state
    ) {
        return Reservation.builder()
                .user(user)
                .trip(trip)
                .startCity(startCity)
                .destinationCity(destinationCity)
                .baggage(createReservationRequestDTO.isBaggage())
                .state(state)
                .build();
    }

    /**
     * Metodo encargado de convertir un objeto {@link Reservation} en {@link ReservationDTO} y almacenarlo en una lista
     * @param listReservation   Lista de reservas
     * @return Lista con objetos {@link ReservationDTO}
     */
    public static List<ReservationDTO> convertReservationToReservationDTO(List<Reservation> listReservation, Map<Long, String> urlImagesUsers){
        return listReservation.stream()
                .map(reservation -> ReservationDTO.builder()
                        .id(reservation.getId())
                        .createdAt(reservation.getCreatedAt())
                        .startCity(reservation.getStartCity().getCity().getName())
                        .destinationCity(reservation.getDestinationCity().getCity().getName())
                        .baggage(reservation.isBaggage())
                        .nameUser(reservation.getUser().getName())
                        .lastNameUser(reservation.getUser().getLastname())
                        .urlImage(urlImagesUsers.get(reservation.getUser().getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
