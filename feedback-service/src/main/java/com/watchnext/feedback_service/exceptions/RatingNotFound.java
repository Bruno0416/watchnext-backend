package com.watchnext.feedback_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class RatingNotFound extends WatchNextException {

    public RatingNotFound(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public RatingNotFound() {
        super(
            HttpStatus.NOT_FOUND,
            "La calificacion solicitada no existe o no le pertenece a este usuario."
        );
    }
}
