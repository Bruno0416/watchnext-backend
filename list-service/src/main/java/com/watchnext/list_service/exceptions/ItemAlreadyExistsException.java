package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ItemAlreadyExistsException extends WatchNextException {

    public ItemAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
