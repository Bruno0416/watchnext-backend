package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ListAlreadyExistsException extends WatchNextException {

    public ListAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
