package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ListAlreadyExists extends WatchNextException {

    public ListAlreadyExists(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
