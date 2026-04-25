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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
        transitionInternal(entity, scope, List.of(expectedCurrentState), newStateName);
    }

    @Transactional
    public <T> void transition(
            T entity,
            ScopeEnum scope,
            List<String> expectedCurrentStates,
            String newStateName
    ) {
        transitionInternal(entity, scope, expectedCurrentStates, newStateName);
    }

    private <T> void transitionInternal(
            T entity,
            ScopeEnum scope,
            List<String> expectedCurrentStates,
            String newStateName
    ) {

        @SuppressWarnings("unchecked")
        StateHistoryFinder<T> finder = finders.stream()
                .filter(f -> f.getScope() == scope)
                .map(f -> (StateHistoryFinder<T>) f)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No existe handler para el scope " + scope));

        final State newState = stateRepository
                .findByNameAndScope(newStateName, scope)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el estado " + newStateName));

        final StateHistory current = finder.findCurrent(entity)
                .orElseThrow(() -> new ConflictException("La entidad no tiene un estado actual"));

        final var currentState = current.getState();

        if (currentState.isFinish()) {
            throw new ConflictException("El estado actual es un estado final");
        }

        if (!expectedCurrentStates.contains(currentState.getName())) {
            throw new ConflictException(
                    "El estado actual no permite esta transición. Estados permitidos: " + expectedCurrentStates
            );
        }

        current.setFinishDateTime(LocalDateTime.now());

        StateHistory next = finder.buildNew(newState, entity);

        stateHistoryRepository.save(current);
        stateHistoryRepository.save(next);
    }
}
