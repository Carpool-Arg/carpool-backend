package com.carpool.carpool.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Clase utils para el formateo de fechas que se mostraran en el front
public class DateUtils {
    /**
     * Metodo que sirve para formatear una fecha que no tiene fecha y hora a un formato
     * mas tradicional
     * @param date la fecha que queremos formatear
     * @return un String con la fecha formateada
     */
    public static String formatDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = date.format(formatter);
        return formattedDate;
    }


    /**
     * Metodo que sirve para formatear una fecha con hora a un formato mas
     * tradicional
     * @param dateTime la fecha y hora que queremos formatear
     * @return un String con la fecha y hora formateada
     */
    public static String formatDateTime(LocalDateTime dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);
        return formattedDateTime;
    }


}
