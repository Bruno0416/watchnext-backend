package com.watchnext.common.exceptions;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handler para TODAS las excepciones personalizadas
    @ExceptionHandler(WatchNextException.class)
    public ProblemDetail handleWatchNextException(WatchNextException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatus(),
            ex.getMessage()
        );

        problemDetail.setTitle("Error en la solicitud");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 2. Handler errores de validaciones de WebMVC
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrorsMvc(
        MethodArgumentNotValidException ex
    ) {
        return buildValidationProblemDetail(
            ex.getBindingResult().getFieldErrors()
        );
    }

    // 3. Handler errores de validaciones de WebFlux
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationErrorsFlux(
        WebExchangeBindException ex
    ) {
        return buildValidationProblemDetail(
            ex.getBindingResult().getFieldErrors()
        );
    }

    // 4. Handler para JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "El cuerpo de la solicitud es invalido o esta mal formado"
        );

        problemDetail.setTitle("Error en la solicitud");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 5. Handler para violaciones de @Validated en @PathVariable y @RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(
        ConstraintViolationException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Error en la validación de los parametros"
        );

        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("invalid_fields", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 6. Fallback — cualquier excepcion no manejada
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrio un error inesperado. Por favor intenta de nuevo."
        );

        problemDetail.setTitle("Error interno del servidor");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // Helper para construir errores de validaciones
    private ProblemDetail buildValidationProblemDetail(
        List<FieldError> fieldErrors
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Error en la validación de los datos"
        );
        problemDetail.setTitle("Bad Request");

        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach(e ->
            errors.put(e.getField(), e.getDefaultMessage())
        );

        problemDetail.setProperty("invalid_fields", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
