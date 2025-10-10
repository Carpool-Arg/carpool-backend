package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.trip.stop.TripStopRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationImplementation implements  IReservationService{
    private final TripRepository tripRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TripStopRepository  tripStopRepository;

    /*

     * */
    @Override
    public Response<Void> createReservation(ReservationRequestDTO reservationRequestDTO) {
        Trip trip = tripRepository.findById(reservationRequestDTO.getTrip())
                .orElseThrow(()->new ResourceNotFoundException("El viaje no existe"));

        tripValidations(trip);

        User userAuth = this.getAuthenticatedActiveUser();

        Optional<Reservation> existingReservation = reservationRepository.findByUserId(userAuth.getId());

        if (existingReservation.isPresent()) {
            throw new ConflictException("Ya tenés una reserva asociada, no se permiten múltiples solicitudes.");
        }

        cityValidations(reservationRequestDTO.getStartCity(), reservationRequestDTO.getDestinationCity(), trip);


        return null;
    }

    /**
     * Validaciones relacionadas al viaje. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - que no esté en curso
     * - Que no esté cerrado
     * - Que no esté cancelado
     * - Que no esté finalizado
     * @param Trip trip viaje que se quiere reservar
     */
    private void tripValidations(Trip trip){
        // 1. obtener el estado actual (sin fecha fin)
        Optional<StateHistory> currentStateOptional = stateHistoryRepository.findByTripAndFinishDateTimeIsNull(trip);

        //2. Si no se encuentra un estado actual, obtener el ultimo estado con fecha fin
        StateHistory stateHistory = currentStateOptional
                .orElseGet(() -> stateHistoryRepository
                        .findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(trip)
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontró un estado válido para el viaje")));

        State currentState = stateHistory.getState();

        // 3. validar estados
        if (!currentState.getName().equals("CREATED")){
            throw new ConflictException("No es posible reservar el viaje, debido a su estado actual.");
        }
    }

    /**
     * Validaciones relacionadas a las ciudades. Comprobamos lo siguiente:
     * - Que el viaje NO esté lleno
     * - La localidad origen y destino no pueden ser iguales. LISTO
     * - La localidad origen y destino deben existir en la tabla TripStop, en base al trip que se envia por la request
     * - Que la localidad origen y destino que se pasan, respeten el orden establecido en TripStop
     * @param Long startCity ciduad origen
     * @param Long destinationCity ciduad destino
     * @param Trip trip viaje para el cual se solicita la reserva
     * @throws ConflictException si las ciudades son iguales, si no existen en tripStop o no respetan el orden.
     */
    private void cityValidations(Long startCity, Long destinationCity, Trip trip){
        if (Objects.equals(startCity, destinationCity)){
            throw new ConflictException("La ciudad origen y destino no pueden ser iguales");
        }

        TripStop stopStartCity = tripStopRepository.findByTripIdAndCityId(trip.getId(), startCity)
                .orElseThrow(()->new ConflictException("La ciudad origen ingresada no pertenece al viaje"));

        TripStop stopDestinationCity =  tripStopRepository.findByTripIdAndCityId(trip.getId(), destinationCity)
                .orElseThrow(()->new ConflictException("La ciudad destino ingresada no pertenece al viaje"));

        if (stopStartCity.getStopOrder() > stopDestinationCity.getStopOrder()){
            throw new ConflictException("El orden de las ciudades seleccionadas no es válido para este viaje.");
        }
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     * Si no hay un usuario autenticado, lanza una excepción.
     * @return User
     * @throws ResourceNotFoundException si no se encuentra un usuario autenticado.
     */
    public User getAuthenticatedActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }
}

