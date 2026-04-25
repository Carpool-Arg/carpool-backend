package com.carpool.carpool.service.state;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationStateHistoryFinder implements StateHistoryFinder<Reservation> {

  private final StateHistoryRepository stateHistoryRepository;

  @Override
  public ScopeEnum getScope() {
      return ScopeEnum.RESERVATION;
  }

  @Override
  public Optional<StateHistory> findCurrent(Reservation reservation) {
      return stateHistoryRepository
              .findByReservationIdAndFinishDateTimeIsNull(reservation.getId());
  }

  @Override
  public StateHistory buildNew(State state, Reservation reservation) {
      return StateHistory.builder()
              .state(state)
              .reservation(reservation)
              .build();
  }
}

