package com.watchnext.feedback_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class RatingAlreadyExists extends WatchNextException {

    public RatingAlreadyExists(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public RatingAlreadyExists() {
        super(
            HttpStatus.CONFLICT,
            "Ya existe una calificacion para este recurso"
        );
    }
}
