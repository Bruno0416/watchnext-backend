package com.watchnext.content_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class TmdbResourceNotFound extends WatchNextException {

    public TmdbResourceNotFound(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
