package com.watchnext.content_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ErrorFetchingMovieList extends WatchNextException {

    public ErrorFetchingMovieList(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
