package com.watchnext.feedback_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ReviewAlreadyExists extends WatchNextException {

    public ReviewAlreadyExists(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ReviewAlreadyExists() {
        super(HttpStatus.CONFLICT, "Ya existe una review para este recurso");
    }
}
