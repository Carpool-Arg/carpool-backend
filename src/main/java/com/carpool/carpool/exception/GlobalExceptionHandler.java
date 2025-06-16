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
    public ResponseEntity<Response<String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseUtils.buildErrorResponseUtil(HttpStatus.UNAUTHORIZED, List.of(ex.getMessage()));
    }

    /**
     * Excepcion utilizada cuando se viola alguna validación definida en la clase model. Ej: @NotNull
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseUtils.buildErrorResponseUtil(HttpStatus.BAD_REQUEST, List.of("Error de validación", ex.getMessage()));
    }

    /**
     * Excepcion utilizada cuando el argumento de la petición no es válido.
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseUtils.buildErrorResponseUtil(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Excepcion utilizada cuando falta algún argumento en la petición.
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<String>> handleArgumentMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseUtils.buildErrorResponseUtil(HttpStatus.BAD_REQUEST, List.of("Tipo de argumento inválido", ex.getMessage()));
    }

    /**
     * Excepcion genérica.
     * @param ex Excepción
     * @return Response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleGenericException(Exception ex) {
        return ResponseUtils.buildErrorResponseUtil(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Error inesperado", ex.getMessage()));
    }
}
