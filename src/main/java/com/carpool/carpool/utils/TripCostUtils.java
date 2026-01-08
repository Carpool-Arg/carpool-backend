package com.carpool.carpool.utils;

import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;

public class TripCostUtils {

  /**
   * Metodo para determinar el total a cobrar para un viaje, si es desde el origen
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
            double totalDistance = CoordsUtils.calculateDistance(
                originCity.getLatitude(), 
                originCity.getLongitude(), 
                tripDestination.getCity().getLatitude(), 
                tripDestination.getCity().getLongitude()
            );
            return (trip.getKilometerPrice() * totalDistance) + trip.getDriverPriceDiscount();
        }
    }else if(originCity.getId() == tripOrigin.getCity().getId() && destinationCity.getId() == tripDestination.getCity().getId()){
        return trip.getPublishedSeatPrice();
    }else{
        double totalDistance = CoordsUtils.calculateDistance(
            originCity.getLatitude(), 
            originCity.getLongitude(), 
            destinationCity.getLatitude(), 
            destinationCity.getLongitude()
        );

        return (trip.getKilometerPrice() * totalDistance) + trip.getDriverPriceDiscount();
    }
  }
}
