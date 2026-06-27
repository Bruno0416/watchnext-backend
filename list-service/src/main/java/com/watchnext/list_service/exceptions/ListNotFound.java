package com.watchnext.list_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ListNotFound extends WatchNextException {

    public ListNotFound(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ListNotFound() {
        this("Lista no encontrada o no pertenece al usuario");
    }
}
