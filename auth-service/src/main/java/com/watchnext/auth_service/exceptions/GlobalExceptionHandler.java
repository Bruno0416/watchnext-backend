package com.watchnext.auth_service.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler para excepciones personalizadas
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ProblemDetail handleEmailAlreadyInUse(
        EmailAlreadyInUseException ex,
        HttpServletRequest request
    ) {
        // Spring Boot crea la estructura estándar automáticamente
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );

        // Puedes agregar campos personalizados (como el timestamp o el endpoint)
        problemDetail.setTitle("Conflicto de registro");
        problemDetail.setType(
            URI.create("https://watchnext.com/errors/email-in-use")
        );
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("endpoint", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(
        InvalidCredentialsException ex,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage()
        );
        problemDetail.setTitle("Credenciales inválidas");
        problemDetail.setType(
            URI.create("https://watchnext.com/errors/invalid-credentials")
        );
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("endpoint", request.getRequestURI());
        return problemDetail;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(
        InvalidRefreshTokenException ex,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage()
        );
        problemDetail.setTitle("Refresh token inválido");
        problemDetail.setType(
            URI.create("https://watchnext.com/errors/invalid-refresh-token")
        );
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("endpoint", request.getRequestURI());
        return problemDetail;
    }

    // Handler para errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Error en la validación de los datos"
        );
        problemDetail.setTitle("Bad Request");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        // Agregamos el mapa de errores específicos al JSON de respuesta
        problemDetail.setProperty("invalid_fields", errors);
        problemDetail.setProperty("endpoint", request.getRequestURI());

        return problemDetail;
    }
}
