package com.carpool.carpool.service.trip.tripStop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.trip.tripStop.TripStopRequestDTO;
import com.carpool.carpool.enums.parameters.ParametersEnum;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.repository.parameters.ParametersRepository;
import com.carpool.carpool.utils.CoordsUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripStopComponent {

    private final ParametersRepository parametersRepository;
    private final CityRepository cityRepository;

    /**
     * Este metodo permite calcular las paradas intermedias. Para ello verifica las paradas, sus ciudades,
     * orden y calcula las distancias y tiempos estimados
     * @param trip				Objeto {@link Trip}
     * @param stopDTOs			Las paradas intermedias del tipo {@link TripStopRequestDTO}
     * @param baseStartTime		Horario de inicio del viaje
     * @return
     */
	public double buildStops(Trip trip, List<TripStopRequestDTO> stopDTOs, LocalDateTime baseStartTime) {

        double averageSpeed = getAverageSpeed();

        City previousCity = null;
        double totalDistanceAccumulated = 0.0;
        LocalDateTime currentArrivalTime = baseStartTime;
        int order = 1;

        for (TripStopRequestDTO dto : stopDTOs) {

            City currentCity = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> {
                        log.error("La ciudad con ID {} no existe.", dto.getCityId());
                        return new EntityNotFoundException("Ocurrió un error al intentar buscar las ciudades.");
                    });

            double distanceFromPrevious = 0.0;

            if (previousCity != null) {
                distanceFromPrevious = CoordsUtils.calculateDistance(
                        previousCity.getLatitude(), previousCity.getLongitude(),
                        currentCity.getLatitude(), currentCity.getLongitude()
                );
            }

            totalDistanceAccumulated += distanceFromPrevious;

            double estimatedHours = totalDistanceAccumulated / averageSpeed;
            currentArrivalTime = baseStartTime.plusMinutes((long) (estimatedHours * 60));

            TripStop newStop = TripStop.builder()
                    .city(currentCity)
                    .isStart(dto.isStart())
                    .isDestination(dto.isDestination())
                    .observation(dto.getObservation())
                    .stopOrder(order++)
                    .trip(trip)
                    .distanceFromPrevious(distanceFromPrevious)
                    .estimatedArrivalDateTime(currentArrivalTime)
                    .build();

            trip.getTripStops().add(newStop);
            previousCity = currentCity;
        }

        return totalDistanceAccumulated;
    }

    private double getAverageSpeed() {
        return parametersRepository.findByKeyName(ParametersEnum.AVERAGE_SPEED_KMH.getKey())
                .map(config -> Double.parseDouble(config.getKeyValue()))
                .orElse(80.0);
    }
}
