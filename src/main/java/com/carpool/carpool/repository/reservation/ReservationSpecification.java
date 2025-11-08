package com.carpool.carpool.repository.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.reservation.Reservation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Metodo que permite realizar consultas dinámicas a la base de datos para obtener datos.
 */
public class ReservationSpecification {
    public static Specification<Reservation> byFilter(ReservationRequestDTO filter, Long idUser) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("trip").get("vehicle").get("driver").get("user").get("id"), idUser));
            if (filter.getIdTrip() != null && filter.getIdTrip() > 0) {
                predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), filter.getIdTrip()));
            }
            if (filter.getIdStartCity() != null && filter.getIdStartCity() > 0) {
                predicates.add(criteriaBuilder.equal(root.get("startCity").get("id"), filter.getIdStartCity()));
            }
            if (filter.getIdDestinationCity() != null && filter.getIdDestinationCity() > 0) {
                predicates.add(criteriaBuilder.equal(root.get("destinationCity").get("id"), filter.getIdDestinationCity()));
            }
            if (filter.getBaggage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("baggage"), filter.getBaggage()));
            }
            if(filter.getNameState() != null && !filter.getNameState().isBlank()){
                predicates.add(criteriaBuilder.equal(root.get("state").get("name"), filter.getNameState().toUpperCase()));
                predicates.add(criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("state").get("name"), filter.getNameState().toUpperCase()),
                        criteriaBuilder.equal(root.get("state").get("scope"), ScopeEnum.RESERVATION.name())
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
