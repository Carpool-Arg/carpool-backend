package com.carpool.carpool.utils;

import java.text.Normalizer;

public class TextUtils {
    /**
     * Este metodo nos sirve para normalizar strings a un formato en donde se pasa todo a masyuisculas y se quitan los acentos
     * Sirve principalmente para poder normalizar los nombres de las ciudades ya que en la base de datos estan gaurdados sin acentos 
     * y todo en mayuscula
     * @param text el texto que queremos normalizar
     * @return el texto normalizado
     */
    public static String normalize(String text){
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toUpperCase();
    }
}


