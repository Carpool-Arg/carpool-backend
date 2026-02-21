package com.carpool.carpool.mappers.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.carpool.carpool.dto.trip.CurrentTripResponseDTO;
import com.carpool.carpool.dto.trip.TripDriverDTO;
import com.carpool.carpool.dto.trip.TripPriceCalculationResponseDTO;

import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;
import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.driver.DriverSearchResponseDTO;
import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.TripSearchResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.CurrentTripStopResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopResponseDTO;
import com.carpool.carpool.dto.trip.tripStop.TripStopSearchResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseTripDTO;
import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.service.media.IMediaService;
import com.carpool.carpool.service.trip.tripStop.TripStopComponent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TripMapper {   
	
    private final IMediaService mediaService;
    private final StateHistoryRepository stateHistoryRepository;
    private final TripStopComponent tripStopComponent;
    
    public Trip convertTripRequestDTOToTrip(TripRequestDTO tripRequestDTO, Vehicle vehicle){ 
        Trip trip = Trip.builder()
            .startTripDateTime(tripRequestDTO.getStartDateTime())
            .availableSeat(tripRequestDTO.getAvailableSeat())
            .currentAvailableSeats(tripRequestDTO.getAvailableSeat())
            .availableBaggage(BaggageEnum.valueOf(tripRequestDTO.getAvailableBaggage()))
            .vehicle(vehicle)
            .seatPrice(tripRequestDTO.getSeatPrice())
            .tripStops(new ArrayList<>())
        .build();
        
        LocalDateTime baseStartTime = tripRequestDTO.getStartDateTime();

        double totalDistance = tripStopComponent.buildStops(trip, tripRequestDTO.getTripStops(), baseStartTime);
        
        if (totalDistance <= 0) {
            throw new ConflictException("No se pudo calcular la distancia total del viaje.");
        }
        
        trip.setKilometerPrice(tripRequestDTO.getSeatPrice() / totalDistance);
        return trip;

    }

    public TripResponseDTO convertTripToTripResponseDTO(Trip trip) {
       
        Driver driver = trip.getVehicle().getDriver();
        User user = driver.getUser();
        String profilePictureUrl = mediaService.getProfilePictureUrlByUserId(user.getId());
       
        List<TripStopResponseDTO> tripStopResponseDTOs = getTripstopResponseDTO(trip);
        
        Vehicle vehicleEntity = trip.getVehicle();
        VehicleResponseTripDTO vehicle = VehicleResponseTripDTO.builder()
        	.id(vehicleEntity.getId())
            .domain(vehicleEntity.getDomain())
            .vehicleTypeName(vehicleEntity.getVehicleType().getName())
            .brand(vehicleEntity.getBrand())
            .model(vehicleEntity.getModel())
            .color(vehicleEntity.getColor())
            .build();

        DriverSearchResponseDTO driverSearchDTO = DriverSearchResponseDTO.builder()
            .driverId(driver.getId())
            .fullName(user.getName() + " " + user.getLastname()) 
            .profileImageUrl(profilePictureUrl)
            .rating(driver.getRating()) 
            .build();

        return TripResponseDTO.builder()
            .id(trip.getId())
            .driverInfo(driverSearchDTO)
            .tripStops(tripStopResponseDTOs)
            .vehicle(vehicle)
            .startDateTime(trip.getStartTripDateTime())
            .availableSeat(trip.getAvailableSeat())
            .currentAvailableSeats(trip.getCurrentAvailableSeats())
            .availableBaggage(trip.getAvailableBaggage().toString())
            .seatPrice(roundPrice(trip.getPublishedSeatPrice())) 
            .build();
    }

    public CurrentTripResponseDTO covertTripToCurrentTripResponseDTO(Trip trip){
        List<CurrentTripStopResponseDTO> currentTripStopResponseDTO = getCurrentTripstopsResponseDTO(trip);

        return CurrentTripResponseDTO.builder()
            .idTrip(trip.getId())
            .tripStops(currentTripStopResponseDTO)
            .totalDistance(trip.getTripStops().stream().mapToDouble(TripStop::getDistanceFromPrevious).sum())
            .build();

    }   

    /**
     * Este metodo obtiene un listado de viajes y los convierte en un listado de objetos {@link TripDriverDTO}
     * @param listTrips Listado de viajes
     * @return Lista que contiene objetos {@link TripDriverDTO}
     */
    public List<TripDriverDTO> convertTripToTripDriverResponseDTO(List<Trip> listTrips) {

        return listTrips.stream()
                .map(trip -> {

                    boolean hasReservations =
                            stateHistoryRepository.hasAcceptedReservations(trip.getId());

                    Vehicle vehicle = trip.getVehicle();

                    VehicleResponseTripDTO vehicleDTO = VehicleResponseTripDTO.builder()
                            .domain(vehicle.getDomain())
                            .vehicleTypeName(vehicle.getVehicleType().getName())
                            .brand(vehicle.getBrand())
                            .model(vehicle.getModel())
                            .color(vehicle.getColor())
                            .build();

                    String startCity = trip.getTripStops().stream()
                            .filter(TripStop::isStart)
                            .findFirst()
                            .map(ts -> ts.getCity().getName())
                            .orElse("");

                    String destinationCity = trip.getTripStops().stream()
                            .filter(TripStop::isDestination)
                            .findFirst()
                            .map(ts -> ts.getCity().getName())
                            .orElse("");

                    LocalDateTime estimatedArrivalDate = trip.getTripStops().stream()
                            .filter(TripStop::isDestination)
                            .findFirst()
                            .map(TripStop::getEstimatedArrivalDateTime)
                            .orElse(null);

                    return TripDriverDTO.builder()
                            .id(trip.getId())
                            .vehicle(vehicleDTO)
                            .startDateTime(trip.getStartTripDateTime())
                            .availableSeat(trip.getAvailableSeat())
                            .currentAvailableSeats(trip.getCurrentAvailableSeats())
                            .startCity(startCity)
                            .destinationCity(destinationCity)
                            .availableBaggage(trip.getAvailableBaggage().getTypeBaggage())
                            .seatPrice(roundPrice(trip.getSeatPrice() - trip.getDriverPriceDiscount()))
                            .estimatedArrivalDateTime(estimatedArrivalDate)
                            .tripState(getCurrentTripStatusName(trip))
                            .hasReservations(hasReservations)
                            .build();
                })
                .toList();
    }

    public TripSearchResponseDTO converTripToTripSearchResponseDTO(Trip trip, double total) {
       
        Driver driver = trip.getVehicle().getDriver();
        User user = driver.getUser(); 
        String profilePictureUrl = mediaService.getProfilePictureUrlByUserId(user.getId());

        DriverSearchResponseDTO driverSearchDTO = DriverSearchResponseDTO.builder()
            .fullName(user.getName() + " " + user.getLastname()) 
            .profileImageUrl(profilePictureUrl)
            .rating(driver.getRating()) 
            .build();
        
        List<TripStopSearchResponseDTO> tripStopSearchResponseDTOs = trip.getTripStops().stream()
            .map(tripStop -> TripStopSearchResponseDTO.builder()
                .cityName(tripStop.getCity().getName())
                .observation(tripStop.getObservation())
                .start(tripStop.isStart())
                .destination(tripStop.isDestination())
                .estimatedArrivalDateTime(tripStop.getEstimatedArrivalDateTime())
                .build())
            .collect(Collectors.toList());


        return TripSearchResponseDTO.builder()
            .driverInfo(driverSearchDTO)
            .startDateTime(trip.getStartTripDateTime())
            .tripStops(tripStopSearchResponseDTOs) 
            .availableSeat(trip.getCurrentAvailableSeats())
            .seatPrice(roundPrice(total))
            .tripId(trip.getId())
            .build();
    }


    private List<TripStopResponseDTO> getTripstopResponseDTO(Trip trip){
        return trip.getTripStops().stream()
            .map(tripStop -> TripStopResponseDTO.builder()
                .cityId(tripStop.getCity().getId())
                .cityName(tripStop.getCity().getName())
                .estimatedArrivalDateTime(tripStop.getEstimatedArrivalDateTime())
                .observation(tripStop.getObservation())
                .order(tripStop.getStopOrder())
                .start(tripStop.isStart())
                .destination(tripStop.isDestination())
                .build())
        .collect(Collectors.toList());
    }

    private List<CurrentTripStopResponseDTO> getCurrentTripstopsResponseDTO(Trip trip) {
        return trip.getTripStops().stream()
            .map(tripStop -> {
                TripStopResponseDTO tripStopResponseDTO =
                    TripStopResponseDTO.builder()
                        .cityId(tripStop.getCity().getId())
                        .cityName(tripStop.getCity().getName())
                        .estimatedArrivalDateTime(tripStop.getEstimatedArrivalDateTime())
                        .observation(tripStop.getObservation())
                        .order(tripStop.getStopOrder())
                        .start(tripStop.isStart())
                        .destination(tripStop.isDestination())
                        .build();

                return CurrentTripStopResponseDTO.builder()
                    .tripStop(tripStopResponseDTO)
                    .arrivalDateTime(tripStop.getArrivalDateTime())
                    .distanceFromPrevious(tripStop.getDistanceFromPrevious())
                    .tripstopId(tripStop.getId())
                    .build();
            })
            .collect(Collectors.toList());
    }



    public TripPriceCalculationResponseDTO convertTriptoTripPriceCalculationResponseDTO(double seatPrice, double splitCommission) { 
        return TripPriceCalculationResponseDTO.builder()
                .seatPrice(roundPrice(seatPrice))
                .publishedSeatPrice(roundPrice(seatPrice + splitCommission))
                .driverPriceDiscount(roundPrice(splitCommission))
                .netEarningsPerSeat(roundPrice(seatPrice - splitCommission))
                .build();
    }

    private double roundPrice(double value) {
        return BigDecimal.valueOf(value)
                .setScale(3, RoundingMode.HALF_UP) 
                .doubleValue();
    }

    private String getCurrentTripStatusName(Trip trip) {
        return trip.getStateHistory().stream()
            .filter(history -> history.getFinishDateTime() == null)
            .map(history -> history.getState().getName())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "El viaje no tiene un estado actual"
            ));
    }
}
