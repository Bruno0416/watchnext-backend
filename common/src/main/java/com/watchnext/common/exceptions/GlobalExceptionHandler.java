package com.watchnext.common.exceptions;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handler especifico para limite de envios excedido (429), con header Retry-After
    @ExceptionHandler(RateLimitExceeded.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(
        RateLimitExceeded ex
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatus(),
            ex.getMessage()
        );

        problemDetail.setTitle("Error en la solicitud");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(ex.getStatus())
            .header(
                HttpHeaders.RETRY_AFTER,
                String.valueOf(ex.getRetryAfterSeconds())
            )
            .body(problemDetail);
    }

    // 2. Handler para TODAS las demas excepciones personalizadas
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

    // 3. Handler errores de validaciones de WebMVC
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrorsMvc(
        MethodArgumentNotValidException ex
    ) {
        return buildValidationProblemDetail(
            ex.getBindingResult().getFieldErrors()
        );
    }

    // 4. Handler errores de validaciones de WebFlux
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationErrorsFlux(
        WebExchangeBindException ex
    ) {
        return buildValidationProblemDetail(
            ex.getBindingResult().getFieldErrors()
        );
    }

    // 5. Handler para JSON mal formado
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

    // 6. Handler para violaciones de @Validated en @PathVariable y @RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(
        ConstraintViolationException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fullPath = violation.getPropertyPath().toString();
            String field = fullPath.substring(fullPath.lastIndexOf('.') + 1);
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

    // 6.5. Handler para errores de conversion en WebFlux
    @ExceptionHandler(org.springframework.web.server.ServerWebInputException.class)
    public ProblemDetail handleServerWebInputException(
        org.springframework.web.server.ServerWebInputException ex
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Valor invalido o faltante en la solicitud"
        );
        problemDetail.setTitle("Error en la solicitud");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 7. Handler para errores de conversion de @PathVariable (Spring MVC envuelve la excepcion original)
    @ExceptionHandler(
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    )
    public ProblemDetail handlePathVariableTypeMismatch(
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
            ex
    ) {
        Throwable cause = ex.getCause();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message =
            "Valor invalido para el parametro '" + ex.getName() + "'";

        if (cause instanceof WatchNextException watchNextException) {
            status = watchNextException.getStatus();
            message = watchNextException.getMessage();
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            status,
            message
        );
        problemDetail.setTitle("Error en la solicitud");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 8. Fallback - cualquier excepcion no manejada
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrio un error inesperado. Por favor intenta de nuevo."
        );

        problemDetail.setTitle("Error interno del servidor");
        problemDetail.setProperty("timestamp", Instant.now());

        System.out.println(ex.getMessage());

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
