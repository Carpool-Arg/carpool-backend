package com.carpool.carpool.utils;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.response.ResponseStateEnum;
import com.carpool.carpool.security.utils.SimpleGrantedAuthorityJsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

/**
 * Clase de utilidad utilizada para mejorar aspectos referidos a la clase Response.
 */
public class ResponseUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class);

    /**
     * Este método crea una response con un mensaje y estado ERROR.
     * @param status Estado HTTP
     * @param messages Mensaje
     * @return Response
     */
    public static <T> ResponseEntity<Response<T>> buildErrorResponseUtil(HttpStatus status, List<String> messages) {
        Response<T> response = new Response<>();
        response.setMessages(messages);
        response.setState(ResponseStateEnum.ERROR);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Este método crea una response con un mensaje y estado ERROR.
     * @param status Estado HTTP
     * @param messages Mensaje
     * @param data Data
     * @return Response
     */
    public static <T> ResponseEntity<Response<T>> buildOKResponseUtil(HttpStatus status, List<String> messages, T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        response.setMessages(messages);
        response.setState(ResponseStateEnum.OK);
        return new ResponseEntity<>(response, status);
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
}
