package com.carpool.carpool.repository.reservation;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.stateHistory.StateHistory;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Metodo que permite realizar consultas dinámicas a la base de datos para obtener datos.
 */
public class ReservationSpecification {
    public static Specification<Reservation> byFilter(Long idTrip, Long idStartCity, Long idDestinationCity, Boolean baggage, String nameState, Long idUser) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("trip").get("vehicle").get("driver").get("user").get("id"), idUser));
            if (idTrip != null && idTrip > 0) {
                predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), idTrip));
            }
            if (idStartCity != null && idStartCity > 0) {
                predicates.add(criteriaBuilder.equal(root.get("startCity").get("id"), idStartCity));
            }
            if (idDestinationCity != null && idDestinationCity > 0) {
                predicates.add(criteriaBuilder.equal(root.get("destinationCity").get("id"), idDestinationCity));
            }
            if (baggage != null) {
                predicates.add(criteriaBuilder.equal(root.get("baggage"), baggage));
            }

            if (nameState != null && !nameState.isEmpty()) {

                Root<StateHistory> shRoot = query.from(StateHistory.class);

                Predicate link = criteriaBuilder.equal(shRoot.get("reservation"), root);

                Predicate stateFilter = criteriaBuilder.equal(shRoot.get("state").get("name"), nameState);

                predicates.add(criteriaBuilder.and(link, stateFilter));

                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
