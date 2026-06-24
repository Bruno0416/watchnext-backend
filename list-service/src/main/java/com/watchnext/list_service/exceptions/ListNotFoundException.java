package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ListNotFoundException extends WatchNextException {

    public ListNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ListNotFoundException() {
        this("Lista no encontrada o no pertenece al usuario");
    }
}
