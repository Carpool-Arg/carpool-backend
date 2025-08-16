package com.carpool.carpool.exception;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.List;

@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Excepcion utilizada cuando username no se encuentra.
     * @param ex ExcepciónExcepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Response<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }   

    /**
     * Excepcion utilizada cuando se viola alguna validación definida en la clase model. Ej: @NotNull
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Error de validación", ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada cuando el argumento de la petición no es válido.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada cuando falta algún argumento en la petición.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleArgumentMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Tipo de argumento inválido", ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada para cuando hay un error del lado del cliente.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada para cuando un recurso no se encuentra.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Void>> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.NOT_FOUND);
    }

    /**
     * Excepcion utilizada para cuando un registro se encuentra en uso.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<Void>> handleConflict(ConflictException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.CONFLICT);
    }

    /**
     * Excepcion utilizada para cuando hay un error de conexion con redis
     * @param ex Excepción de conexion de redis
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Response<Void>> handleRedisException(RedisConnectionFailureException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.CONFLICT);
    }

    /**
     * Excepcion utilizada para cuando hay un error relacionado a la auth con Google
     * @param ex Excepción de invalidación con Google
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response<Void>> handleInvalidGoogleToken(UnauthorizedException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Excepcion genérica.
     * @param ex Excepción
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Error inesperado",ex.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Excepcion utilizada cuando el formato de una fecha es incorrecto.
     * @param ex Excepción de formato de mensaje no leíble
     * @return {@link ResponseEntity} que contiene {@link Response} con data {@link Void}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorMessage = "Formato de fecha inválido. Se esperaba el formato dd/MM/yyyy.";
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(errorMessage)), HttpStatus.BAD_REQUEST);
    }
}
