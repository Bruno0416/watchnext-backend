package com.watchnext.content_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class TmdbResourceNotFoundException extends WatchNextException {

    public TmdbResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
