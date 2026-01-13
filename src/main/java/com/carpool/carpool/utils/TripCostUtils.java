package com.carpool.carpool.utils;

import java.util.List;

import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;

public class TripCostUtils {

  /**
   * Método para determinar el total a cobrar para un viaje, si es desde el origen
   * hasta el destino del viaje devolvemos el precio publicado del viaje, sino devolvemos un total 
   * calculado segun el precio por kilometro del viaje
   * @param originCity ciudad de origen de la reserva
   * @param destinationCity ciudad de origen de la reserva
   * @param trip el viaje al que se hace la reserva
   * @return el total a cobrar
   */
  public static double calculateTripTotal(City originCity, City destinationCity, Trip trip ){
    TripStop tripOrigin = trip.getTripStops().stream()
        .filter(TripStop::isStart)
        .findFirst()
        .orElseThrow(() -> new ConflictException("No existe el origen del viaje."));

    TripStop tripDestination = trip.getTripStops().stream()
        .filter(TripStop::isDestination)
        .findFirst()
        .orElseThrow(() -> new ConflictException("No existe el destino del viaje."));

    
    /*
    Hacemos esto para poder hacer el calculo y mostrar el precio correspondiente en el feed, 
    ya que el mismo no puede pasar una ciudad de destino como parametro, por lo tanto si la ciudad de destino es null
    usamos el destino del viaje para determinar si hay que usar el precio por kilometro o no 
     */
    if(destinationCity == null ){
        if(originCity.getId() == tripOrigin.getCity().getId()){
            return trip.getPublishedSeatPrice();
        }else{
            return (trip.getKilometerPrice() * calculateDistanceBetweenStops(originCity, tripDestination.getCity(), trip.getTripStops())) + trip.getDriverPriceDiscount();
        }
    }else if(originCity.getId() == tripOrigin.getCity().getId() && destinationCity.getId() == tripDestination.getCity().getId()){
        return trip.getPublishedSeatPrice();
    }else{
        return (trip.getKilometerPrice() * calculateDistanceBetweenStops(originCity, destinationCity, trip.getTripStops())) + trip.getDriverPriceDiscount();
    }
  }


  public static double calculateDistanceBetweenStops(City originCity, City destinationCity,List<TripStop> tripStops){
    if(tripStops.isEmpty() || tripStops == null){
        throw new ConflictException("La lista de paradas del viaje está vacía.");
    }

    TripStop origin = tripStops.stream()
        .filter(ts -> ts.getCity().equals(originCity))
        .findFirst()
    .orElseThrow(() -> new ConflictException("No se encontró una parada intermedia con la ciudad de origen ingresada."));

    TripStop destination = tripStops.stream()
        .filter(ts -> ts.getCity().equals(destinationCity))
        .findFirst()
    .orElseThrow(() -> new ConflictException("No se encontró una parada intermedia con la ciudad de destino ingresada."));
    
    
    if(origin.getStopOrder() > destination.getStopOrder()){
        throw new IllegalArgumentException("El orden de la parada de origen no puede ser mayor al de la parada de destino.");
    }
    
    return tripStops.stream()
        .filter(ts -> ts.getStopOrder() > origin.getStopOrder() &&
            ts.getStopOrder() <= destination.getStopOrder())
        .mapToDouble(TripStop::getDistanceFromPrevious)
    .sum();
  }
}
