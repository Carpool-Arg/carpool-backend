package com.carpool.carpool.service.state;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.repository.state.StateRepository;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StateTransitionService {

    private final StateRepository stateRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final List<StateHistoryFinder<?>> finders;

    @Transactional
    public <T> void transition(
            T entity,
            ScopeEnum scope,
            String expectedCurrentState,
            String newStateName
    ) {
      @SuppressWarnings("unchecked")
      StateHistoryFinder<T> finder = finders.stream()
              .filter(f -> f.getScope() == scope)
              .map(f -> (StateHistoryFinder<T>) f)
              .findFirst()
              .orElseThrow(() ->
                      new IllegalStateException("No existe handler para el scope " + scope));

      State newState = stateRepository
              .findByNameAndScope(newStateName, scope)
              .orElseThrow(() ->
                      new ResourceNotFoundException("No se encontró el estado " + newStateName));

      StateHistory current = finder.findCurrent(entity)
              .orElseThrow(() ->
                      new ConflictException("La entidad no tiene un estado actual"));

      if (!current.getState().getName().equals(expectedCurrentState)) {
          throw new ConflictException(
                  "El estado actual no es " + expectedCurrentState
          );
      }

      current.setFinishDateTime(LocalDateTime.now());

      StateHistory next = finder.buildNew(newState, entity);

      stateHistoryRepository.save(current);
      stateHistoryRepository.save(next);
    }
}
