package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationImplementation implements  IReservationService{
    private final TripRepository tripRepository;
    private final StateHistoryRepository stateHistoryRepository;
    /*
     * VALIDACIONES:
     * Validaciones de viaje:
     *      -que exista: LISTO
     *      -que no esté lleno:
     *      -que no esté cerrado: LISTO
     *      -que no esté cancelado: LISTO
     *      -que no esté finalizado: LISTO
     * El pasajero no puede enviar mas de una solicitud para el mismo viaje
     * La localidad origen y destino no pueden ser iguales
     * La localidad origen y destino deben existir en la tabla TripStop, en base al trip que se envia por la request
     * Verificar que la localidad origen y destino que se pasan, respeten el orden establecido en TripStop
     * */

    @Override
    public Response<Void> createReservation(ReservationRequestDTO reservationRequestDTO) {
        Trip trip = tripRepository.findById(reservationRequestDTO.getTrip())
                .orElseThrow(()->new ResourceNotFoundException("El viaje no existe"));

        tripValidations(trip);
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
        Optional<StateHistory> currentStateOptional = stateHistoryRepository.findByTripAndFinishDatetimeIsNull(trip);

        //2. Si no se encuentra un estado actual, obtener el ultimo estado con fecha fin
        StateHistory stateHistory = currentStateOptional
                .orElseGet(() -> stateHistoryRepository
                        .findTopByTripAndFinishDatetimeIsNotNullOrderByFinishDatetimeDesc(trip)
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontró un estado válido para el viaje")));

        State currentState = stateHistory.getState();

        // 3. validar estados
        if (!currentState.getName().equals("CREATED")){
            throw new ConflictException("No es posible reservar el viaje, debido a su estado actual.");
        }
    }


}

