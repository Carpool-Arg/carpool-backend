package com.carpool.carpool.mappers.reservation;

import com.carpool.carpool.dto.reservation.CreateReservationRequestDTO;
import com.carpool.carpool.dto.reservation.ReservationDTO;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.service.media.IMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    private final IMediaService mediaService;

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
    public Reservation convertReservationRequestDTOToReservation(
            CreateReservationRequestDTO createReservationRequestDTO,
            User user,
            Trip trip,
            TripStop startCity,
            TripStop destinationCity,
            double total
    ) {
        return Reservation.builder()
                .user(user)
                .trip(trip)
                .startCity(startCity)
                .destinationCity(destinationCity)
                .baggage(createReservationRequestDTO.isBaggage())
                .total(total)
                .build();
    }

    /**
     * Metodo encargado de convertir un objeto {@link Reservation} en {@link ReservationDTO} y almacenarlo en una lista
     * @param listReservation   Lista de reservas
     * @return Lista con objetos {@link ReservationDTO}
     */
    public static List<ReservationDTO> convertReservationToReservationDTO(Page<Reservation> listReservation, Map<Long, String> urlImagesUsers, Map<Long, StateHistory> stateHistoryMap){
        return listReservation.stream()
                .map(reservation -> ReservationDTO.builder()
                        .id(reservation.getId())
                        .tripStartDatetime(reservation.getTrip().getStartTripDateTime())
                        .createdAt(reservation.getCreatedAt())
                        .startCity(reservation.getStartCity().getCity().getName())
                        .destinationCity(reservation.getDestinationCity().getCity().getName())
                        .baggage(reservation.isBaggage())
                        .nameUser(reservation.getUser().getName())
                        .lastNameUser(reservation.getUser().getLastname())
                        .urlImage(urlImagesUsers.get(reservation.getUser().getId()))
                        .state(Optional.ofNullable(stateHistoryMap.get(reservation.getId()))
                                .map(sh -> sh.getState().getName())
                                .orElse(null))
                        .ratingUser(reservation.getUser().getRating())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Metodo encargado de convertir un objeto {@link Reservation} en {@link ReservationDTO} y almacenarlo en una lista
     * @param listReservation   Lista de reservas
     * @return Lista con objetos {@link ReservationDTO}
     */
    public static List<ReservationDTO> convertMyReservationsToReservationDTO(Page<Reservation> listReservation, Map<Long, String> urlImagesDrivers, Map<Long, StateHistory> stateHistoryMap){
        return listReservation.stream()
                .map(reservation -> ReservationDTO.builder()
                        .id(reservation.getId())
                        .tripStartDatetime(reservation.getTrip().getStartTripDateTime())
                        .createdAt(reservation.getCreatedAt())
                        .startCity(reservation.getStartCity().getCity().getName())
                        .destinationCity(reservation.getDestinationCity().getCity().getName())
                        .baggage(reservation.isBaggage())
                        .nameUser(reservation.getTrip().getVehicle().getDriver().getUser().getName())
                        .lastNameUser(reservation.getTrip().getVehicle().getDriver().getUser().getLastname())
                        .urlImage(urlImagesDrivers.get(reservation.getTrip().getVehicle().getDriver().getUser().getId()))
                        .state(Optional.ofNullable(stateHistoryMap.get(reservation.getId()))
                                .map(sh -> sh.getState().getName())
                                .orElse(null))
                        .ratingUser(reservation.getTrip().getVehicle().getDriver().getUser().getRating())
                        .build())
                .collect(Collectors.toList());
    }
}
