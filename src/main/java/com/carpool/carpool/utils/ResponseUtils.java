package com.carpool.carpool.utils;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.response.ResponseStateEnum;
import com.carpool.carpool.security.utils.SimpleGrantedAuthorityJsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Clase de utilidad utilizada para mejorar aspectos referidos a la clase Response.
 */
public class ResponseUtils {

    private static final ObjectMapper mapper = new ObjectMapper().addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class);

    /**
     * Constructor privado para evitar el instanciamiento de una clase Utils.
     */
    private ResponseUtils(){

    }
    /**
     * Método utilizado para escribir una Response
     * @param response
     * @param entity
     * @param contentType
     * @throws IOException
     */
    public static void writeResponse(HttpServletResponse response, ResponseEntity<?> entity, String contentType) throws IOException {
        response.setStatus(entity.getStatusCodeValue());
        response.setContentType(contentType);
        response.getWriter().write(mapper.writeValueAsString(entity.getBody()));
    }

    /**
     * Metodo para construir la Response con estado OK definida por nosotros,
     * si la response es estado OK se le pasa como parametro la data del tipo 
     * que definamos
     * @param <T> tipo de la data
     * @param messages mensajes que queramos devolver 
     * @param data el cuerpo de la respuesta
     * @return Response con estado OK
     */
    public static <T> Response<T> buildOKResponse(List<String> messages, T data){
        Response<T> response = new Response<>();
        response.setData(data);
        response.setMessages(messages);
        response.setState(ResponseStateEnum.OK);
        return response;
    }

    /**
     * Metodo para construir la Response con estado ERROR, 
     * no se le pasa la data, solo los mensajes 
     * @param messages mensajes que queramos devolver 
     * @return Response con estado ERROR
     */
    public static Response<Void> buildErrorResponse(List<String> messages){
        Response<Void> response = new Response<>();
        response.setMessages(messages);
        response.setState(ResponseStateEnum.ERROR);
        return response;
    }

}
