package com.watchnext.content_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ErrorFetchingTvList extends WatchNextException {

    public ErrorFetchingTvList(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
