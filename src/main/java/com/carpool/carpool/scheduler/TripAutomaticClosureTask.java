package com.carpool.carpool.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.enums.reservation.ReservationStateEnum;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.repository.reservation.ReservationRepository;
import com.carpool.carpool.repository.state.StateRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.service.notification.INotificationService;
import com.carpool.carpool.service.reservation.IReservationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripAutomaticClosureTask {
    private final TripRepository tripRepository;
    private final StateRepository stateRepository; 
    private final StateHistoryRepository stateHistoryRepository;
    private final INotificationService notificationService;
    private final IReservationService reservationService; 
    private final ReservationRepository reservationRepository; 

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void executeClosure() {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(30);
        List<Trip> tripsToClose = tripRepository.findTripToClose(threshold);

        if (tripsToClose.isEmpty()) return;

        State closedState = stateRepository.findByNameAndScope("CLOSED", ScopeEnum.TRIP)
                .orElseThrow(() -> new IllegalStateException("Estado CLOSED no encontrado"));

        for (Trip trip : tripsToClose) {
            try {
                LocalDateTime now = LocalDateTime.now();
                stateHistoryRepository.findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(trip)
                        .ifPresent(sh -> {
                            sh.setFinishDateTime(now);
                            stateHistoryRepository.save(sh); 
                        });

                StateHistory newStateHistory = StateHistory.builder()
                        .trip(trip)
                        .state(closedState)
                        .startDateTime(now)
                        .build();
                
                stateHistoryRepository.save(newStateHistory);
                
                List<Reservation> pendingReservations = reservationRepository
                    .findByTripIdAndStateName(trip.getId(),ReservationStateEnum.PENDING.name());  
                
                for(Reservation r : pendingReservations){
                    try{
                        reservationService.cancelReservation(r.getId());
                        notificationService.send(
                            r.getUser(), 
                            NotificationEventEnum.RESERVATION_CANCELLED_BY_SYSTEM,
                            r);
                        log.info("Reserva PENDING {} cancelada por cierre automático del viaje {}", 
                                r.getId(), trip.getId());
                    }catch (Exception e) {
                        log.error("Error al cancelar reserva {} del viaje {}: {}", 
                                r.getId(), trip.getId(), e.getMessage());
                    }
                }
                try {
                    notificationService.send(
                        trip.getVehicle().getDriver().getUser(), 
                        NotificationEventEnum.TRIP_CLOSED_AUTOMATICALLY, 
                        trip
                    );
                } catch (Exception e) {
                    log.error("Error al enviar notificación para viaje {}: {}", trip.getId(), e.getMessage());
                }

            } catch (Exception e) {
                log.error("Error grave procesando viaje {}: {}", trip.getId(), e.getMessage());
            }
        }
    }
}