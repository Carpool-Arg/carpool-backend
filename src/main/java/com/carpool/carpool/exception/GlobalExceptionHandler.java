package com.carpool.carpool.exception;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Excepcion utilizada cuando username no se encuentra.
     * @param ex ExcepciónExcepción
     * @return Response
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Response<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }   

    /**
     * Excepcion utilizada cuando se viola alguna validación definida en la clase model. Ej: @NotNull
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Error de validación", ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada cuando el argumento de la petición no es válido.
     * @param ex Excepción
     * @return Response
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
     * @return Response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleArgumentMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Tipo de argumento inválido", ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada para cuando hay un error del lado del cliente.
     * @param ex Excepción
     * @return ResponseEntity<Response<Void>>
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepcion utilizada para cuando un registro se encuentra en uso.
     * @param ex Excepción
     * @return ResponseEntity<Response<Void>>
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<Void>> handleConflict(ConflictException ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of(ex.getMessage())), HttpStatus.CONFLICT);
    }

    /**
     * Excepcion genérica.
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ResponseUtils.buildErrorResponse(List.of("Error inesperado",ex.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
