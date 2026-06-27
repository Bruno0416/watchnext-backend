package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ItemAlreadyExists extends WatchNextException {

    public ItemAlreadyExists(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
