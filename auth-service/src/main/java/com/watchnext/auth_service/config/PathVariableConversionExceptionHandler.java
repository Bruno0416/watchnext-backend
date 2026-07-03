package com.watchnext.auth_service.config;

import com.watchnext.common.exceptions.WatchNextException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class PathVariableConversionExceptionHandler {

    // Spring MVC envuelve las excepciones lanzadas por un Converter de @PathVariable
    // GlobalExceptionHandler nunca ve la excepcion
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
        MethodArgumentTypeMismatchException ex
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
}
